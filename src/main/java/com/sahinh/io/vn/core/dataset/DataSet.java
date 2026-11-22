package com.sahinh.io.vn.core.dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * DataSet — JDBC thuần. API giữ đúng tên gọi của DriveMaster
 * (searchAndRetrieve / update) để agent quen tay.
 * Không dùng JdbcTemplate / JPA / Repository.
 */
public class DataSet {
    private final DataSource dataSource;

    public DataSet(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void bind(PreparedStatement ps, Object... params) throws Exception {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /** SELECT -> List<Map>, key là tên cột VIẾT HOA. */
    public List<Map<String, Object>> searchAndRetrieve(String sql, Object... params) {
        List<Map<String, Object>> out = new ArrayList<>();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        row.put(md.getColumnLabel(i).toUpperCase(), rs.getObject(i));
                    }
                    out.add(row);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("SQL failed: " + sql + " :: " + e.getMessage(), e);
        }
        return out;
    }

    public Map<String, Object> findOne(String sql, Object... params) {
        List<Map<String, Object>> rows = searchAndRetrieve(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int count(String sql, Object... params) {
        Map<String, Object> row = findOne(sql, params);
        if (row == null || row.isEmpty()) {
            return 0;
        }
        Object v = row.values().iterator().next();
        return v == null ? 0 : ((Number) v).intValue();
    }

    /** INSERT / UPDATE / DELETE -> số dòng ảnh hưởng. */
    public int update(String sql, Object... params) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("SQL failed: " + sql + " :: " + e.getMessage(), e);
        }
    }

    /** INSERT -> id tự tăng. */
    public long insertAndGetId(String sql, Object... params) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return -1;
        } catch (Exception e) {
            throw new RuntimeException("SQL failed: " + sql + " :: " + e.getMessage(), e);
        }
    }
}
