
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class EmployeeDAO {

    // Lấy danh sách nhân viên
    public DefaultTableModel getAllEmployees() {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "ID", "Mã nhân viên", "Họ tên",
                "Vai trò", "Số điện thoại", "Trạng thái"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = """
            SELECT employee_id, employee_code, full_name,
                   role, phone, status
            FROM employees
            ORDER BY employee_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("employee_id"),
                    rs.getString("employee_code"),
                    rs.getString("full_name"),
                    rs.getString("role"),
                    rs.getString("phone"),
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // Thêm nhân viên
    public boolean addEmployee(
            String code,
            String name,
            String role,
            String phone,
            String email,
            String status) {

        String sql = """
            INSERT INTO employees
            (employee_code, full_name, role, phone, email, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, role);
            ps.setString(4, phone);
            ps.setString(5, email);
            ps.setString(6, status);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật nhân viên
    public boolean updateEmployee(
            int id,
            String name,
            String role,
            String phone,
            String email,
            String status) {

        String sql = """
            UPDATE employees
            SET full_name = ?, role = ?, phone = ?,
                email = ?, status = ?
            WHERE employee_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, role);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, status);
            ps.setInt(6, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa nhân viên
    public boolean deleteEmployee(int id) {

        String sql = "DELETE FROM employees WHERE employee_id = ?";

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
