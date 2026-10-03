#!/usr/bin/env python3
"""Restore the repository backup into an ephemeral SQL Server and inspect schema/access-control sources.

This script does not SELECT business rows or personal account rows, execute application procedures,
or mutate restored database contents after restore. Its JSON output is encrypted by GitHub Actions.
"""
from __future__ import annotations

import datetime as dt
import json
import os
from pathlib import Path
import sys
import time
from typing import Any

import pytds

BACKUP = "/var/opt/mssql/backup/Atiran2.bak"
DATABASE = "Atiran2"
CURRENT_STAGE = "initialization"


def json_value(value: Any) -> Any:
    if isinstance(value, (dt.datetime, dt.date, dt.time)):
        return value.isoformat()
    if isinstance(value, (bytes, bytearray, memoryview)):
        return {"binary_length": len(value)}
    return value


def connect(database: str = "master", tries: int = 60):
    last_error = None
    for _ in range(tries):
        try:
            return pytds.connect(
                server="127.0.0.1",
                port=1433,
                database=database,
                user="sa",
                password=os.environ["SA_PASS"],
                autocommit=True,
                timeout=900,
                login_timeout=10,
            )
        except Exception as error:  # SQL Server may still be starting its initialization scripts.
            last_error = error
            time.sleep(3)
    raise RuntimeError("SQL Server did not become ready") from last_error


def query(cursor, statement: str) -> list[dict[str, Any]]:
    cursor.execute(statement)
    if cursor.description is None:
        while cursor.nextset():
            pass
        return []
    names = [column[0] for column in cursor.description]
    rows = [
        {name: json_value(value) for name, value in zip(names, row)}
        for row in cursor.fetchall()
    ]
    while cursor.nextset():
        pass
    return rows


def sql_literal(value: str) -> str:
    return "N'" + value.replace("'", "''") + "'"


def quote_identifier(value: str) -> str:
    return "[" + value.replace("]", "]]" ) + "]"


def wait_until_ready() -> None:
    consecutive_successes = 0
    for _ in range(60):
        try:
            connection = connect(tries=1)
            cursor = connection.cursor()
            query(cursor, "SELECT 1")
            connection.close()
            consecutive_successes += 1
            if consecutive_successes == 3:
                return
        except Exception:
            consecutive_successes = 0
        time.sleep(5)
    raise RuntimeError("SQL Server readiness check timed out")


