
package com.mycompany.huythanhstudio.view;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private final Map<String, JPanel> pageCache = new java.util.HashMap<>();

    private final JPanel content = new JPanel(new BorderLayout());

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame() {

        setTitle("HUYTHANH STUDIO | Studio Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1360, 850);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        getContentPane().setBackground(Ui.BG);
        setLayout(new BorderLayout());

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Ui.BG);

        shell.add(createSidebar(), BorderLayout.WEST);

        content.setBackground(Ui.BG);
        shell.add(content, BorderLayout.CENTER);

        add(shell, BorderLayout.CENTER);

        showPage("Tổng quan");
    }

    // ==========================================
    // SIDEBAR
    // ==========================================

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(245, 0));
        sidebar.setBackground(Ui.DARK);
        sidebar.setBorder(new EmptyBorder(25, 15, 20, 15));

        // BRAND
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("NHT");
        logo.setFont(new Font("Arial", Font.BOLD, 30));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandName = new JLabel("HUYTHANH");
        brandName.setFont(new Font("Arial", Font.BOLD, 17));
        brandName.setForeground(Color.WHITE);
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("STUDIO MANAGER");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitle.setForeground(new Color(190, 190, 190));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logo);
        brand.add(Box.createVerticalStrut(8));
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(3));
        brand.add(subtitle);
        brand.add(Box.createVerticalStrut(30));

        sidebar.add(brand, BorderLayout.NORTH);

        // MENU
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        String[] items = {
            "Tổng quan",
            "Khách hàng",
            "Nhân viên",
            "Dịch vụ chụp",
            "Ảnh mẫu",
            "Lịch chụp",
            "Đơn chụp",
            "Thanh toán",
            "Giao ảnh",
            "Thống kê",
            "Cài đặt"
        };

        for (String item : items) {

            JButton button = new JButton(item);

            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setFont(new Font("Arial", Font.BOLD, 13));
            button.setForeground(new Color(220, 220, 220));
            button.setBackground(Ui.DARK);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(13, 15, 13, 10));
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            button.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );

            button.addActionListener(e -> showPage(item));

            navButtons.put(item, button);
            menu.add(button);
            menu.add(Box.createVerticalStrut(5));
        }

        sidebar.add(menu, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JLabel footerText = new JLabel("HUYTHANH STUDIO");
        footerText.setForeground(new Color(160, 160, 160));
        footerText.setFont(new Font("Arial", Font.BOLD, 11));

        JLabel version = new JLabel("Studio Management System");
        version.setForeground(new Color(130, 130, 130));
        version.setFont(new Font("Arial", Font.PLAIN, 10));

        footer.add(new JSeparator());
        footer.add(Box.createVerticalStrut(12));
        footer.add(footerText);
        footer.add(Box.createVerticalStrut(5));
        footer.add(version);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    // ==========================================
    // PAGE NAVIGATION
    // ==========================================

    private void showPage(String name) {

        // UPDATE ACTIVE MENU
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {

            JButton button = entry.getValue();

            if (entry.getKey().equals(name)) {
                button.setBackground(new Color(65, 75, 90));
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(Ui.DARK);
                button.setForeground(new Color(220, 220, 220));
            }
        }

        // GET PAGE FROM CACHE
        JPanel page = pageCache.get(name);

        if (page == null) {

            switch (name) {

                case "Tổng quan" ->
                    page = new TongQuanPanel();

                case "Ảnh mẫu" ->
                    page = new GalleryPanel();

                case "Đơn chụp" ->
                    page = new OrderPanel();

                case "Thanh toán" ->
                    page = new PaymentPanel();

                case "Cài đặt" ->
                    page = new SimplePagePanel(
                            name,
                            "Thông tin cấu hình phần mềm và studio."
                    );

                case "Thống kê" ->
                    page = new SimplePagePanel(
                            name,
                            "Tổng hợp hoạt động và doanh thu của studio."
                    );

                default ->
                    page = new ManagementPanel(name);
            }

            pageCache.put(name, page);
        }

        // DISPLAY PAGE
        content.removeAll();
        content.add(page, BorderLayout.CENTER);

        content.revalidate();
        content.repaint();
    }
}
