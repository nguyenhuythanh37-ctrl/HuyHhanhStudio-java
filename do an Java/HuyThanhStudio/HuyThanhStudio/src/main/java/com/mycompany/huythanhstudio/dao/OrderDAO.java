
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class OrderDAO {

    // ==========================================
    // LỚP LƯU THÔNG TIN LỊCH CHỤP ĐỂ TẠO ĐƠN
    // ==========================================

    public static class BookingOption {

        public final long id;
        public final String bookingCode;
        public final String customerName;
        public final String serviceName;
        public final Timestamp scheduledAt;
        public final BigDecimal price;

        public BookingOption(
                long id,
                String bookingCode,
                String customerName,
                String serviceName,
                Timestamp scheduledAt,
                BigDecimal price) {

            this.id = id;
            this.bookingCode = bookingCode;
            this.customerName = customerName;
            this.serviceName = serviceName;
            this.scheduledAt = scheduledAt;
            this.price = price;
        }

        @Override
        public String toString() {
            return bookingCode + " | " + customerName
                    + " | " + serviceName
                    + " | " + price.toPlainString() + " đ";
        }
    }

    // ==========================================
    // LẤY DANH SÁCH ĐƠN CHỤP
    // ==========================================

    public DefaultTableModel getAllOrders() {

        String[] columns = {
            "ID",
            "Mã đơn",
            "Mã lịch",
            "Khách hàng",
            "Dịch vụ",
            "Ngày chụp",
            "Ngày tạo đơn",
            "Tổng giá",
            "Giảm giá",
            "Thành tiền",
            "Trạng thái",
            "Ghi chú"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = """
            SELECT
                o.order_id,
                o.order_code,
                COALESCE(b.booking_code, ''),
                c.full_name,
                COALESCE(s.service_name, ''),
                b.scheduled_at,
                o.order_date,
                o.subtotal,
                o.discount,
                o.total_amount,
                o.status,
                o.note
            FROM orders o
            JOIN customers c
                ON o.customer_id = c.customer_id
            LEFT JOIN bookings b
                ON o.booking_id = b.booking_id
            LEFT JOIN services s
                ON b.service_id = s.service_id
            ORDER BY o.order_date DESC, o.order_id DESC
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getLong("order_id"),
                    rs.getString("order_code"),
                    rs.getString(3),
                    rs.getString("full_name"),
                    rs.getString(5),
                    rs.getTimestamp("scheduled_at"),
                    rs.getTimestamp("order_date"),
                    rs.getBigDecimal("subtotal"),
                    rs.getBigDecimal("discount"),
                    rs.getBigDecimal("total_amount"),
                    rs.getString("status"),
                    rs.getString("note")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return model;
    }

    // ==========================================
    // LẤY DANH SÁCH LỊCH CHƯA TẠO ĐƠN
    // ==========================================

    public List<BookingOption> getBookingOptions() throws SQLException {

        List<BookingOption> list = new ArrayList<>();

        String sql = """
            SELECT
                b.booking_id,
                b.booking_code,
                c.full_name,
                s.service_name,
                b.scheduled_at,
                s.price
            FROM bookings b
            JOIN customers c
                ON b.customer_id = c.customer_id
            JOIN services s
                ON b.service_id = s.service_id
            WHERE b.status <> 'Đã hủy'
              AND NOT EXISTS (
                  SELECT 1
                  FROM orders o
                  WHERE o.booking_id = b.booking_id
              )
            ORDER BY b.scheduled_at DESC
        """;

        try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                list.add(new BookingOption(
                    rs.getLong("booking_id"),
                    rs.getString("booking_code"),
                    rs.getString("full_name"),
                    rs.getString("service_name"),
                    rs.getTimestamp("scheduled_at"),
                    rs.getBigDecimal("price")
                ));
            }
        }

        return list;
    }

    // ==========================================
    // THÊM ĐƠN CHỤP TỪ LỊCH CHỤP
    // ==========================================

    public boolean addOrder(
            long bookingId,
            BigDecimal discount,
            String status,
            String note) throws SQLException {

        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Giảm giá không được là số âm."
            );
        }

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException(
                "Trạng thái đơn chụp không hợp lệ."
            );
        }

        String bookingSql = """
            SELECT
                b.customer_id,
                b.status,
                s.price
            FROM bookings b
            JOIN services s
                ON b.service_id = s.service_id
            WHERE b.booking_id = ?
            FOR UPDATE
        """;

        String duplicateSql = """
            SELECT COUNT(*)
            FROM orders
            WHERE booking_id = ?
        """;

        String insertSql = """
            INSERT INTO orders (
                order_code,
                customer_id,
                booking_id,
                subtotal,
                discount,
                total_amount,
                status,
                note
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                long customerId;
                BigDecimal subtotal;
                String bookingStatus;

                // Lấy thông tin lịch và giá dịch vụ
                try (PreparedStatement ps =
                        conn.prepareStatement(bookingSql)) {

                    ps.setLong(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                "Không tìm thấy lịch chụp."
                            );
                        }

                        customerId = rs.getLong("customer_id");
                        bookingStatus = rs.getString("status");
                        subtotal = rs.getBigDecimal("price");
                    }
                }

                if ("Đã hủy".equals(bookingStatus)) {
                    throw new SQLException(
                        "Không thể tạo đơn từ lịch chụp đã hủy."
                    );
                }

                if (subtotal == null
                        || subtotal.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException(
                        "Giá dịch vụ không hợp lệ."
                    );
                }

                // Kiểm tra lịch đã có đơn hay chưa
                try (PreparedStatement ps =
                        conn.prepareStatement(duplicateSql)) {

                    ps.setLong(1, bookingId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException(
                                "Lịch chụp này đã có đơn hàng."
                            );
                        }
                    }
                }

                if (discount.compareTo(subtotal) > 0) {
                    throw new IllegalArgumentException(
                        "Giảm giá không được lớn hơn giá trị đơn."
                    );
                }

                BigDecimal total = subtotal.subtract(discount);

                String orderCode =
                        "DH" + System.currentTimeMillis();

                try (PreparedStatement ps =
                        conn.prepareStatement(insertSql)) {

                    ps.setString(1, orderCode);
                    ps.setLong(2, customerId);
                    ps.setLong(3, bookingId);
                    ps.setBigDecimal(4, subtotal);
                    ps.setBigDecimal(5, discount);
                    ps.setBigDecimal(6, total);
                    ps.setString(7, status);
                    ps.setString(8, note);

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (e instanceof IllegalArgumentException illegalException) {
                    throw illegalException;
                }

                throw new SQLException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // CẬP NHẬT ĐƠN CHỤP
    // ==========================================

    public boolean updateOrder(
            long orderId,
            BigDecimal discount,
            String status,
            String note) throws SQLException {

        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "Giảm giá không được là số âm."
            );
        }

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException(
                "Trạng thái đơn chụp không hợp lệ."
            );
        }

        String selectSql = """
            SELECT subtotal
            FROM orders
            WHERE order_id = ?
            FOR UPDATE
        """;

        String paidSql = """
            SELECT COALESCE(SUM(amount), 0)
            FROM payments
            WHERE order_id = ?
              AND status = 'Đã thanh toán'
        """;

        String updateSql = """
            UPDATE orders
            SET discount = ?,
                total_amount = ?,
                status = ?,
                note = ?
            WHERE order_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                BigDecimal subtotal;

                try (PreparedStatement ps =
                        conn.prepareStatement(selectSql)) {

                    ps.setLong(1, orderId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                "Không tìm thấy đơn chụp."
                            );
                        }

                        subtotal = rs.getBigDecimal("subtotal");
                    }
                }

                if (discount.compareTo(subtotal) > 0) {
                    throw new IllegalArgumentException(
                        "Giảm giá không được lớn hơn giá trị đơn."
                    );
                }

                BigDecimal total = subtotal.subtract(discount);
                BigDecimal paid = BigDecimal.ZERO;

                try (PreparedStatement ps =
                        conn.prepareStatement(paidSql)) {

                    ps.setLong(1, orderId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            paid = rs.getBigDecimal(1);
                        }
                    }
                }

                if (paid.compareTo(total) > 0) {
                    throw new SQLException(
                        "Không thể giảm thành tiền xuống thấp hơn "
                        + "số tiền khách hàng đã thanh toán."
                    );
                }

                try (PreparedStatement ps =
                        conn.prepareStatement(updateSql)) {

                    ps.setBigDecimal(1, discount);
                    ps.setBigDecimal(2, total);
                    ps.setString(3, status);
                    ps.setString(4, note);
                    ps.setLong(5, orderId);

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (e instanceof IllegalArgumentException illegalException) {
                    throw illegalException;
                }

                throw new SQLException(e);

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // XÓA ĐƠN CHỤP
    // ==========================================

    public boolean deleteOrder(long orderId) throws SQLException {

        String checkSql = """
            SELECT COUNT(*)
            FROM payments
            WHERE order_id = ?
        """;

        String deleteSql = """
            DELETE FROM orders
            WHERE order_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // Không cho xóa đơn đã có lịch sử thanh toán
                try (PreparedStatement ps =
                        conn.prepareStatement(checkSql)) {

                    ps.setLong(1, orderId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next() && rs.getInt(1) > 0) {
                            throw new SQLException(
                                "Đơn chụp đã có giao dịch thanh toán. "
                                + "Không thể xóa để bảo toàn lịch sử tài chính."
                            );
                        }
                    }
                }

                try (PreparedStatement ps =
                        conn.prepareStatement(deleteSql)) {

                    ps.setLong(1, orderId);

                    int affected = ps.executeUpdate();

                    if (affected == 0) {
                        throw new SQLException(
                            "Không tìm thấy đơn chụp cần xóa."
                        );
                    }
                }

                conn.commit();
                return true;

            } catch (Exception e) {

                conn.rollback();

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                throw new SQLException(e);

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // KIỂM TRA TRẠNG THÁI ĐƠN
    // ==========================================

    public boolean isValidStatus(String status) {

        return status != null && (
            status.equals("Mới tạo")
            || status.equals("Đang thực hiện")
            || status.equals("Chờ giao ảnh")
            || status.equals("Hoàn thành")
            || status.equals("Đã hủy")
        );
    }
}
