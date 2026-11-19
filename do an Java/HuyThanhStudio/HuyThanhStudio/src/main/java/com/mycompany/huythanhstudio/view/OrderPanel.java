
package com.mycompany.huythanhstudio.view;

import com.mycompany.huythanhstudio.dao.OrderDAO;
import com.mycompany.huythanhstudio.dao.OrderDAO.BookingOption;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class OrderPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();

    private JTable table;
    private DefaultTableModel model;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    private JLabel orderCount;
    private JLabel revenueValue;

    private TableRowSorter<DefaultTableModel> sorter;

    public OrderPanel() {

        setLayout(new BorderLayout(0, 18));
        setBackground(Ui.BG);
        setBorder(new EmptyBorder(25, 28, 25, 28));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(
                new BoxLayout(titleArea, BoxLayout.Y_AXIS)
        );

        titleArea.add(
                Ui.label("Quản lý đơn chụp", 27, true, Ui.DARK)
        );

        titleArea.add(Box.createVerticalStrut(5));

        titleArea.add(
                Ui.label(
                        "Quản lý đơn hàng được tạo từ lịch chụp.",
                        13, false, Ui.MUTED
                )
        );

        JButton refreshButton = Ui.button("Làm mới");
        refreshButton.addActionListener(e -> refreshData());

        header.add(titleArea, BorderLayout.CENTER);
        header.add(refreshButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // STATISTICS
        JPanel statistics = new JPanel(
                new GridLayout(1, 2, 14, 0)
        );
        statistics.setOpaque(false);

        orderCount = Ui.label("0", 24, true, Ui.DARK);
        revenueValue = Ui.label("0 đ", 24, true, Ui.GOLD);

        statistics.add(
                createStatCard("TỔNG SỐ ĐƠN", orderCount)
        );

        statistics.add(
                createStatCard("TỔNG GIÁ TRỊ ĐƠN", revenueValue)
        );

        // SEARCH AND FILTER
        JPanel tools = new JPanel(new BorderLayout(10, 0));
        tools.setOpaque(false);

        JPanel filters = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        filters.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setFont(Ui.FONT);
        searchField.setToolTipText(
                "Tìm mã đơn, mã lịch hoặc tên khách hàng"
        );

        statusFilter = new JComboBox<>(new String[]{
            "Tất cả trạng thái",
            "Mới tạo",
            "Đang thực hiện",
            "Chờ giao ảnh",
            "Hoàn thành",
            "Đã hủy"
        });

        statusFilter.setFont(Ui.FONT);

        filters.add(searchField);
        filters.add(statusFilter);

        JButton addButton = Ui.button("+ Tạo đơn");
        JButton editButton = Ui.button("Sửa");
        JButton deleteButton = Ui.button("Xóa");

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 0)
        );
        actions.setOpaque(false);

        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        tools.add(filters, BorderLayout.WEST);
        tools.add(actions, BorderLayout.EAST);

        // TABLE
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

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(Ui.FONT);
        table.getTableHeader().setFont(Ui.FONT);
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        JPanel card = Ui.card();
        card.add(tools, BorderLayout.NORTH);
        card.add(Ui.scroll(table), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        center.add(statistics, BorderLayout.NORTH);
        center.add(card, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // EVENTS
        addButton.addActionListener(e -> addOrder());
        editButton.addActionListener(e -> editOrder());
        deleteButton.addActionListener(e -> deleteOrder());

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

        statusFilter.addActionListener(e -> applyFilters());

        refreshData();
    }

    // ==========================================
    // STATISTIC CARD
    // ==========================================

    private JPanel createStatCard(String title, JLabel value) {

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
    // LOAD DATA
    // ==========================================

    public void refreshData() {

        model.setRowCount(0);

        DefaultTableModel data = orderDAO.getAllOrders();

        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < data.getRowCount(); i++) {

            Object[] row = new Object[data.getColumnCount()];

            for (int j = 0; j < data.getColumnCount(); j++) {
                row[j] = data.getValueAt(i, j);
            }

            model.addRow(row);

            Object amount = data.getValueAt(i, 9);

            if (amount instanceof BigDecimal) {
                total = total.add((BigDecimal) amount);
            }
        }

        orderCount.setText(String.valueOf(model.getRowCount()));
        revenueValue.setText(formatMoney(total));

        hideIdColumn();
        applyFilters();
        configureColumns();
    }

    // ==========================================
    // FILTER
    // ==========================================

    private void applyFilters() {

        if (sorter == null) {
            return;
        }

        String keyword = searchField.getText().trim();
        String status = statusFilter.getSelectedItem().toString();

        java.util.List<RowFilter<DefaultTableModel, Object>> filters =
                new java.util.ArrayList<>();

        if (!keyword.isEmpty()) {
            filters.add(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(keyword),
                            1, 2, 3, 4
                    )
            );
        }

        if (!"Tất cả trạng thái".equals(status)) {
            filters.add(
                    RowFilter.regexFilter(
                            "^" + java.util.regex.Pattern.quote(status) + "$",
                            10
                    )
            );
        }

        sorter.setRowFilter(
                filters.isEmpty()
                        ? null
                        : RowFilter.andFilter(filters)
        );
    }

    // ==========================================
    // ADD ORDER
    // ==========================================

    private void addOrder() {

        List<BookingOption> bookings;

        try {
            bookings = orderDAO.getBookingOptions();
        } catch (Exception e) {
            showError("Không thể tải lịch chụp: " + e.getMessage());
            return;
        }

        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không có lịch chụp nào đủ điều kiện tạo đơn.\n"
                    + "Hãy kiểm tra lịch chụp và trạng thái của lịch.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        JComboBox<BookingOption> bookingBox =
                new JComboBox<>(bookings.toArray(new BookingOption[0]));

        JTextField priceField = new JTextField();
        JTextField discountField = new JTextField("0");
        JTextField totalField = new JTextField();
        JTextField noteField = new JTextField();

        priceField.setEditable(false);
        totalField.setEditable(false);

        JComboBox<String> statusBox = new JComboBox<>(
                new String[]{
                    "Mới tạo",
                    "Đang thực hiện",
                    "Chờ giao ảnh",
                    "Hoàn thành",
                    "Đã hủy"
                }
        );

        Runnable calculate = () -> {
            BookingOption selected =
                    (BookingOption) bookingBox.getSelectedItem();

            if (selected == null) {
                return;
            }

            BigDecimal price = selected.price;
            priceField.setText(formatMoney(price));

            try {
                BigDecimal discount = new BigDecimal(
                        discountField.getText().trim().isEmpty()
                                ? "0"
                                : discountField.getText().trim()
                );

                if (discount.compareTo(BigDecimal.ZERO) < 0
                        || discount.compareTo(price) > 0) {
                    totalField.setText("Giảm giá không hợp lệ");
                    return;
                }

                totalField.setText(
                        formatMoney(price.subtract(discount))
                );

            } catch (NumberFormatException e) {
                totalField.setText("Giảm giá không hợp lệ");
            }
        };

        bookingBox.addActionListener(e -> calculate.run());

        discountField.getDocument().addDocumentListener(
                new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calculate.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calculate.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calculate.run();
            }
        });

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));

        addField(form, "Lịch chụp:", bookingBox);
        addField(form, "Giá dịch vụ:", priceField);
        addField(form, "Giảm giá (VNĐ):", discountField);
        addField(form, "Thành tiền:", totalField);
        addField(form, "Trạng thái:", statusBox);
        addField(form, "Ghi chú:", noteField);

        calculate.run();

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Tạo đơn chụp",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            BookingOption selected =
                    (BookingOption) bookingBox.getSelectedItem();

            if (selected == null) {
                throw new IllegalArgumentException(
                        "Hãy chọn lịch chụp."
                );
            }

            BigDecimal discount = new BigDecimal(
                    discountField.getText().trim().isEmpty()
                            ? "0"
                            : discountField.getText().trim()
            );

            orderDAO.addOrder(
                    selected.id,
                    discount,
                    statusBox.getSelectedItem().toString(),
                    noteField.getText().trim()
            );

            showSuccess("Tạo đơn chụp thành công!");
            refreshData();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ==========================================
    // EDIT ORDER
    // ==========================================

    private void editOrder() {

        int selected = table.getSelectedRow();

        if (selected < 0) {
            showError("Hãy chọn đơn chụp cần sửa.");
            return;
        }

        int row = table.convertRowIndexToModel(selected);

        long orderId = Long.parseLong(
                model.getValueAt(row, 0).toString()
        );

        String orderCode =
                model.getValueAt(row, 1).toString();

        BigDecimal subtotal =
                (BigDecimal) model.getValueAt(row, 7);

        BigDecimal oldDiscount =
                (BigDecimal) model.getValueAt(row, 8);

        JTextField priceField = new JTextField(
                formatMoney(subtotal)
        );

        JTextField discountField = new JTextField(
                oldDiscount.toPlainString()
        );

        JTextField totalField = new JTextField();

        priceField.setEditable(false);
        totalField.setEditable(false);

        JComboBox<String> statusBox = new JComboBox<>(
                new String[]{
                    "Mới tạo",
                    "Đang thực hiện",
                    "Chờ giao ảnh",
                    "Hoàn thành",
                    "Đã hủy"
                }
        );

        statusBox.setSelectedItem(
                model.getValueAt(row, 10).toString()
        );

        JTextField noteField = new JTextField(
                model.getValueAt(row, 11) == null
                        ? ""
                        : model.getValueAt(row, 11).toString()
        );

        Runnable calculate = () -> {
            try {
                BigDecimal discount = new BigDecimal(
                        discountField.getText().trim().isEmpty()
                                ? "0"
                                : discountField.getText().trim()
                );

                if (discount.compareTo(BigDecimal.ZERO) < 0
                        || discount.compareTo(subtotal) > 0) {
                    totalField.setText("Không hợp lệ");
                    return;
                }

                totalField.setText(
                        formatMoney(subtotal.subtract(discount))
                );

            } catch (NumberFormatException e) {
                totalField.setText("Không hợp lệ");
            }
        };

        discountField.getDocument().addDocumentListener(
                new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                calculate.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                calculate.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                calculate.run();
            }
        });

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));

        addField(form, "Giá gốc:", priceField);
        addField(form, "Giảm giá (VNĐ):", discountField);
        addField(form, "Thành tiền:", totalField);
        addField(form, "Trạng thái:", statusBox);
        addField(form, "Ghi chú:", noteField);

        calculate.run();

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Sửa đơn " + orderCode,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            BigDecimal discount = new BigDecimal(
                    discountField.getText().trim().isEmpty()
                            ? "0"
                            : discountField.getText().trim()
            );

            orderDAO.updateOrder(
                    orderId,
                    discount,
                    statusBox.getSelectedItem().toString(),
                    noteField.getText().trim()
            );

            showSuccess("Cập nhật đơn chụp thành công!");
            refreshData();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    // ==========================================
    // DELETE ORDER
    // ==========================================

    private void deleteOrder() {

        int selected = table.getSelectedRow();

        if (selected < 0) {
            showError("Hãy chọn đơn chụp cần xóa.");
            return;
        }

        int row = table.convertRowIndexToModel(selected);

        long orderId = Long.parseLong(
                model.getValueAt(row, 0).toString()
        );

        String orderCode =
                model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Mày có chắc muốn xóa đơn " + orderCode + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            orderDAO.deleteOrder(orderId);

            showSuccess("Đã xóa đơn chụp!");
            refreshData();

        } catch (Exception e) {
            showError(e.getMessage());
        }
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
            90, 90, 150, 140, 145, 120,
            110, 110, 110, 120, 150
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
    // FORM UTILITIES
    // ==========================================

    private void addField(
            JPanel panel,
            String title,
            Component component) {

        panel.add(new JLabel(title));
        panel.add(component);
    }

    // ==========================================
    // MONEY FORMAT
    // ==========================================

    private String formatMoney(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(new Locale("vi", "VN"));

        DecimalFormat format =
                new DecimalFormat("#,##0", symbols);

        return format.format(amount) + " đ";
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
                message == null ? "Đã xảy ra lỗi." : message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
