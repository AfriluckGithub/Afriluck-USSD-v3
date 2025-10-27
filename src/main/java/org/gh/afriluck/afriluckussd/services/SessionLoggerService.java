package org.gh.afriluck.afriluckussd.services;

import org.gh.afriluck.afriluckussd.datasource.CustomDataSourceConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionLoggerService {

    public void logSession(String msisdn, String network, String data,
                           String sequenceId, String message, LocalDateTime timestamp) {

        String sql = "INSERT INTO session_logs (id, msisdn, network, data, sequence_id, timestamp, message) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CustomDataSourceConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            UUID uuid = UUID.randomUUID();
            stmt.setString(1, uuid.toString());
            stmt.setString(2, msisdn);
            stmt.setString(3, network);
            stmt.setString(4, data);
            stmt.setString(5, sequenceId);
            stmt.setTimestamp(6, Timestamp.valueOf(timestamp));
            stmt.setString(7, message);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
