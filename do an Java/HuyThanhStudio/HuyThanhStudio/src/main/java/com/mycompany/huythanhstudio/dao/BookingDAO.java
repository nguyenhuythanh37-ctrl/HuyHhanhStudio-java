
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.swing.table.DefaultTableModel;

public class BookingDAO {

    // Dữ liệu chi tiết của một lịch chụp
    public static class BookingInfo {
        public int id;
        public String bookingCode;
        public int customerId;
        public int serviceId;
        public Integer employeeId;
        public LocalDateTime scheduledAt;
        public String location;
        public String status;
        public String note;
    }

    // =========================
    // LẤY TOÀN BỘ LỊCH CHỤP
    // =========================

    public DefaultTableModel getAllBookings() {
        return getBookings(null, null, null);
    }

    // =========================
    // TÌM KIẾM VÀ LỌC LỊCH
    // =========================

    public DefaultTableModel getBookings(
            String keyword,
            LocalDate date,
            String status) {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "ID", "Mã lịch", "Khách hàng", "Dịch vụ",
                "Nhân viên", "Thời gian", "Địa điểm", "Trạng thái"
            }, 0
        );

        StringBuilder sql = new StringBuilder("""
            SELECT
                b.booking_id,
                b.booking_code,
                c.full_name,
                s.service_name,
                COALESCE(e.full_name, 'Chưa phân công'),
                b.scheduled_at,
                b.location,
                b.status
            FROM bookings b
            JOIN customers c ON b.customer_id = c.customer_id
            JOIN services s ON b.service_id = s.service_id
            LEFT JOIN employees e ON b.employee_id = e.employee_id
            WHERE 1 = 1
        """);

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("""
                AND (
                    LOWER(b.booking_code) LIKE ?
                    OR LOWER(c.full_name) LIKE ?
                )
            """);
        }

        if (date != null) {
            sql.append(" AND DATE(b.scheduled_at) = ?");
        }

        if (status != null && !status.equals("Tất cả trạng thái")) {
            sql.append(" AND b.status = ?");
        }

        sql.append(" ORDER BY b.scheduled_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String value = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(index++, value);
                ps.setString(index++, value);
            }

            if (date != null) {
                ps.setDate(index++, Date.valueOf(date));
            }

            if (status != null && !status.equals("Tất cả trạng thái")) {
                ps.setString(index++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6),
                        rs.getString(7),
                        rs.getString(8)
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // =========================
    // LẤY CHI TIẾT MỘT LỊCH
    // =========================

    public BookingInfo getBookingById(int id) {

        String sql = """
            SELECT booking_id, booking_code, customer_id,
                   service_id, employee_id, scheduled_at,
                   location, status, note
            FROM bookings
            WHERE booking_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookingInfo info = new BookingInfo();

                    info.id = rs.getInt("booking_id");
                    info.bookingCode = rs.getString("booking_code");
                    info.customerId = rs.getInt("customer_id");
                    info.serviceId = rs.getInt("service_id");

                    int employee = rs.getInt("employee_id");
                    info.employeeId = rs.wasNull() ? null : employee;

                    info.scheduledAt = rs.getTimestamp(
                        "scheduled_at"
                    ).toLocalDateTime();

                    info.location = rs.getString("location");
                    info.status = rs.getString("status");
                    info.note = rs.getString("note");

                    return info;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =========================
    // KIỂM TRA TRÙNG LỊCH
    // =========================

    public boolean hasEmployeeConflict(
            Integer employeeId,
            int serviceId,
            LocalDateTime scheduledAt,
            Integer excludeBookingId) {

        if (employeeId == null) {
            return false;
        }

        String sql = """
            SELECT COUNT(*)
            FROM bookings b
            JOIN services old_service
                ON b.service_id = old_service.service_id
            JOIN services new_service
                ON new_service.service_id = ?
            WHERE b.employee_id = ?
              AND b.status NOT IN ('Đã hủy', 'Đã hoàn thành')
              AND b.scheduled_at <
                  TIMESTAMPADD(
                      MINUTE,
                      GREATEST(new_service.duration_minutes, 1),
                      ?
                  )
              AND TIMESTAMPADD(
                      MINUTE,
                      GREATEST(old_service.duration_minutes, 1),
                      b.scheduled_at
                  ) > ?
              AND (? IS NULL OR b.booking_id <> ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, serviceId);
            ps.setInt(2, employeeId);
            ps.setTimestamp(3, Timestamp.valueOf(scheduledAt));
            ps.setTimestamp(4, Timestamp.valueOf(scheduledAt));

            if (excludeBookingId == null) {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(5, excludeBookingId);
                ps.setInt(6, excludeBookingId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                "Không thể kiểm tra lịch nhân viên.", e
            );
        }
    }

    // =========================
    // THÊM LỊCH CHỤP
    // =========================

    public boolean addBooking(
            String bookingCode,
            int customerId,
            int serviceId,
            Integer employeeId,
            LocalDateTime scheduledAt,
            String location,
            String status,
            String note) {

        if (employeeId != null
                && !status.equals("Đã hủy")
                && !status.equals("Đã hoàn thành")
                && hasEmployeeConflict(
                    employeeId, serviceId, scheduledAt, null
                )) {
            return false;
        }

        String sql = """
            INSERT INTO bookings (
                booking_code, customer_id, service_id,
                employee_id, scheduled_at, location,
                status, note
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bookingCode);
            ps.setInt(2, customerId);
            ps.setInt(3, serviceId);

            if (employeeId == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setInt(4, employeeId);
            }

            ps.setTimestamp(5, Timestamp.valueOf(scheduledAt));
            ps.setString(6, location);
            ps.setString(7, status);
            ps.setString(8, note);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // CẬP NHẬT TOÀN BỘ LỊCH
    // =========================

    public boolean updateBooking(
            int id,
            int customerId,
            int serviceId,
            Integer employeeId,
            LocalDateTime scheduledAt,
            String location,
            String status,
            String note) {

        if (employeeId != null
                && !status.equals("Đã hủy")
                && !status.equals("Đã hoàn thành")
                && hasEmployeeConflict(
                    employeeId, serviceId, scheduledAt, id
                )) {
            return false;
        }

        String sql = """
            UPDATE bookings
            SET customer_id = ?,
                service_id = ?,
                employee_id = ?,
                scheduled_at = ?,
                location = ?,
                status = ?,
                note = ?
            WHERE booking_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(2, serviceId);

            if (employeeId == null) {
                ps.setNull(3, Types.BIGINT);
            } else {
                ps.setInt(3, employeeId);
            }

            ps.setTimestamp(4, Timestamp.valueOf(scheduledAt));
            ps.setString(5, location);
            ps.setString(6, status);
            ps.setString(7, note);
            ps.setInt(8, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // CẬP NHẬT RIÊNG TRẠNG THÁI
    // =========================

    public boolean updateBookingStatus(int id, String status) {

        String sql = """
            UPDATE bookings
            SET status = ?
            WHERE booking_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // XÓA LỊCH
    // =========================

    public boolean deleteBooking(int id) {

        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // =========================
    // DANH SÁCH KHÁCH HÀNG
    // =========================

    public DefaultTableModel getCustomerOptions() {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Tên khách hàng"}, 0
        );

        String sql = """
            SELECT customer_id, full_name
            FROM customers
            ORDER BY full_name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // =========================
    // DANH SÁCH DỊCH VỤ
    // =========================

    public DefaultTableModel getServiceOptions() {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Tên dịch vụ"}, 0
        );

        String sql = """
            SELECT service_id, service_name
            FROM services
            WHERE is_active = TRUE
            ORDER BY service_name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // =========================
    // DANH SÁCH NHÂN VIÊN
    // =========================

    public DefaultTableModel getEmployeeOptions() {

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Tên nhân viên"}, 0
        );

        String sql = """
            SELECT employee_id, full_name
            FROM employees
            WHERE status = 'Đang làm'
            ORDER BY full_name
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt(1),
                    rs.getString(2)
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }
}
