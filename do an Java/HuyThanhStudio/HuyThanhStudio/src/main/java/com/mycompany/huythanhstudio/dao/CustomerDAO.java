
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class CustomerDAO {

    // Lấy danh sách khách hàng
    public DefaultTableModel getAllCustomers() {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "ID", "Mã khách", "Họ tên",
                "Số điện thoại", "Email", "Địa chỉ"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        String sql = """
            SELECT customer_id, customer_code, full_name,
                   phone, email, address
            FROM customers
            ORDER BY customer_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("customer_id"),
                    rs.getString("customer_code"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // Thêm khách hàng
    public boolean addCustomer(
            String code, String name, String phone,
            String email, String address, String note) {

        String sql = """
            INSERT INTO customers
            (customer_code, full_name, phone, email, address, note)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, address);
            ps.setString(6, note);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa khách hàng
    public boolean deleteCustomer(int customerId) {

        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
