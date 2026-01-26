package com.pms.propertymanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Order(0)
public class SqlServerUnicodeColumnsMigrator implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String product = metaData.getDatabaseProductName();
            if (product == null || !product.toLowerCase().contains("microsoft sql server")) {
                return;
            }

            ensureUnicode(connection, "contact_inquiries", "message", "NVARCHAR(MAX)");
            ensureUnicode(connection, "contact_inquiries", "preferred_contact_time", "NVARCHAR(255)");
            ensureUnicode(connection, "contracts", "history_note", "NVARCHAR(MAX)");
            ensureUnicode(connection, "contract_history", "note", "NVARCHAR(MAX)");
        }
    }

    private void ensureUnicode(Connection connection, String table, String column, String unicodeType) throws Exception {
        ColumnInfo info = getColumnInfo(connection, table, column);
        if (info == null) {
            return;
        }
        if (info.dataType == null) {
            return;
        }
        String t = info.dataType.toLowerCase();
        if (t.equals("nvarchar") || t.equals("nchar") || t.equals("ntext")) {
            return;
        }

        String nullClause = info.nullable ? "NULL" : "NOT NULL";
        String sql = "ALTER TABLE [" + table + "] ALTER COLUMN [" + column + "] " + unicodeType + " " + nullClause;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private ColumnInfo getColumnInfo(Connection connection, String table, String column) throws Exception {
        String sql = """
                SELECT DATA_TYPE, IS_NULLABLE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = ? AND COLUMN_NAME = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String dataType = rs.getString("DATA_TYPE");
                boolean nullable = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
                return new ColumnInfo(dataType, nullable);
            }
        }
    }

    private record ColumnInfo(String dataType, boolean nullable) {
    }
}

