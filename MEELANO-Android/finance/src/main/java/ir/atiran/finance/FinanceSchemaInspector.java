package ir.atiran.finance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only SQL Server catalog inspection. This class never selects rows from business tables. */
public final class FinanceSchemaInspector {
    private static final String OBJECT_COLUMNS_SQL =
            "SELECT s.name AS schema_name, o.name AS object_name, " +
            "CASE WHEN o.type='U' THEN N'TABLE' ELSE N'VIEW' END AS object_kind, " +
            "c.column_id, c.name AS column_name, TYPE_NAME(c.user_type_id) AS type_name, " +
            "c.max_length, c.precision, c.scale, c.is_nullable, c.is_identity, c.is_computed, " +
            "ISNULL(pk.key_ordinal,0) AS primary_key_ordinal " +
            "FROM sys.objects o " +
            "JOIN sys.schemas s ON s.schema_id=o.schema_id " +
            "LEFT JOIN sys.columns c ON c.object_id=o.object_id " +
            "LEFT JOIN (SELECT ic.object_id, ic.column_id, ic.key_ordinal " +
            "FROM sys.indexes i JOIN sys.index_columns ic " +
            "ON ic.object_id=i.object_id AND ic.index_id=i.index_id " +
            "WHERE i.is_primary_key=1 AND ic.key_ordinal>0) pk " +
            "ON pk.object_id=o.object_id AND pk.column_id=c.column_id " +
            "WHERE o.is_ms_shipped=0 AND o.type IN ('U','V') " +
            "ORDER BY s.name, o.name, c.column_id";

    private static final String FOREIGN_KEYS_SQL =
            "SELECT fk.name, ps.name, pt.name, pc.name, rs.name, rt.name, rc.name, fkc.constraint_column_id " +
            "FROM sys.foreign_keys fk " +
            "JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id=fk.object_id " +
            "JOIN sys.tables pt ON pt.object_id=fk.parent_object_id " +
            "JOIN sys.schemas ps ON ps.schema_id=pt.schema_id " +
            "JOIN sys.columns pc ON pc.object_id=pt.object_id AND pc.column_id=fkc.parent_column_id " +
            "JOIN sys.tables rt ON rt.object_id=fk.referenced_object_id " +
            "JOIN sys.schemas rs ON rs.schema_id=rt.schema_id " +
            "JOIN sys.columns rc ON rc.object_id=rt.object_id AND rc.column_id=fkc.referenced_column_id " +
            "WHERE pt.is_ms_shipped=0 AND rt.is_ms_shipped=0 " +
            "ORDER BY ps.name, pt.name, fk.name, fkc.constraint_column_id";

    private static final String INDEX_COLUMNS_SQL =
            "SELECT s.name, t.name, i.name, i.is_unique, i.is_primary_key, " +
            "ic.key_ordinal, ic.is_included_column, c.name " +
            "FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id " +
            "JOIN sys.indexes i ON i.object_id=t.object_id " +
            "JOIN sys.index_columns ic ON ic.object_id=i.object_id AND ic.index_id=i.index_id " +
            "JOIN sys.columns c ON c.object_id=t.object_id AND c.column_id=ic.column_id " +
            "WHERE t.is_ms_shipped=0 AND i.index_id>0 AND i.is_hypothetical=0 " +
            "ORDER BY s.name, t.name, i.name, ic.key_ordinal, ic.index_column_id";

    private FinanceSchemaInspector() { }

    public static Snapshot inspect(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("No open database connection");
        }
        try {
            connection.setReadOnly(true);
        } catch (SQLException ignored) {
            // Some SQL Server JDBC drivers do not implement setReadOnly. All statements below
            // are fixed catalog SELECTs; no business-table data or mutations are issued.
        }

