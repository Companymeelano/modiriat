/*
 * Atiran Finance - READ-ONLY schema/permission inventory for SQL Server.
 * Run in SSMS/sqlcmd against the authorized TEST database.
 * This script reads catalog metadata only: no business rows, credentials, INSERT, UPDATE,
 * DELETE, DDL, or stored-procedure bodies are selected or executed.
 * Review/redact principal names before sharing the result set outside the database team.
 */
SET NOCOUNT ON;

-- Result set 1: database and server version.
SELECT
    DB_NAME() AS database_name,
    CONVERT(nvarchar(128), SERVERPROPERTY('ProductVersion')) AS server_product_version,
    CONVERT(nvarchar(128), SERVERPROPERTY('ProductLevel')) AS server_product_level,
    CONVERT(nvarchar(128), SERVERPROPERTY('Edition')) AS server_edition;

-- Result set 2: all non-system user tables/views and their exact column metadata.
-- PK ordinal is reported only when SQL Server declares a Primary Key.
SELECT
    s.name AS schema_name,
    o.name AS object_name,
    o.type_desc AS object_type,
    c.column_id,
    c.name AS column_name,
    TYPE_NAME(c.user_type_id) AS declared_type,
    TYPE_NAME(c.system_type_id) AS base_system_type,
    c.max_length AS max_length_bytes,
    c.precision AS numeric_precision,
    c.scale AS numeric_scale,
    c.is_nullable,
    c.is_identity,
    c.is_computed,
    c.collation_name,
    ISNULL(pk.key_ordinal, 0) AS primary_key_ordinal
FROM sys.objects AS o
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
LEFT JOIN sys.columns AS c ON c.object_id = o.object_id
LEFT JOIN (
    SELECT ic.object_id, ic.column_id, ic.key_ordinal
    FROM sys.indexes AS i
    JOIN sys.index_columns AS ic
      ON ic.object_id = i.object_id AND ic.index_id = i.index_id
    WHERE i.is_primary_key = 1 AND ic.key_ordinal > 0
) AS pk ON pk.object_id = o.object_id AND pk.column_id = c.column_id
WHERE o.is_ms_shipped = 0 AND o.type IN ('U', 'V')
ORDER BY s.name, o.name, c.column_id;

-- Result set 3: declared foreign keys and their column pairs.
SELECT
    fk.name AS foreign_key_name,
    ps.name AS parent_schema,
    pt.name AS parent_table,
    pc.name AS parent_column,
    rs.name AS referenced_schema,
    rt.name AS referenced_table,
    rc.name AS referenced_column,
    fkc.constraint_column_id AS key_column_ordinal,
    fk.is_disabled,
    fk.is_not_trusted,
    fk.delete_referential_action_desc,
    fk.update_referential_action_desc
FROM sys.foreign_keys AS fk
JOIN sys.foreign_key_columns AS fkc ON fkc.constraint_object_id = fk.object_id
JOIN sys.tables AS pt ON pt.object_id = fk.parent_object_id
JOIN sys.schemas AS ps ON ps.schema_id = pt.schema_id
JOIN sys.columns AS pc ON pc.object_id = pt.object_id AND pc.column_id = fkc.parent_column_id
JOIN sys.tables AS rt ON rt.object_id = fk.referenced_object_id
JOIN sys.schemas AS rs ON rs.schema_id = rt.schema_id
JOIN sys.columns AS rc ON rc.object_id = rt.object_id AND rc.column_id = fkc.referenced_column_id
WHERE pt.is_ms_shipped = 0 AND rt.is_ms_shipped = 0
ORDER BY ps.name, pt.name, fk.name, fkc.constraint_column_id;

-- Result set 4: indexes/unique constraints, column order and included-column metadata.
SELECT
    s.name AS schema_name,
    t.name AS table_name,
    i.name AS index_name,
    i.type_desc AS index_type,
    i.is_primary_key,
    i.is_unique,
    i.is_disabled,
    ic.key_ordinal,
    ic.is_included_column,
    ic.is_descending_key,
    c.name AS column_name
FROM sys.tables AS t
JOIN sys.schemas AS s ON s.schema_id = t.schema_id
JOIN sys.indexes AS i ON i.object_id = t.object_id
JOIN sys.index_columns AS ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
JOIN sys.columns AS c ON c.object_id = t.object_id AND c.column_id = ic.column_id
WHERE t.is_ms_shipped = 0 AND i.index_id > 0 AND i.is_hypothetical = 0
ORDER BY s.name, t.name, i.name, ic.key_ordinal, ic.index_column_id;

-- Result set 5: CHECK constraints can document allowed values, but do not prove business meaning.
SELECT
    s.name AS schema_name,
    t.name AS table_name,
    cc.name AS check_constraint_name,
    cc.is_disabled,
    cc.is_not_trusted,
    cc.definition AS check_definition
FROM sys.check_constraints AS cc
JOIN sys.tables AS t ON t.object_id = cc.parent_object_id
JOIN sys.schemas AS s ON s.schema_id = t.schema_id
WHERE t.is_ms_shipped = 0
ORDER BY s.name, t.name, cc.name;

-- Result set 6: stored procedure/function names and parameter signatures only; bodies are not read/run.
SELECT
    s.name AS schema_name,
    o.name AS routine_name,
    o.type_desc AS routine_type,
    p.parameter_id,
    p.name AS parameter_name,
    TYPE_NAME(p.user_type_id) AS parameter_type,
    p.max_length AS parameter_max_length_bytes,
    p.precision AS parameter_precision,
    p.scale AS parameter_scale,
    p.is_output
FROM sys.objects AS o
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
LEFT JOIN sys.parameters AS p ON p.object_id = o.object_id
WHERE o.is_ms_shipped = 0 AND o.type IN ('P', 'PC', 'FN', 'IF', 'TF', 'FS', 'FT')
ORDER BY s.name, o.name, p.parameter_id;

-- Result set 7: database-role membership and explicit object/database permissions.
-- Principal names may be sensitive; redact them before sharing if required.
SELECT
    member_principal.name AS member_name,
    member_principal.type_desc AS member_type,
    role_principal.name AS database_role
FROM sys.database_role_members AS drm
JOIN sys.database_principals AS role_principal
  ON role_principal.principal_id = drm.role_principal_id
JOIN sys.database_principals AS member_principal
  ON member_principal.principal_id = drm.member_principal_id
ORDER BY role_principal.name, member_principal.name;

SELECT
    grantee.name AS grantee_name,
    grantee.type_desc AS grantee_type,
    dp.state_desc,
    dp.permission_name,
    dp.class_desc,
    CASE WHEN dp.class = 1 THEN OBJECT_SCHEMA_NAME(dp.major_id) END AS object_schema,
    CASE WHEN dp.class = 1 THEN OBJECT_NAME(dp.major_id) END AS object_name,
    CASE WHEN dp.class = 1 THEN COL_NAME(dp.major_id, dp.minor_id) END AS column_name
FROM sys.database_permissions AS dp
JOIN sys.database_principals AS grantee
  ON grantee.principal_id = dp.grantee_principal_id
ORDER BY grantee.name, dp.class_desc, dp.permission_name;
