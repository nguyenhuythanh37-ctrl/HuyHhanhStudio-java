
package com.mycompany.huythanhstudio.view;

import com.mycompany.huythanhstudio.DBConnection;
import com.mycompany.huythanhstudio.dao.PaymentDAO;
import com.mycompany.huythanhstudio.dao.PaymentDAO.OrderOption;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class PaymentPanel extends JPanel {

    private final PaymentDAO paymentDAO = new PaymentDAO();

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel totalValue;
    private JLabel unpaidValue;
    private JLabel countValue;

    public PaymentPanel() {

        setLayout(new BorderLayout());
        setBackground(Ui.BG);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);

        // ======================================
        // HEADER
        // ======================================

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(
                new BoxLayout(titleArea, BoxLayout.Y_AXIS)
        );

        titleArea.add(
                Ui.label("Quản lý thanh toán", 27, true, Ui.DARK)
        );

        titleArea.add(Box.createVerticalStrut(5));

        titleArea.add(
                Ui.label(
                        "Theo dõi giao dịch, doanh thu thực thu và công nợ.",
                        13, false, Ui.MUTED
                )
        );

        JButton refreshButton = createButton("Làm mới");
        refreshButton.addActionListener(e -> refreshData());

        header.add(titleArea, BorderLayout.CENTER);
        header.add(refreshButton, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ======================================
        // STATISTICS
        // ======================================

        JPanel stats = new JPanel(
                new GridLayout(1, 3, 14, 0)
        );
        stats.setOpaque(false);

        totalValue = Ui.label("0 đ", 24, true, Ui.DARK);
        unpaidValue = Ui.label("0 đ", 24, true, Ui.GOLD);
        countValue = Ui.label("0", 24, true, Ui.DARK);

        stats.add(statCard("TỔNG DOANH THU THỰC THU", totalValue));
        stats.add(statCard("TỔNG CÔNG NỢ CÒN LẠI", unpaidValue));
        stats.add(statCard("TỔNG SỐ GIAO DỊCH", countValue));

        // ======================================
        // TOOLBAR
        // ======================================

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        filters.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(230, 36));
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText(
                "Tìm mã thanh toán, mã đơn hoặc tên khách hàng"
        );

        statusFilter = new JComboBox<>(new String[]{
            "Tất cả trạng thái",
            "Đã thanh toán",
            "Chờ xử lý",
            "Hoàn tiền"
        });

        statusFilter.setFont(new Font("Arial", Font.PLAIN, 12));

        filters.add(searchField);
        filters.add(statusFilter);

        JButton addButton = createButton("+ Thêm thanh toán");
        JButton editButton = createButton("Sửa giao dịch");
        JButton deleteButton = createButton("Xóa giao dịch");

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 7, 0)
        );
        actions.setOpaque(false);

        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        // ======================================
        // TABLE
        // ======================================

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

        model = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        table.setRowHeight(36);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFillsViewportHeight(true);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        hideIdColumn();
        configureColumns();

        JPanel card = Ui.card();
        card.add(toolbar, BorderLayout.NORTH);
        card.add(Ui.scroll(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        center.add(stats, BorderLayout.NORTH);
        center.add(card, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);

        // ======================================
        // EVENTS
        // ======================================

        addButton.addActionListener(e -> addPayment());
        editButton.addActionListener(e -> editPayment());
        deleteButton.addActionListener(e -> deletePayment());

        statusFilter.addActionListener(e -> applyFilters());

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        refreshData();
    }

    // ==========================================
    // STATISTIC CARD
    // ==========================================

    private JPanel statCard(String title, JLabel value) {

        JPanel card = Ui.card();

        JPanel content = new JPanel();
        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.add(Ui.label(title, 11, true, Ui.MUTED));
        content.add(Box.createVerticalStrut(12));
        content.add(value);

        card.add(content, BorderLayout.CENTER);

        return card;
    }

    // ==========================================
    // BUTTON
    // ==========================================

    private JButton createButton(String title) {

        JButton button = new JButton(title);

        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setBackground(Ui.DARK);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));

        return button;
    }

    // ==========================================
    // REFRESH DATA
    // ==========================================

    public void refreshData() {

        model.setRowCount(0);

        DefaultTableModel data = paymentDAO.getAllPayments();

        BigDecimal totalPaid = BigDecimal.ZERO;
        int totalTransactions = data.getRowCount();

        for (int i = 0; i < data.getRowCount(); i++) {

            String status = String.valueOf(
                    data.getValueAt(i, 7)
            );

            Object amountObject = data.getValueAt(i, 5);

            if ("Đã thanh toán".equals(status)
                    && amountObject instanceof BigDecimal) {

                totalPaid = totalPaid.add(
                        (BigDecimal) amountObject
                );
            }

            Object[] row = new Object[data.getColumnCount()];

            for (int j = 0; j < data.getColumnCount(); j++) {
                row[j] = data.getValueAt(i, j);
            }

            model.addRow(row);
        }

        BigDecimal totalDebt = getDebtStatistics();

        totalValue.setText(formatMoney(totalPaid));
        unpaidValue.setText(formatMoney(totalDebt));
        countValue.setText(String.valueOf(totalTransactions));

        applyFilters();
    }

    // ==========================================
    // DEBT STATISTICS
    // ==========================================

    private BigDecimal getDebtStatistics() {

        String sql = """
            SELECT COALESCE(
                SUM(
                    GREATEST(
                        o.total_amount - COALESCE(
                            (
                                SELECT SUM(p.amount)
                                FROM payments p
                                WHERE p.order_id = o.order_id
                                AND p.status = 'Đã thanh toán'
                            ), 0
                        ),
                        0
                    )
                ), 0
            ) AS total_debt
            FROM orders o
            WHERE o.status <> 'Đã hủy'
        """;

        try (
            java.sql.Connection conn =
                    DBConnection.getConnection();

            java.sql.PreparedStatement ps =
                    conn.prepareStatement(sql);

            java.sql.ResultSet rs = ps.executeQuery()
        ) {

            if (rs.next()) {

                BigDecimal debt = rs.getBigDecimal("total_debt");

                return debt == null
                        ? BigDecimal.ZERO
                        : debt;
            }

        } catch (Exception e) {

            System.out.println("Lỗi tính công nợ:");
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    // ==========================================
    // SEARCH AND FILTER
    // ==========================================

    private void applyFilters() {

        if (sorter == null) {
            return;
        }

        List<RowFilter<DefaultTableModel, Object>> filters =
                new ArrayList<>();

        String keyword = searchField.getText().trim();

        String selectedStatus =
                (String) statusFilter.getSelectedItem();

        if (!keyword.isEmpty()) {

            filters.add(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(keyword),
                            1, 2, 3, 4, 5, 6, 7, 8
                    )
            );
        }

        if (selectedStatus != null
                && !"Tất cả trạng thái".equals(selectedStatus)) {

            filters.add(
                    RowFilter.regexFilter(
                            "^" + java.util.regex.Pattern.quote(selectedStatus) + "$",
                            7
                    )
            );
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // ==========================================
    // ADD PAYMENT
    // ==========================================

    private void addPayment() {

        List<OrderOption> orders = paymentDAO.getOrderOptions();

        if (orders.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Hiện không có đơn chụp nào còn công nợ.\n"
                    + "Hãy kiểm tra lại dữ liệu đơn chụp.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JComboBox<OrderOption> orderBox =
                new JComboBox<>(
                        orders.toArray(new OrderOption[0])
                );

        JTextField amountField = new JTextField();

        JComboBox<String> methodBox = new JComboBox<>(
                new String[]{
                    "Tiền mặt",
                    "Chuyển khoản",
                    "Thẻ",
                    "Khác"
                }
        );

        JComboBox<String> statusBox = new JComboBox<>(
                new String[]{
                    "Đã thanh toán",
                    "Chờ xử lý"
                }
        );

        JTextField noteField = new JTextField();

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));

        addField(form, "Đơn chụp:", orderBox);
        addField(form, "Số tiền (VNĐ):", amountField);
        addField(form, "Phương thức:", methodBox);
        addField(form, "Trạng thái:", statusBox);
        addField(form, "Ghi chú:", noteField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Thêm giao dịch thanh toán",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            OrderOption selected =
                    (OrderOption) orderBox.getSelectedItem();

            if (selected == null) {
                throw new IllegalArgumentException(
                        "Hãy chọn đơn chụp."
                );
            }

            BigDecimal amount = parseMoney(
                    amountField.getText()
            );

            paymentDAO.addPayment(
                    selected.id,
                    amount,
                    (String) methodBox.getSelectedItem(),
                    (String) statusBox.getSelectedItem(),
                    noteField.getText().trim()
            );

            showSuccess("Thêm giao dịch thanh toán thành công!");

            refreshData();

        } catch (Exception e) {

            showError(
                    "Không thể thêm giao dịch:\n" + e.getMessage()
            );
        }
    }

    // ==========================================
    // EDIT PAYMENT
    // ==========================================

    private void editPayment() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {

            showError("Hãy chọn giao dịch cần sửa.");

            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);

        long paymentId = Long.parseLong(
                model.getValueAt(row, 0).toString()
        );

        String paymentCode =
                String.valueOf(model.getValueAt(row, 1));

        String orderCode =
                String.valueOf(model.getValueAt(row, 2));

        String customer =
                String.valueOf(model.getValueAt(row, 3));

        BigDecimal oldAmount =
                (BigDecimal) model.getValueAt(row, 5);

        String oldMethod =
                String.valueOf(model.getValueAt(row, 6));

        String oldStatus =
                String.valueOf(model.getValueAt(row, 7));

        String oldNote =
                model.getValueAt(row, 8) == null
                        ? ""
                        : String.valueOf(model.getValueAt(row, 8));

        JTextField orderField = new JTextField(
                orderCode + " - " + customer
        );

        JTextField amountField = new JTextField(
                oldAmount.toPlainString()
        );

        orderField.setEditable(false);

        JComboBox<String> methodBox = new JComboBox<>(
                new String[]{
                    "Tiền mặt",
                    "Chuyển khoản",
                    "Thẻ",
                    "Khác"
                }
        );

        methodBox.setSelectedItem(oldMethod);

        JComboBox<String> statusBox = new JComboBox<>(
                new String[]{
                    "Đã thanh toán",
                    "Chờ xử lý",
                    "Hoàn tiền"
                }
        );

        statusBox.setSelectedItem(oldStatus);

        JTextField noteField = new JTextField(oldNote);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));

        addField(form, "Mã giao dịch:", new JLabel(paymentCode));
        addField(form, "Đơn chụp:", orderField);
        addField(form, "Số tiền (VNĐ):", amountField);
        addField(form, "Phương thức:", methodBox);
        addField(form, "Trạng thái:", statusBox);

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));

        wrapper.add(form, BorderLayout.CENTER);

        JPanel notePanel = new JPanel(new BorderLayout(8, 0));
        notePanel.add(new JLabel("Ghi chú:"), BorderLayout.WEST);
        notePanel.add(noteField, BorderLayout.CENTER);

        wrapper.add(notePanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(
                this,
                wrapper,
                "Sửa giao dịch thanh toán",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            BigDecimal amount = parseMoney(
                    amountField.getText()
            );

            paymentDAO.updatePayment(
                    paymentId,
                    amount,
                    (String) methodBox.getSelectedItem(),
                    (String) statusBox.getSelectedItem(),
                    noteField.getText().trim()
            );

            showSuccess("Cập nhật giao dịch thành công!");

            refreshData();

        } catch (Exception e) {

            showError(
                    "Không thể cập nhật giao dịch:\n" + e.getMessage()
            );
        }
    }

    // ==========================================
    // DELETE PAYMENT
    // ==========================================

    private void deletePayment() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {

            showError("Hãy chọn giao dịch cần xóa.");

            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);

        long paymentId = Long.parseLong(
                model.getValueAt(row, 0).toString()
        );

        String paymentCode =
                String.valueOf(model.getValueAt(row, 1));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Mày có chắc chắn muốn xóa giao dịch "
                + paymentCode + " không?\n"
                + "Thao tác này sẽ làm thay đổi công nợ của đơn chụp.",
                "Xác nhận xóa giao dịch",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            paymentDAO.deletePayment(paymentId);

            showSuccess("Đã xóa giao dịch thanh toán!");

            refreshData();

        } catch (Exception e) {

            showError(
                    "Không thể xóa giao dịch:\n" + e.getMessage()
            );
        }
    }

    // ==========================================
    // FORM UTILITIES
    // ==========================================

    private void addField(
            JPanel panel,
            String title,
            Component component
    ) {

        panel.add(new JLabel(title));
        panel.add(component);
    }

    // ==========================================
    // HIDE ID COLUMN
    // ==========================================

    private void hideIdColumn() {

        for (int i = 0;
                i < table.getColumnModel().getColumnCount();
                i++) {

            TableColumn column =
                    table.getColumnModel().getColumn(i);

            if (column.getModelIndex() == 0) {

                table.removeColumn(column);

                return;
            }
        }
    }

    // ==========================================
    // TABLE WIDTH
    // ==========================================

    private void configureColumns() {

        int[] widths = {
            135, 125, 160, 145,
            125, 120, 125, 190
        };

        for (int i = 0;
                i < table.getColumnModel().getColumnCount();
                i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }
    }

    // ==========================================
    // PARSE MONEY
    // ==========================================

    private BigDecimal parseMoney(String value) {

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Hãy nhập số tiền thanh toán."
            );
        }

        String normalized = value.trim()
                .replace(",", "")
                .replace(".", "")
                .replace(" ", "");

        return new BigDecimal(normalized);
    }

    // ==========================================
    // FORMAT MONEY
    // ==========================================

    private String formatMoney(BigDecimal money) {

        if (money == null) {
            money = BigDecimal.ZERO;
        }

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(new Locale("vi", "VN"));

        DecimalFormat format =
                new DecimalFormat("#,##0", symbols);

        return format.format(money) + " đ";
    }

    // ==========================================
    // MESSAGE
    // ==========================================

    private void showSuccess(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
