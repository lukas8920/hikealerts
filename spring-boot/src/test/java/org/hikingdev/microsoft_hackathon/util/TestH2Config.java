package org.hikingdev.microsoft_hackathon.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@TestConfiguration
public class TestH2Config {
    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE ALIAS Levenshtein FOR \"org.hikingdev.microsoft_hackathon.util.Levenshtein.levenshtein\";");  // Your function DDL here
        }
    }
}
