package com.odysseusinc.athena.service.saver.v5.history.delta;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Service
@Slf4j
public class CacheDeltaService {

    @Autowired
    @Qualifier("dataSourceAthenaV5History")
    protected DataSource v5HistoryDataSource;

    public boolean isDeltaVersionCached(int version1, int version2) {

        try (
                Connection conn = v5HistoryDataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT is_cached(?,?)")
        ) {
            stmt.setInt(1, version1);
            stmt.setInt(2, version2);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (Exception e) {
            log.error("Error checking cache status for versions: {} and {}", version1, version2, e);
        }
        return false;
    }
}
