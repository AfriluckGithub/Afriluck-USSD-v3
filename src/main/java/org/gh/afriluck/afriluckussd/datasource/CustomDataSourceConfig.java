package org.gh.afriluck.afriluckussd.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

public class CustomDataSourceConfig {

    private static HikariDataSource dataSource = null;

    static {
        HikariConfig config = new HikariConfig();
        try {
            config.setJdbcUrl("jdbc:postgresql://10.180.180.16:5432/afriluck_sessions");
            config.setUsername("root");
            config.setPassword("root");
            config.setDriverClassName("org.postgresql.Driver");

            // Optional: Connection pool tuning (adjust as needed)
            config.setMaximumPoolSize(50);
            config.setMinimumIdle(10);
            config.setIdleTimeout(180000);
            config.setMaxLifetime(90000);
            config.setKeepaliveTime(60000);
            config.setConnectionTimeout(30000);
            config.setLeakDetectionThreshold(10000);
            config.setConnectionTestQuery("SELECT 1");

            dataSource = new HikariDataSource(config);

            Runtime.getRuntime().addShutdownHook(new Thread(dataSource::close));

        }catch (Exception e) {
            System.err.println("Failed to initialize HikariDataSource: " + e.getMessage());
            e.printStackTrace(); // <== Make sure this is printing in your logs
            throw new RuntimeException("Failed to initialize HikariDataSource", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}

