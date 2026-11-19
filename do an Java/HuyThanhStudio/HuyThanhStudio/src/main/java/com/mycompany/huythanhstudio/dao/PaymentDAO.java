
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class PaymentDAO {

    // ==========================================
    // CLASS LUU THONG TIN DON CHUP
    // ==========================================

    public static class OrderOption {

        public long id;
        public String label;

        public OrderOption(long id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // ==========================================
    // 1. LAY DANH SACH THANH TOAN
    // ==========================================

    public DefaultTableModel getAllPayments() {

        String[] columns = {
            "ID",
            "Mã thanh toán",
            "Mã đơn hàng",
            "Khách hàng",
            "Ngày thanh toán",
            "Số tiền",
            "Phương thức",
            "Trạng thái",
            "Ghi chú"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = """
            SELECT
                p.payment_id,
                p.payment_code,
                o.order_code,
                c.full_name,
                p.paid_at,
                p.amount,
                p.method,
                p.status,
                p.reference_note
            FROM payments p
            INNER JOIN orders o
                ON p.order_id = o.order_id
            INNER JOIN customers c
                ON o.customer_id = c.customer_id
            ORDER BY p.paid_at DESC, p.payment_id DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getLong("payment_id"),
                    rs.getString("payment_code"),
                    rs.getString("order_code"),
                    rs.getString("full_name"),
                    rs.getTimestamp("paid_at"),
                    rs.getBigDecimal("amount"),
                    rs.getString("method"),
                    rs.getString("status"),
                    rs.getString("reference_note")
                });
            }

        } catch (SQLException e) {
            System.out.println("Lỗi lấy danh sách thanh toán:");
            e.printStackTrace();
        }

        return model;
    }

    // ==========================================
    // 2. LAY DANH SACH DON CHUP CON NO
    // ==========================================

    public List<OrderOption> getOrderOptions() {

        List<OrderOption> list = new ArrayList<>();

        String sql = """
            SELECT
                o.order_id,
                o.order_code,
                c.full_name,
                b.booking_code,
                s.service_name,
                o.total_amount,
                COALESCE(
                    SUM(
                        CASE
                            WHEN p.status = 'Đã thanh toán'
                            THEN p.amount
                            ELSE 0
                        END
                    ), 0
                ) AS paid_amount
            FROM orders o
            INNER JOIN customers c
                ON o.customer_id = c.customer_id
            LEFT JOIN bookings b
                ON o.booking_id = b.booking_id
            LEFT JOIN services s
                ON b.service_id = s.service_id
            LEFT JOIN payments p
                ON o.order_id = p.order_id
            WHERE o.status <> 'Đã hủy'
            GROUP BY
                o.order_id,
                o.order_code,
                c.full_name,
                b.booking_code,
                s.service_name,
                o.total_amount
            HAVING o.total_amount >
                COALESCE(
                    SUM(
                        CASE
                            WHEN p.status = 'Đã thanh toán'
                            THEN p.amount
                            ELSE 0
                        END
                    ), 0
                )
            ORDER BY o.order_date DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                long id = rs.getLong("order_id");

                BigDecimal total =
                        rs.getBigDecimal("total_amount");

                BigDecimal paid =
                        rs.getBigDecimal("paid_amount");

                BigDecimal remaining = total.subtract(paid);

                String bookingCode = rs.getString("booking_code");
                String serviceName = rs.getString("service_name");

                String label =
                        rs.getString("order_code")
                        + " | "
                        + rs.getString("full_name")
                        + " | "
                        + (bookingCode == null ? "Chưa có lịch" : bookingCode)
                        + " | "
                        + (serviceName == null ? "Chưa có dịch vụ" : serviceName)
                        + " | Còn nợ: "
                        + remaining.toPlainString()
                        + " đ";

                list.add(new OrderOption(id, label));
            }

        } catch (SQLException e) {
            System.out.println("Lỗi lấy danh sách đơn còn nợ:");
            e.printStackTrace();
        }

        return list;
    }

    // ==========================================
    // 3. THEM GIAO DICH THANH TOAN
    // ==========================================

    public boolean addPayment(
            long orderId,
            BigDecimal amount,
            String method,
            String status,
            String note
    ) throws SQLException {

        validatePayment(amount, method, status);

        String insertSql = """
            INSERT INTO payments (
                payment_code,
                order_id,
                amount,
                method,
                status,
                reference_note
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                BigDecimal remaining =
                        getRemainingAmount(conn, orderId, null);

                if (remaining == null) {
                    throw new SQLException("Không tìm thấy đơn chụp.");
                }

                if ("Đã hủy".equals(getOrderStatus(conn, orderId))) {
                    throw new SQLException(
                            "Không thể thanh toán cho đơn chụp đã hủy."
                    );
                }

                if ("Đã thanh toán".equals(status)
                        && amount.compareTo(remaining) > 0) {

                    throw new SQLException(
                            "Số tiền vượt quá công nợ hiện tại.\n"
                            + "Số tiền còn nợ: "
                            + remaining.toPlainString() + " đ"
                    );
                }

                String paymentCode =
                        "TT" + System.currentTimeMillis();

                try (PreparedStatement ps =
                        conn.prepareStatement(insertSql)) {

                    ps.setString(1, paymentCode);
                    ps.setLong(2, orderId);
                    ps.setBigDecimal(3, amount);
                    ps.setString(4, method);
                    ps.setString(5, status);
                    ps.setString(6, note);

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // 4. SUA TOAN BO GIAO DICH
    // ==========================================

    public boolean updatePayment(
            long paymentId,
            BigDecimal amount,
            String method,
            String status,
            String note
    ) throws SQLException {

        validatePayment(amount, method, status);

        String selectSql = """
            SELECT order_id
            FROM payments
            WHERE payment_id = ?
            FOR UPDATE
        """;

        String updateSql = """
            UPDATE payments
            SET amount = ?,
                method = ?,
                status = ?,
                reference_note = ?
            WHERE payment_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                long orderId;

                try (PreparedStatement ps =
                        conn.prepareStatement(selectSql)) {

                    ps.setLong(1, paymentId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Không tìm thấy giao dịch thanh toán."
                            );
                        }

                        orderId = rs.getLong("order_id");
                    }
                }

                BigDecimal remaining =
                        getRemainingAmount(conn, orderId, paymentId);

                if (remaining == null) {
                    throw new SQLException("Không tìm thấy đơn chụp.");
                }

                if ("Đã thanh toán".equals(status)) {

                    if ("Đã hủy".equals(getOrderStatus(conn, orderId))) {
                        throw new SQLException(
                                "Không thể xác nhận thanh toán cho đơn đã hủy."
                        );
                    }

                    if (amount.compareTo(remaining) > 0) {
                        throw new SQLException(
                                "Số tiền vượt quá công nợ còn lại.\n"
                                + "Số tiền còn nợ: "
                                + remaining.toPlainString() + " đ"
                        );
                    }
                }

                try (PreparedStatement ps =
                        conn.prepareStatement(updateSql)) {

                    ps.setBigDecimal(1, amount);
                    ps.setString(2, method);
                    ps.setString(3, status);
                    ps.setString(4, note);
                    ps.setLong(5, paymentId);

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // 5. CAP NHAT RIENG TRANG THAI
    // ==========================================

    public boolean updatePaymentStatus(
            long paymentId,
            String newStatus
    ) throws SQLException {

        if (!isValidStatus(newStatus)) {
            throw new SQLException(
                    "Trạng thái thanh toán không hợp lệ."
            );
        }

        String selectSql = """
            SELECT order_id, amount
            FROM payments
            WHERE payment_id = ?
            FOR UPDATE
        """;

        String updateSql = """
            UPDATE payments
            SET status = ?
            WHERE payment_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                long orderId;
                BigDecimal amount;

                try (PreparedStatement ps =
                        conn.prepareStatement(selectSql)) {

                    ps.setLong(1, paymentId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Không tìm thấy giao dịch."
                            );
                        }

                        orderId = rs.getLong("order_id");
                        amount = rs.getBigDecimal("amount");
                    }
                }

                if ("Đã thanh toán".equals(newStatus)) {

                    BigDecimal remaining =
                            getRemainingAmount(conn, orderId, paymentId);

                    if (remaining == null) {
                        throw new SQLException("Không tìm thấy đơn chụp.");
                    }

                    if ("Đã hủy".equals(getOrderStatus(conn, orderId))) {
                        throw new SQLException(
                                "Không thể xác nhận giao dịch cho đơn đã hủy."
                        );
                    }

                    if (amount.compareTo(remaining) > 0) {
                        throw new SQLException(
                                "Số tiền vượt quá công nợ còn lại: "
                                + remaining.toPlainString() + " đ"
                        );
                    }
                }

                try (PreparedStatement ps =
                        conn.prepareStatement(updateSql)) {

                    ps.setString(1, newStatus);
                    ps.setLong(2, paymentId);

                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // 6. XOA GIAO DICH
    // ==========================================

    public boolean deletePayment(long paymentId)
            throws SQLException {

        String deleteSql = """
            DELETE FROM payments
            WHERE payment_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // Kiểm tra và khóa giao dịch trước khi xóa.
                String checkSql = """
                    SELECT payment_id
                    FROM payments
                    WHERE payment_id = ?
                    FOR UPDATE
                """;

                try (PreparedStatement ps =
                        conn.prepareStatement(checkSql)) {

                    ps.setLong(1, paymentId);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Không tìm thấy giao dịch cần xóa."
                            );
                        }
                    }
                }

                try (PreparedStatement ps =
                        conn.prepareStatement(deleteSql)) {

                    ps.setLong(1, paymentId);

                    int affected = ps.executeUpdate();

                    if (affected == 0) {
                        throw new SQLException(
                                "Không thể xóa giao dịch."
                        );
                    }
                }

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                throw e;

            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // 7. TINH SO TIEN CON NO
    // ==========================================

    private BigDecimal getRemainingAmount(
            Connection conn,
            long orderId,
            Long excludePaymentId
    ) throws SQLException {

        String sql = """
            SELECT
                o.total_amount,
                COALESCE(
                    (
                        SELECT SUM(p.amount)
                        FROM payments p
                        WHERE p.order_id = o.order_id
                        AND p.status = 'Đã thanh toán'
                        AND (? IS NULL OR p.payment_id <> ?)
                    ), 0
                ) AS paid_amount
            FROM orders o
            WHERE o.order_id = ?
            FOR UPDATE
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            if (excludePaymentId == null) {
                ps.setNull(1, Types.BIGINT);
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(1, excludePaymentId);
                ps.setLong(2, excludePaymentId);
            }

            ps.setLong(3, orderId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                BigDecimal total = rs.getBigDecimal("total_amount");
                BigDecimal paid = rs.getBigDecimal("paid_amount");

                if (total == null) {
                    total = BigDecimal.ZERO;
                }

                if (paid == null) {
                    paid = BigDecimal.ZERO;
                }

                return total.subtract(paid);
            }
        }
    }

    // ==========================================
    // 8. LAY TRANG THAI DON CHUP
    // ==========================================

    private String getOrderStatus(
            Connection conn,
            long orderId
    ) throws SQLException {

        String sql = """
            SELECT status
            FROM orders
            WHERE order_id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        }

        return null;
    }

    // ==========================================
    // 9. KIEM TRA DU LIEU THANH TOAN
    // ==========================================

    private void validatePayment(
            BigDecimal amount,
            String method,
            String status
    ) throws SQLException {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new SQLException(
                    "Số tiền thanh toán phải lớn hơn 0."
            );
        }

        if (!isValidMethod(method)) {
            throw new SQLException(
                    "Phương thức thanh toán không hợp lệ."
            );
        }

        if (!isValidStatus(status)) {
            throw new SQLException(
                    "Trạng thái thanh toán không hợp lệ."
            );
        }
    }

    private boolean isValidMethod(String method) {

        return "Tiền mặt".equals(method)
                || "Chuyển khoản".equals(method)
                || "Thẻ".equals(method)
                || "Khác".equals(method);
    }

    private boolean isValidStatus(String status) {

        return "Đã thanh toán".equals(status)
                || "Chờ xử lý".equals(status)
                || "Hoàn tiền".equals(status);
    }
}