def restore_and_collect(output_path: Path) -> None:
    global CURRENT_STAGE
    CURRENT_STAGE = "wait for isolated SQL Server readiness"
    wait_until_ready()
    connection = connect()
    cursor = connection.cursor()

    CURRENT_STAGE = "read backup header metadata"
    header_rows = query(cursor, f"RESTORE HEADERONLY FROM DISK = {sql_literal(BACKUP)}")
    if not header_rows:
        raise RuntimeError("Backup header is empty")
    header = header_rows[0]
    # Keep only non-sensitive backup provenance fields; omit host, user and media-path fields.
    allowed_header_fields = (
        "DatabaseName",
        "BackupTypeDescription",
        "BackupStartDate",
        "BackupFinishDate",
        "DatabaseVersion",
        "CompatibilityLevel",
        "BackupSize",
        "CompressedBackupSize",
    )
    backup_metadata = {key: header.get(key) for key in allowed_header_fields if key in header}

    CURRENT_STAGE = "read backup file metadata"
    files = query(cursor, f"RESTORE FILELISTONLY FROM DISK = {sql_literal(BACKUP)}")
    if not files:
        raise RuntimeError("Backup file list is empty")
    # Do not export physical paths from the backup. Move every data/log file to a fixed
    # path inside this throwaway SQL Server container.
    data_index = 0
    log_index = 0
    moves: list[str] = []
    file_metadata: list[dict[str, str]] = []
    for file_row in files:
        logical_name = str(file_row.get("LogicalName", ""))
        file_type = str(file_row.get("Type", ""))
        if not logical_name or file_type not in ("D", "L"):
            raise RuntimeError("Backup contains an unsupported or unnamed database file type")
        if file_type == "D":
            target = (
                "/var/opt/mssql/data/Atiran2.mdf"
                if data_index == 0
                else f"/var/opt/mssql/data/Atiran2_{data_index}.ndf"
            )
            data_index += 1
            type_label = "data"
        else:
            target = (
                "/var/opt/mssql/data/Atiran2_log.ldf"
                if log_index == 0
                else f"/var/opt/mssql/data/Atiran2_log_{log_index}.ldf"
            )
            log_index += 1
            type_label = "log"
        moves.append(f"MOVE {sql_literal(logical_name)} TO {sql_literal(target)}")
        file_metadata.append({"logical_name": logical_name, "type": type_label})

    if data_index == 0 or log_index == 0:
        raise RuntimeError("Backup must contain at least one data file and one log file")

    CURRENT_STAGE = "restore dated backup inside the ephemeral SQL Server container"
    restore_sql = (
        f"RESTORE DATABASE {quote_identifier(DATABASE)} FROM DISK = {sql_literal(BACKUP)} "
        "WITH REPLACE, RECOVERY, " + ", ".join(moves) + ", STATS = 10"
    )
    query(cursor, restore_sql)
    connection.close()

    CURRENT_STAGE = "collect SQL catalog metadata only"
    connection = connect(DATABASE, tries=10)
    cursor = connection.cursor()
    # This catalog section reads SQL Server system metadata only; it does not read business rows.
    server_rows = query(
        cursor,
        "SELECT DB_NAME() AS database_name, "
        "CAST(SERVERPROPERTY('ProductVersion') AS nvarchar(40)) AS product_version, "
        "CAST(SERVERPROPERTY('ProductLevel') AS nvarchar(40)) AS product_level, "
        "CAST(SERVERPROPERTY('Edition') AS nvarchar(100)) AS edition, "
        "CAST(DATABASEPROPERTYEX(DB_NAME(), 'Collation') AS nvarchar(128)) AS database_collation, "
        "d.compatibility_level, d.create_date "
        "FROM sys.databases AS d WHERE d.database_id = DB_ID()",
    )

    objects = query(
        cursor,
        "SELECT s.name AS schema_name, o.name AS object_name, o.type, o.type_desc, "
        "o.create_date, o.modify_date, "
        "CASE WHEN o.type = 'U' THEN t.temporal_type_desc ELSE NULL END AS temporal_type_desc, "
        "CASE WHEN o.type = 'U' THEN t.is_memory_optimized ELSE NULL END AS is_memory_optimized "
        "FROM sys.objects AS o "
        "JOIN sys.schemas AS s ON s.schema_id = o.schema_id "
        "LEFT JOIN sys.tables AS t ON t.object_id = o.object_id "
        "WHERE o.is_ms_shipped = 0 AND o.type IN ('U','V','P','FN','IF','TF','FS','FT','TR','SN') "
        "ORDER BY s.name, o.type, o.name",
    )

    columns = query(
        cursor,
        "SELECT s.name AS schema_name, o.name AS object_name, c.column_id, c.name AS column_name, "
        "ut.name AS user_type_name, st.name AS system_type_name, c.max_length, c.precision, c.scale, "
        "c.is_nullable, c.is_identity, c.is_computed, c.is_rowguidcol, c.is_sparse, c.is_column_set, "
        "c.generated_always_type_desc, c.collation_name, "
        "CASE WHEN c.default_object_id <> 0 THEN 1 ELSE 0 END AS has_default "
        "FROM sys.columns AS c "
        "JOIN sys.objects AS o ON o.object_id = c.object_id "
        "JOIN sys.schemas AS s ON s.schema_id = o.schema_id "
        "JOIN sys.types AS ut ON ut.user_type_id = c.user_type_id "
        "JOIN sys.types AS st ON st.system_type_id = c.system_type_id AND st.user_type_id = st.system_type_id "
        "WHERE o.is_ms_shipped = 0 AND o.type IN ('U','V') "
        "ORDER BY s.name, o.name, c.column_id",
    )

    keys_and_indexes = query(
        cursor,
        "SELECT s.name AS schema_name, o.name AS object_name, i.name AS index_name, i.type_desc, "
        "i.is_primary_key, i.is_unique, i.is_unique_constraint, i.is_disabled, i.has_filter, "
        "ic.key_ordinal, ic.is_included_column, ic.is_descending_key, c.name AS column_name "
        "FROM sys.indexes AS i "
        "JOIN sys.objects AS o ON o.object_id = i.object_id "
        "JOIN sys.schemas AS s ON s.schema_id = o.schema_id "
        "LEFT JOIN sys.index_columns AS ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id "
        "LEFT JOIN sys.columns AS c ON c.object_id = ic.object_id AND c.column_id = ic.column_id "
        "WHERE o.is_ms_shipped = 0 AND o.type = 'U' AND i.index_id > 0 "
        "ORDER BY s.name, o.name, i.index_id, ic.key_ordinal, ic.index_column_id",
    )

    foreign_keys = query(
        cursor,
        "SELECT fk.name AS foreign_key_name, ps.name AS parent_schema, pt.name AS parent_table, "
        "pc.name AS parent_column, rs.name AS referenced_schema, rt.name AS referenced_table, "
        "rc.name AS referenced_column, fkc.constraint_column_id, fk.is_disabled, fk.is_not_trusted, "
        "fk.delete_referential_action_desc, fk.update_referential_action_desc "
        "FROM sys.foreign_keys AS fk "
        "JOIN sys.foreign_key_columns AS fkc ON fkc.constraint_object_id = fk.object_id "
        "JOIN sys.tables AS pt ON pt.object_id = fkc.parent_object_id "
        "JOIN sys.schemas AS ps ON ps.schema_id = pt.schema_id "
        "JOIN sys.columns AS pc ON pc.object_id = fkc.parent_object_id AND pc.column_id = fkc.parent_column_id "
        "JOIN sys.tables AS rt ON rt.object_id = fkc.referenced_object_id "
        "JOIN sys.schemas AS rs ON rs.schema_id = rt.schema_id "
        "JOIN sys.columns AS rc ON rc.object_id = fkc.referenced_object_id AND rc.column_id = fkc.referenced_column_id "
        "ORDER BY ps.name, pt.name, fk.name, fkc.constraint_column_id",
    )

    constraints = query(
        cursor,
        "SELECT s.name AS schema_name, t.name AS table_name, cc.name AS constraint_name, "
        "'CHECK' AS constraint_type, cc.is_disabled, cc.is_not_trusted, cc.definition "
        "FROM sys.check_constraints AS cc "
        "JOIN sys.tables AS t ON t.object_id = cc.parent_object_id "
        "JOIN sys.schemas AS s ON s.schema_id = t.schema_id "
        "UNION ALL "
        "SELECT s.name, t.name, dc.name, 'DEFAULT', 0, 0, dc.definition "
        "FROM sys.default_constraints AS dc "
        "JOIN sys.tables AS t ON t.object_id = dc.parent_object_id "
        "JOIN sys.schemas AS s ON s.schema_id = t.schema_id "
        "ORDER BY schema_name, table_name, constraint_type, constraint_name",
    )

    parameters = query(
        cursor,
        "SELECT s.name AS schema_name, o.name AS object_name, o.type_desc, p.parameter_id, "
        "p.name AS parameter_name, ty.name AS type_name, p.max_length, p.precision, p.scale, "
        "p.is_output, p.is_cursor_ref, p.has_default_value "
        "FROM sys.parameters AS p "
        "JOIN sys.objects AS o ON o.object_id = p.object_id "
        "JOIN sys.schemas AS s ON s.schema_id = o.schema_id "
        "JOIN sys.types AS ty ON ty.user_type_id = p.user_type_id "
        "WHERE o.is_ms_shipped = 0 AND o.type IN ('P','FN','IF','TF') "
        "ORDER BY s.name, o.name, p.parameter_id",
    )

    dependencies = query(
        cursor,
        "SELECT OBJECT_SCHEMA_NAME(d.referencing_id) AS referencing_schema, "
        "OBJECT_NAME(d.referencing_id) AS referencing_object, "
        "d.referenced_server_name, d.referenced_database_name, d.referenced_schema_name, "
        "d.referenced_entity_name, d.referenced_minor_id, d.is_schema_bound_reference "
        "FROM sys.sql_expression_dependencies AS d "
        "JOIN sys.objects AS o ON o.object_id = d.referencing_id "
        "WHERE o.is_ms_shipped = 0 "
        "ORDER BY referencing_schema, referencing_object, referenced_schema_name, referenced_entity_name",
    )

    triggers = query(
        cursor,
        "SELECT s.name AS schema_name, t.name AS table_name, tr.name AS trigger_name, "
        "tr.is_disabled, tr.is_instead_of_trigger, tr.is_not_for_replication "
        "FROM sys.triggers AS tr "
        "JOIN sys.tables AS t ON t.object_id = tr.parent_id "
        "JOIN sys.schemas AS s ON s.schema_id = t.schema_id "
        "WHERE tr.parent_class = 1 ORDER BY s.name, t.name, tr.name",
    )

    CURRENT_STAGE = "read non-personal access-control reference data"
    acl_reference = {
        "roles": query(
            cursor,
            "SELECT r.id AS role_id, r.name AS role_name, r.SubSystemId AS subsystem_id, "
            "ss.[Name] AS subsystem_name, ss.[Status] AS subsystem_status "
            "FROM dbo.Roles AS r LEFT JOIN security.[SubSystem] AS ss ON ss.SubSystemId = r.SubSystemId "
            "ORDER BY r.id",
        ),
        "subsystems": query(
            cursor,
            "SELECT SubSystemId AS subsystem_id, [Name] AS subsystem_name, [Status] AS subsystem_status, ShowOrder "
            "FROM security.[SubSystem] ORDER BY ShowOrder, SubSystemId",
        ),
        "permissions": query(
            cursor,
            "SELECT PermissionId AS permission_id, PermissionName AS permission_name "
            "FROM security.[Permission] ORDER BY PermissionId",
        ),
        "forms": query(
            cursor,
            "SELECT FormId AS form_id, [Title] AS title, [NameSpace] AS namespace, [Class] AS class_name "
            "FROM security.[Form] ORDER BY FormId",
        ),
        "menus": query(
            cursor,
            "SELECT MenuID AS menu_id, SubSystemID AS subsystem_id, [Text] AS menu_text, "
            "[Description] AS menu_description, ParentMenuID AS parent_menu_id, FormID AS form_id, "
            "[order] AS display_order, Shortcut AS shortcut "
            "FROM security.Menu ORDER BY SubSystemID, [order], MenuID",
        ),
        "fields": query(
            cursor,
            "SELECT FieldId AS field_id, FormId AS form_id, [Title] AS title "
            "FROM security.[Field] ORDER BY FormId, FieldId",
        ),
        "role_form_permissions": query(
            cursor,
            "SELECT RoleId AS role_id, FormId AS form_id, PermissionId AS permission_id "
            "FROM security.RoleFormPermission ORDER BY RoleId, FormId, PermissionId",
        ),
        "role_field_permissions": query(
            cursor,
            "SELECT RoleId AS role_id, FieldId AS field_id, PermissionId AS permission_id "
            "FROM security.RoleFieldPermission ORDER BY RoleId, FieldId, PermissionId",
        ),
    }

    CURRENT_STAGE = "read access-control module source definitions without executing them"
    authorization_logic_sources = query(
        cursor,
        "SELECT s.name AS schema_name, o.name AS object_name, o.type_desc, m.definition "
        "FROM sys.sql_modules AS m "
        "JOIN sys.objects AS o ON o.object_id = m.object_id "
        "JOIN sys.schemas AS s ON s.schema_id = o.schema_id "
        "WHERE (s.name = N'EMS' AND o.name = N'GetUser') "
        "OR (s.name = N'dbo' AND o.name IN (N'get_role_id', N'ProcMenuPermission', N'ProcGroupPermission', "
        "N'vw_MenuInfo', N'Create_Login', N'ChangePassword')) "
        "OR (s.name = N'security' AND o.name IN (N'FormAndFieldPermissions', N'LoginDetailsTR')) "
        "ORDER BY s.name, o.name",
    )

    roles = query(
        cursor,
        "SELECT name AS role_name, type_desc, is_fixed_role, authentication_type_desc "
        "FROM sys.database_principals WHERE type = 'R' ORDER BY name",
    )

    role_permissions = query(
        cursor,
        "SELECT grantee.name AS role_name, p.class_desc, p.permission_name, p.state_desc, "
        "OBJECT_SCHEMA_NAME(p.major_id) AS object_schema, OBJECT_NAME(p.major_id) AS object_name "
        "FROM sys.database_permissions AS p "
        "JOIN sys.database_principals AS grantee ON grantee.principal_id = p.grantee_principal_id "
        "WHERE grantee.type = 'R' "
        "ORDER BY grantee.name, p.class_desc, object_schema, object_name, p.permission_name",
    )

    snapshot = {
        "inspection": {
            "scope": "sql_catalog_non_personal_acl_configuration_and_selected_server_code",
            "business_rows_read": False,
            "user_account_rows_read": False,
            "stored_password_hashes_read": False,
            "application_procedures_executed": False,
            "database_contents_modified_after_restore": False,
            "source_file": "14050603.zip",
        },
        "backup": backup_metadata,
        "backup_files": file_metadata,
        "server_database": server_rows,
        "objects": objects,
        "columns": columns,
        "indexes_and_keys": keys_and_indexes,
        "foreign_keys": foreign_keys,
        "constraints": constraints,
        "module_parameters": parameters,
        "dependencies": dependencies,
        "triggers": triggers,
        "database_roles": roles,
        "role_permissions": role_permissions,
        "acl_reference_data": acl_reference,
        "authorization_logic_sources": authorization_logic_sources,
    }
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2, default=json_value), encoding="utf-8")
    connection.close()

    print(
        "Metadata snapshot complete: objects=%d columns=%d indexes=%d foreign_keys=%d modules=%d; "
        "business_rows_read=0; result_file_written=1"
        % (len(objects), len(columns), len(keys_and_indexes), len(foreign_keys), len(parameters))
    )


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: atiran_finance_schema_snapshot.py OUTPUT.json", file=sys.stderr)
        return 2
    try:
        restore_and_collect(Path(sys.argv[1]))
        return 0
    except Exception as error:
        # This text contains only the stage and driver error (never query results). It is also
        # emitted as a GitHub annotation so failures remain diagnosable if raw logs are unavailable.
        detail = str(error).replace(os.environ.get("SA_PASS", "\u0000"), "[redacted]")
        detail = " ".join(detail.replace("\r", " ").replace("\n", " ").split())[:300]
        message = f"Stage: {CURRENT_STAGE}; error: {type(error).__name__}; detail: {detail or 'no driver detail'}"
        escaped = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A").replace("::", "%3A%3A")
        print("::error title=Schema and ACL snapshot::" + escaped)
        print("Schema snapshot failed: " + message, file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
