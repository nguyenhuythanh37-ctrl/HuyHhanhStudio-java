
package com.mycompany.huythanhstudio.dao;

import com.mycompany.huythanhstudio.DBConnection;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardDAO {

    // 1. THONG KE TONG QUAN
    public Object[] getStatistics() {

        String sql = """
            SELECT
                (SELECT COUNT(*) FROM customers),

                (SELECT COUNT(*)
                 FROM services
                 WHERE is_active = TRUE),

                (SELECT COUNT(*)
                 FROM bookings
                 WHERE scheduled_at >= NOW()
                 AND status IN ('Chờ xác nhận', 'Đã xác nhận')),

                (SELECT COALESCE(SUM(amount), 0)
                 FROM payments
                 WHERE status = 'Đã thanh toán'
                 AND paid_at >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
                 AND paid_at < DATE_FORMAT(
                     CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01'
                 ))
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double revenue = rs.getDouble(4);

                DecimalFormatSymbols symbols =
                        new DecimalFormatSymbols(new Locale("vi", "VN"));

                DecimalFormat df =
                        new DecimalFormat("#,##0", symbols);

                return new Object[]{
                    rs.getInt(1),
                    rs.getInt(2),
                    rs.getInt(3),
                    df.format(revenue) + " đ"
                };
            }

        } catch (SQLException e) {
            System.out.println("Loi lay thong ke tong quan:");
            e.printStackTrace();
        }

        return new Object[]{0, 0, 0, "0 đ"};
    }

    // 2. DANH SACH LICH CHUP
    public List<Object[]> getUpcomingBookings() {

        List<Object[]> list = new ArrayList<>();

        String sql = """
            SELECT
                b.booking_code,
                c.full_name,
                s.service_name,
                b.scheduled_at,
                b.status
            FROM bookings b
            LEFT JOIN customers c
                ON b.customer_id = c.customer_id
            LEFT JOIN services s
                ON b.service_id = s.service_id
            ORDER BY
                CASE
                    WHEN b.scheduled_at >= NOW()
                    THEN 0
                    ELSE 1
                END ASC,
                CASE
                    WHEN b.scheduled_at >= NOW()
                    THEN b.scheduled_at
                END ASC,
                CASE
                    WHEN b.scheduled_at < NOW()
                    THEN b.scheduled_at
                END DESC
            LIMIT 8
        """;

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Timestamp timestamp =
                        rs.getTimestamp("scheduled_at");

                String date = timestamp != null
                        ? timestamp.toLocalDateTime().format(formatter)
                        : "";

                list.add(new Object[]{
                    rs.getString("booking_code"),
                    rs.getString("full_name"),
                    rs.getString("service_name"),
                    date,
                    rs.getString("status")
                });
            }

        } catch (SQLException e) {
            System.out.println("Loi lay danh sach lich chup:");
            e.printStackTrace();
        }

        return list;
    }

    // 3. HOAT DONG GAN DAY
    public List<Object[]> getRecentActivities() {

        List<Object[]> list = new ArrayList<>();

        String sql = """
            SELECT
                activity_title,
                activity_detail,
                activity_time
            FROM (
                SELECT
                    'Khách hàng mới' AS activity_title,
                    CONCAT(full_name, ' vừa được thêm') AS activity_detail,
                    created_at AS activity_time
                FROM customers

                UNION ALL

                SELECT
                    'Lịch chụp mới',
                    CONCAT('Đã tạo lịch chụp ', booking_code),
                    created_at
                FROM bookings

                UNION ALL

                SELECT
                    'Thanh toán',
                    CONCAT(
                        'Thanh toán ',
                        payment_code,
                        ' đã được ghi nhận'
                    ),
                    paid_at
                FROM payments
                WHERE status = 'Đã thanh toán'
            ) AS activities
            ORDER BY activity_time DESC
            LIMIT 5
        """;

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Timestamp timestamp =
                        rs.getTimestamp("activity_time");

                String time = timestamp != null
                        ? timestamp.toLocalDateTime().format(formatter)
                        : "";

                list.add(new Object[]{
                    rs.getString("activity_title"),
                    rs.getString("activity_detail"),
                    time
                });
            }

        } catch (SQLException e) {
            System.out.println("Loi lay hoat dong gan day:");
            e.printStackTrace();
        }

        return list;
    }

    // 4. THONG TIN NHANH
    public Object[] getQuickStatistics() {

        String sql = """
            SELECT
                (SELECT COUNT(*)
                 FROM bookings
                 WHERE status = 'Chờ xác nhận'),

                (SELECT COUNT(*)
                 FROM orders o
                 WHERE o.status <> 'Đã hủy'
                 AND o.total_amount > COALESCE(
                     (
                         SELECT SUM(p.amount)
                         FROM payments p
                         WHERE p.order_id = o.order_id
                         AND p.status = 'Đã thanh toán'
                     ), 0
                 )),

                (SELECT COUNT(*)
                 FROM orders
                 WHERE status = 'Hoàn thành')
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return new Object[]{
                    rs.getInt(1),
                    rs.getInt(2),
                    rs.getInt(3)
                };
            }

        } catch (SQLException e) {
            System.out.println("Loi lay thong tin nhanh:");
            e.printStackTrace();
        }

        return new Object[]{0, 0, 0};
    }
}
