package com.pms.propertymanagement.config.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(0)
public class MaintenanceSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureMaintenanceRequestsCodePopulated();
        ensureMaintenanceRequestsHasTenantId();
        ensureMaintenanceRequestsRequesterIdDoesNotBlockInserts();
        ensureMaintenanceRequestsDropPriority();
    }

    private void ensureMaintenanceRequestsHasTenantId() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'maintenance_requests'",
                Integer.class
        );
        if (tableCount == null || tableCount == 0) {
            return;
        }

        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'maintenance_requests' AND COLUMN_NAME = 'tenant_id'",
                Integer.class
        );
        if (columnCount != null && columnCount > 0) {
            return;
        }

        System.out.println("Đang cập nhật schema: thêm cột tenant_id vào bảng maintenance_requests");
        jdbcTemplate.execute("ALTER TABLE maintenance_requests ADD tenant_id BIGINT NULL");

        Integer fkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys.foreign_keys WHERE name = 'FK_maintenance_requests_tenant'",
                Integer.class
        );
        if (fkCount != null && fkCount == 0) {
            System.out.println("Đang cập nhật schema: thêm FK FK_maintenance_requests_tenant");
            jdbcTemplate.execute(
                    "ALTER TABLE maintenance_requests WITH NOCHECK " +
                            "ADD CONSTRAINT FK_maintenance_requests_tenant " +
                            "FOREIGN KEY (tenant_id) REFERENCES users(id)"
            );
        }
    }

    private void ensureMaintenanceRequestsCodePopulated() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'maintenance_requests'",
                Integer.class
        );
        if (tableCount == null || tableCount == 0) {
            return;
        }

        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'maintenance_requests' AND COLUMN_NAME = 'code'",
                Integer.class
        );
        if (columnCount == null || columnCount == 0) {
            return;
        }

        jdbcTemplate.execute(
                "UPDATE maintenance_requests " +
                        "SET code = CONCAT('MR-', FORMAT(COALESCE(created_at, GETDATE()), 'yyyyMMddHHmmss'), '-', id) " +
                        "WHERE code IS NULL OR LTRIM(RTRIM(code)) = ''"
        );
    }

    private void ensureMaintenanceRequestsDropPriority() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'maintenance_requests'",
                Integer.class
        );
        if (tableCount == null || tableCount == 0) {
            return;
        }

        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'maintenance_requests' AND COLUMN_NAME = 'priority'",
                Integer.class
        );
        if (columnCount == null || columnCount == 0) {
            return;
        }

        System.out.println("Đang cập nhật schema: xóa cột priority khỏi bảng maintenance_requests");

        jdbcTemplate.queryForList(
                "SELECT dc.name " +
                        "FROM sys.default_constraints dc " +
                        "JOIN sys.columns c ON c.default_object_id = dc.object_id " +
                        "JOIN sys.tables t ON t.object_id = c.object_id " +
                        "WHERE t.name = 'maintenance_requests' AND c.name = 'priority'",
                String.class
        ).forEach(name -> jdbcTemplate.execute("ALTER TABLE maintenance_requests DROP CONSTRAINT [" + name + "]"));

        jdbcTemplate.queryForList(
                "SELECT cc.name " +
                        "FROM sys.check_constraints cc " +
                        "JOIN sys.tables t ON t.object_id = cc.parent_object_id " +
                        "WHERE t.name = 'maintenance_requests' AND cc.definition LIKE '%priority%'",
                String.class
        ).forEach(name -> jdbcTemplate.execute("ALTER TABLE maintenance_requests DROP CONSTRAINT [" + name + "]"));

        jdbcTemplate.queryForList(
                "SELECT i.name " +
                        "FROM sys.indexes i " +
                        "JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id " +
                        "JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id " +
                        "JOIN sys.tables t ON t.object_id = i.object_id " +
                        "WHERE t.name = 'maintenance_requests' AND c.name = 'priority' " +
                        "AND i.is_primary_key = 0 AND i.is_unique_constraint = 0 AND i.name IS NOT NULL",
                String.class
        ).forEach(name -> jdbcTemplate.execute("DROP INDEX [" + name + "] ON maintenance_requests"));

        jdbcTemplate.execute("ALTER TABLE maintenance_requests DROP COLUMN priority");
    }

    private void ensureMaintenanceRequestsRequesterIdDoesNotBlockInserts() {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'maintenance_requests'",
                Integer.class
        );
        if (tableCount == null || tableCount == 0) {
            return;
        }

        Integer requesterColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'maintenance_requests' AND COLUMN_NAME = 'requester_id'",
                Integer.class
        );
        if (requesterColumnCount == null || requesterColumnCount == 0) {
            return;
        }

        String requesterNullable = jdbcTemplate.queryForObject(
                "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'maintenance_requests' AND COLUMN_NAME = 'requester_id'",
                String.class
        );
        if ("NO".equalsIgnoreCase(requesterNullable)) {
            System.out.println("Đang cập nhật schema: cho phép NULL cho cột requester_id trong bảng maintenance_requests");
            jdbcTemplate.execute("ALTER TABLE maintenance_requests ALTER COLUMN requester_id BIGINT NULL");
        }

        Integer tenantColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'maintenance_requests' AND COLUMN_NAME = 'tenant_id'",
                Integer.class
        );
        if (tenantColumnCount != null && tenantColumnCount > 0) {
            jdbcTemplate.execute(
                    "UPDATE maintenance_requests SET tenant_id = requester_id " +
                            "WHERE tenant_id IS NULL AND requester_id IS NOT NULL"
            );
        }
    }
}
