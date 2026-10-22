package com.tracker.hrtracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.datasource.url=${DATABASE_URL:jdbc:postgresql://ep-nameless-bird-b3wghibc-pooler.c-4.ap-southeast-1.aws.neon.tech/neondb?sslmode=require}",
    "spring.datasource.username=${DATABASE_USERNAME:neondb_owner}",
    "spring.datasource.password=${DATABASE_PASSWORD:npg_OP7ZTr2QvalA}",
    "spring.datasource.driverClassName=org.postgresql.Driver",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
})
@ActiveProfiles("prod")
public class NeonConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verify Neon Cloud PostgreSQL connection and table creation")
    void testNeonConnection() throws Exception {
        assertNotNull(dataSource);

        try (Connection conn = dataSource.getConnection()) {
            assertNotNull(conn);
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("✅ CONNECTED TO NEON: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());

            // Check created tables in PostgreSQL
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }

            System.out.println("✅ TABLES CREATED IN NEON: " + tables);
            assertTrue(tables.contains("users"), "users table should exist in Neon");
            assertTrue(tables.contains("contacts"), "contacts table should exist in Neon");
            assertTrue(tables.contains("call_logs"), "call_logs table should exist in Neon");
            assertTrue(tables.contains("applications"), "applications table should exist in Neon");
        }
    }
}