        Snapshot snapshot = new Snapshot();
        snapshot.databaseName = connection.getCatalog() == null ? "" : connection.getCatalog();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT DB_NAME(), CONVERT(nvarchar(128), SERVERPROPERTY('ProductVersion'))")) {
            if (result.next()) {
                String database = result.getString(1);
                String version = result.getString(2);
                if (database != null) snapshot.databaseName = database;
                snapshot.serverProductVersion = version == null ? "" : version;
            }
        }

        Map<String, DbObject> objects = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(OBJECT_COLUMNS_SQL)) {
            while (result.next()) {
                String schema = value(result.getString(1));
                String name = value(result.getString(2));
                String kind = value(result.getString(3));
                String key = schema + "\u0000" + name;
                DbObject object = objects.get(key);
                if (object == null) {
                    object = new DbObject(schema, name, kind);
                    objects.put(key, object);
                }
                int columnId = result.getInt(4);
                if (result.wasNull()) continue;
                Column column = new Column();
                column.ordinal = columnId;
                column.name = value(result.getString(5));
                column.sqlType = value(result.getString(6));
                column.maxLengthBytes = result.getInt(7);
                column.precision = result.getInt(8);
                column.scale = result.getInt(9);
                column.nullable = result.getBoolean(10);
                column.identity = result.getBoolean(11);
                column.computed = result.getBoolean(12);
                column.primaryKeyOrdinal = result.getInt(13);
                object.columns.add(column);
            }
        }
        snapshot.objects.addAll(objects.values());

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(FOREIGN_KEYS_SQL)) {
            while (result.next()) {
                ForeignKeyColumn fk = new ForeignKeyColumn();
                fk.name = value(result.getString(1));
                fk.parentSchema = value(result.getString(2));
                fk.parentTable = value(result.getString(3));
                fk.parentColumn = value(result.getString(4));
                fk.referencedSchema = value(result.getString(5));
                fk.referencedTable = value(result.getString(6));
                fk.referencedColumn = value(result.getString(7));
                fk.ordinal = result.getInt(8);
                snapshot.foreignKeyColumns.add(fk);
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(INDEX_COLUMNS_SQL)) {
            while (result.next()) {
                IndexColumn index = new IndexColumn();
                index.schema = value(result.getString(1));
                index.table = value(result.getString(2));
                index.name = value(result.getString(3));
                index.unique = result.getBoolean(4);
                index.primary = result.getBoolean(5);
                index.keyOrdinal = result.getInt(6);
                index.included = result.getBoolean(7);
                index.column = value(result.getString(8));
                snapshot.indexColumns.add(index);
            }
        }
        return snapshot;
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    public static final class Snapshot {
        public String databaseName = "";
        public String serverProductVersion = "";
        public final List<DbObject> objects = new ArrayList<>();
        public final List<ForeignKeyColumn> foreignKeyColumns = new ArrayList<>();
        public final List<IndexColumn> indexColumns = new ArrayList<>();

        public int tableCount() {
            int count = 0;
            for (DbObject object : objects) if ("TABLE".equals(object.kind)) count++;
            return count;
        }

        public int viewCount() {
            int count = 0;
            for (DbObject object : objects) if ("VIEW".equals(object.kind)) count++;
            return count;
        }

        public int columnCount() {
            int count = 0;
            for (DbObject object : objects) count += object.columns.size();
            return count;
        }
    }

    public static final class DbObject {
        public final String schema;
        public final String name;
        public final String kind;
        public final List<Column> columns = new ArrayList<>();

        DbObject(String schema, String name, String kind) {
            this.schema = schema;
            this.name = name;
            this.kind = kind;
        }
    }

    public static final class Column {
        public int ordinal;
        public String name = "";
        public String sqlType = "";
        public int maxLengthBytes;
        public int precision;
        public int scale;
        public boolean nullable;
        public boolean identity;
        public boolean computed;
        public int primaryKeyOrdinal;
    }

    public static final class ForeignKeyColumn {
        public String name = "";
        public String parentSchema = "";
        public String parentTable = "";
        public String parentColumn = "";
        public String referencedSchema = "";
        public String referencedTable = "";
        public String referencedColumn = "";
        public int ordinal;
    }

    public static final class IndexColumn {
        public String schema = "";
        public String table = "";
        public String name = "";
        public String column = "";
        public boolean unique;
        public boolean primary;
        public boolean included;
        public int keyOrdinal;
    }
}
