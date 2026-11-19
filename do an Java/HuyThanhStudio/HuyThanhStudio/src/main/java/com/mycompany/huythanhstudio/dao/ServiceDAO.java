
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class ServiceDAO {

    // Lấy danh sách dịch vụ
    public DefaultTableModel getAllServices() {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "ID", "Mã dịch vụ", "Tên dịch vụ",
                "Giá", "Thời lượng", "Mô tả", "Trạng thái"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = """
            SELECT service_id, service_code, service_name,
                   price, duration_minutes, description, is_active
            FROM services
            ORDER BY service_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("service_id"),
                    rs.getString("service_code"),
                    rs.getString("service_name"),
                    rs.getBigDecimal("price"),
                    rs.getInt("duration_minutes"),
                    rs.getString("description"),
                    rs.getBoolean("is_active") ? "Đang hoạt động" : "Ngừng hoạt động"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // Thêm dịch vụ
    public boolean addService(
            String code,
            String name,
            double price,
            int duration,
            String description) {

        String sql = """
            INSERT INTO services
            (service_code, service_name, price,
             duration_minutes, description, is_active)
            VALUES (?, ?, ?, ?, ?, TRUE)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setDouble(3, price);
            ps.setInt(4, duration);
            ps.setString(5, description);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật dịch vụ
    public boolean updateService(
            int id,
            String name,
            double price,
            int duration,
            String description,
            boolean active) {

        String sql = """
            UPDATE services
            SET service_name = ?, price = ?,
                duration_minutes = ?, description = ?,
                is_active = ?
            WHERE service_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, duration);
            ps.setString(4, description);
            ps.setBoolean(5, active);
            ps.setInt(6, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa dịch vụ
    public boolean deleteService(int id) {

        String sql = "DELETE FROM services WHERE service_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
