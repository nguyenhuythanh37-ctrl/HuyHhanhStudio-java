
package com.mycompany.huythanhstudio.view;

import com.mycompany.huythanhstudio.dao.DashboardDAO;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class TongQuanPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private final JLabel clock = Ui.label("", 12, false, Ui.GOLD);

    private final JLabel customerValue =
            Ui.label("0", 25, true, Ui.DARK);

    private final JLabel serviceValue =
            Ui.label("0", 25, true, Ui.DARK);

    private final JLabel bookingValue =
            Ui.label("0", 25, true, Ui.DARK);

    private final JLabel revenueValue =
            Ui.label("0 đ", 25, true, Ui.DARK);

    private final JLabel pendingValue =
            Ui.label("0", 20, true, Ui.GOLD);

    private final JLabel unpaidValue =
            Ui.label("0", 20, true, Ui.GOLD);

    private final JLabel completedValue =
            Ui.label("0", 20, true, Ui.GOLD);

    private final JTable bookingTable = new JTable();

    private final JPanel activityContent = new JPanel();

    public TongQuanPanel() {

        setLayout(new BorderLayout());
        setBackground(Ui.BG);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setOpaque(false);

        // =========================
        // HEADING
        // =========================

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(
                new BoxLayout(titleArea, BoxLayout.Y_AXIS)
        );

        titleArea.add(
                Ui.label("Tổng quan", 27, true, Ui.DARK)
        );

        titleArea.add(Box.createVerticalStrut(5));

        titleArea.add(
                Ui.label(
                        "Theo dõi tình hình hoạt động của studio.",
                        13, false, Ui.MUTED
                )
        );

        titleArea.add(Box.createVerticalStrut(8));
        titleArea.add(clock);

        JButton refreshButton = new JButton("Làm mới");

        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );
        refreshButton.setBackground(Ui.DARK);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(
                new Font("Arial", Font.BOLD, 12)
        );
        refreshButton.setBorder(
                new EmptyBorder(10, 18, 10, 18)
        );

        refreshButton.addActionListener(e -> refreshData());

        heading.add(titleArea, BorderLayout.CENTER);
        heading.add(refreshButton, BorderLayout.EAST);

        root.add(heading, BorderLayout.NORTH);

        // =========================
        // BODY
        // =========================

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);

        // =========================
        // STATISTICS
        // =========================

        JPanel stats = new JPanel(
                new GridLayout(1, 4, 14, 0)
        );

        stats.setOpaque(false);

        stats.add(stat(
                "TỔNG KHÁCH HÀNG",
                customerValue,
                "Khách hàng đã đăng ký"
        ));

        stats.add(stat(
                "TỔNG DỊCH VỤ",
                serviceValue,
                "Dịch vụ đang cung cấp"
        ));

        stats.add(stat(
                "LỊCH CHỤP SẮP TỚI",
                bookingValue,
                "Buổi chụp sắp diễn ra"
        ));

        stats.add(stat(
                "DOANH THU THÁNG",
                revenueValue,
                "Doanh thu đã thanh toán"
        ));

        body.add(stats, BorderLayout.NORTH);

        // =========================
        // LOWER AREA
        // =========================

        JPanel lower = new JPanel(
                new GridLayout(1, 2, 14, 0)
        );

        lower.setOpaque(false);

        // =========================
        // UPCOMING BOOKINGS
        // =========================

        JPanel bookings = Ui.card();

        JPanel bookingHeader = new JPanel();

        bookingHeader.setOpaque(false);
        bookingHeader.setLayout(
                new BoxLayout(bookingHeader, BoxLayout.Y_AXIS)
        );

        bookingHeader.add(
                Ui.label("Lịch chụp sắp tới", 16, true, Ui.DARK)
        );

        bookingHeader.add(Box.createVerticalStrut(4));

        bookingHeader.add(
                Ui.label(
                        "Các lịch chụp gần nhất",
                        11, false, Ui.MUTED
                )
        );

        bookings.add(bookingHeader, BorderLayout.NORTH);

        bookingTable.setRowHeight(30);
        bookingTable.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        bookingTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        bookingTable.setFillsViewportHeight(true);

        bookings.add(
                Ui.scroll(bookingTable),
                BorderLayout.CENTER
        );

        lower.add(bookings);

        // =========================
        // RIGHT AREA
        // =========================

        JPanel right = new JPanel(
                new GridLayout(2, 1, 0, 14)
        );

        right.setOpaque(false);

        // =========================
        // RECENT ACTIVITIES
        // =========================

        JPanel activity = Ui.card();

        activity.add(
                Ui.label(
                        "Hoạt động gần đây",
                        16, true, Ui.DARK
                ),
                BorderLayout.NORTH
        );

        activityContent.setOpaque(false);
        activityContent.setLayout(
                new BoxLayout(
                        activityContent,
                        BoxLayout.Y_AXIS
                )
        );

        activity.add(
                activityContent,
                BorderLayout.CENTER
        );

        right.add(activity);

        // =========================
        // QUICK INFORMATION
        // =========================

        JPanel quick = Ui.card();

        quick.add(
                Ui.label(
                        "Thông tin nhanh",
                        16, true, Ui.DARK
                ),
                BorderLayout.NORTH
        );

        JPanel metrics = new JPanel(
                new GridLayout(1, 3, 8, 0)
        );

        metrics.setOpaque(false);

        metrics.add(
                metric(pendingValue, "Chờ xác nhận")
        );

        metrics.add(
                metric(unpaidValue, "Chưa thanh toán")
        );

        metrics.add(
                metric(completedValue, "Hoàn thành")
        );

        quick.add(metrics, BorderLayout.CENTER);

        right.add(quick);

        lower.add(right);

        body.add(lower, BorderLayout.CENTER);

        root.add(body, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);

        // =========================
        // CLOCK
        // =========================

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd/MM/yyyy  •  HH:mm:ss",
                        new Locale("vi", "VN")
                );

        Timer timer = new Timer(1000, e ->
                clock.setText(
                        LocalDateTime.now().format(formatter)
                )
        );

        timer.setInitialDelay(0);
        timer.start();

        // =========================
        // LOAD MYSQL DATA
        // =========================

        refreshData();
    }

    // ==========================================
    // STATISTIC CARD
    // ==========================================

    private JPanel stat(
            String title,
            JLabel value,
            String detail
    ) {

        JPanel panel = Ui.card();

        JPanel content = new JPanel();

        content.setOpaque(false);
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.add(
                Ui.label(title, 11, true, Ui.MUTED)
        );

        content.add(Box.createVerticalStrut(13));

        content.add(value);

        content.add(Box.createVerticalStrut(6));

        content.add(
                Ui.label(detail, 11, false, Ui.MUTED)
        );

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // ACTIVITY ROW
    // ==========================================

    private JPanel activityRow(
            String title,
            String detail,
            String time
    ) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        panel.setBorder(
                new EmptyBorder(9, 0, 9, 0)
        );

        JPanel content = new JPanel();

        content.setOpaque(false);
        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.add(
                Ui.label(title, 12, true, Ui.DARK)
        );

        content.add(Box.createVerticalStrut(4));

        content.add(
                Ui.label(detail, 11, false, Ui.MUTED)
        );

        panel.add(content, BorderLayout.CENTER);

        panel.add(
                Ui.label(time, 10, false, Ui.GOLD),
                BorderLayout.EAST
        );

        return panel;
    }

    // ==========================================
    // QUICK METRIC
    // ==========================================

    private JPanel metric(
            JLabel number,
            String title
    ) {

        JPanel panel = new JPanel();

        panel.setBackground(Ui.BG);

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                new EmptyBorder(10, 5, 10, 5)
        );

        JLabel label = Ui.label(
                title, 10, false, Ui.MUTED
        );

        number.setAlignmentX(0.5f);
        label.setAlignmentX(0.5f);

        panel.add(number);

        panel.add(Box.createVerticalStrut(4));

        panel.add(label);

        return panel;
    }

    // ==========================================
    // REFRESH DATA FROM MYSQL
    // ==========================================

    public void refreshData() {

        try {

            // 1. TONG QUAN
            Object[] stats =
                    dashboardDAO.getStatistics();

            customerValue.setText(
                    String.valueOf(stats[0])
            );

            serviceValue.setText(
                    String.valueOf(stats[1])
            );

            bookingValue.setText(
                    String.valueOf(stats[2])
            );

            revenueValue.setText(
                    String.valueOf(stats[3])
            );

            // 2. THONG TIN NHANH
            Object[] quick =
                    dashboardDAO.getQuickStatistics();

            pendingValue.setText(
                    String.valueOf(quick[0])
            );

            unpaidValue.setText(
                    String.valueOf(quick[1])
            );

            completedValue.setText(
                    String.valueOf(quick[2])
            );

            // 3. LICH CHUP SAP TOI
            String[] columns = {
                "Mã lịch",
                "Khách hàng",
                "Dịch vụ",
                "Ngày chụp",
                "Trạng thái"
            };

            DefaultTableModel bookingModel =
                    new DefaultTableModel(columns, 0) {

                @Override
                public boolean isCellEditable(
                        int row,
                        int column
                ) {
                    return false;
                }
            };

            List<Object[]> bookings =
                    dashboardDAO.getUpcomingBookings();

            for (Object[] row : bookings) {
                bookingModel.addRow(row);
            }

            bookingTable.setModel(bookingModel);

            // 4. HOAT DONG GAN DAY
            activityContent.removeAll();

            List<Object[]> activities =
                    dashboardDAO.getRecentActivities();

            if (activities.isEmpty()) {

                activityContent.add(
                        Ui.label(
                                "Chưa có hoạt động nào.",
                                12, false, Ui.MUTED
                        )
                );

            } else {

                for (Object[] item : activities) {

                    activityContent.add(
                            activityRow(
                                    String.valueOf(item[0]),
                                    String.valueOf(item[1]),
                                    String.valueOf(item[2])
                            )
                    );
                }
            }

            activityContent.revalidate();
            activityContent.repaint();

            revalidate();
            repaint();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể cập nhật dữ liệu tổng quan:\n"
                    + e.getMessage(),
                    "Lỗi Dashboard",
                    JOptionPane.ERROR_MESSAGE
            );

            e.printStackTrace();
        }
    }
}
