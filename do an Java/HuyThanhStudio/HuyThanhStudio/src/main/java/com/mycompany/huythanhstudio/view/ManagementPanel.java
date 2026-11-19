
package com.mycompany.huythanhstudio.view;

import com.mycompany.huythanhstudio.dao.CustomerDAO;
import com.mycompany.huythanhstudio.dao.EmployeeDAO;
import com.mycompany.huythanhstudio.dao.ServiceDAO;
import com.mycompany.huythanhstudio.dao.BookingDAO;
import com.mycompany.huythanhstudio.dao.BookingDAO.BookingInfo;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

public class ManagementPanel extends JPanel {

    private DefaultTableModel model;
    private final String module;
    private JTable table;

    private JTextField search;
    private JTextField dateFilter;
    private JComboBox<String> statusFilter;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    private boolean loading = false;

    private static final String[] BOOKING_STATUSES = {
        "Chờ xác nhận",
        "Đã xác nhận",
        "Đã hoàn thành",
        "Đã hủy"
    };

    public ManagementPanel(String module) {
        this.module = module;

        setLayout(new BorderLayout(0, 16));
        setBackground(Ui.BG);
        setBorder(BorderFactory.createEmptyBorder(25, 28, 25, 28));

        add(Ui.label(module, 27, true, Ui.DARK), BorderLayout.NORTH);

        String[] columns = columnsFor(module);

        if (isDatabaseModule()) {
            model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        } else {
            model = new DefaultTableModel(sampleFor(module), columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
        }

        table = new JTable(model);
        table.setRowHeight(38);
        table.setFont(Ui.FONT);
        table.getTableHeader().setFont(Ui.FONT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);

        JPanel tools = new JPanel(new BorderLayout(10, 0));
        tools.setOpaque(false);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);

        search = new JTextField();
        search.setFont(Ui.FONT);
        search.setPreferredSize(new Dimension(220, 36));

        if (module.equals("Lịch chụp")) {
            search.setToolTipText("Tìm theo mã lịch hoặc tên khách hàng");
        } else {
            search.setToolTipText("Nhập từ khóa tìm kiếm");
        }

        filters.add(search);

        if (module.equals("Lịch chụp")) {
            dateFilter = new JTextField();
            dateFilter.setFont(Ui.FONT);
            dateFilter.setPreferredSize(new Dimension(115, 36));
            dateFilter.setToolTipText("Ngày lọc theo định dạng yyyy-MM-dd");

            statusFilter = new JComboBox<>(new String[]{
                "Tất cả trạng thái",
                "Chờ xác nhận",
                "Đã xác nhận",
                "Đã hoàn thành",
                "Đã hủy"
            });
            statusFilter.setFont(Ui.FONT);

            filters.add(new JLabel("Ngày:"));
            filters.add(dateFilter);
            filters.add(statusFilter);
        }

        JButton add = Ui.button("+ Thêm mới");
        JButton edit = Ui.button("Sửa");
        JButton delete = Ui.button("Xóa dòng");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(edit);
        actions.add(delete);

        tools.add(filters, BorderLayout.WEST);
        tools.add(actions, BorderLayout.EAST);

        center.add(tools, BorderLayout.NORTH);
        center.add(Ui.scroll(table), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        add.addActionListener(e -> addRecord());
        edit.addActionListener(e -> editRecord());
        delete.addActionListener(e -> deleteRecord());

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearch();
            }
        });

        if (module.equals("Lịch chụp")) {
            dateFilter.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        loadBookingsFiltered();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        loadBookingsFiltered();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        loadBookingsFiltered();
                    }
                }
            );

            statusFilter.addActionListener(e -> loadBookingsFiltered());
        }

        loadModuleData();
    }

    // =========================
    // KIỂM TRA MODULE
    // =========================

    private boolean isDatabaseModule() {
        return module.equals("Khách hàng")
            || module.equals("Nhân viên")
            || module.equals("Dịch vụ chụp")
            || module.equals("Lịch chụp");
    }

    private void loadModuleData() {
        switch (module) {
            case "Khách hàng" -> loadCustomers();
            case "Nhân viên" -> loadEmployees();
            case "Dịch vụ chụp" -> loadServices();
            case "Lịch chụp" -> loadBookingsFiltered();
            default -> {
            }
        }
    }

    // =========================
    // TÌM KIẾM
    // =========================

    private void applySearch() {
        if (loading) {
            return;
        }

        if (module.equals("Lịch chụp")) {
            loadBookingsFiltered();
            return;
        }

        TableRowSorter<DefaultTableModel> sorter =
            new TableRowSorter<>(model);

        table.setRowSorter(sorter);

        String keyword = search.getText().trim();

        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(
                javax.swing.RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(keyword)
                )
            );
        }
    }

    // =========================
    // TẢI DỮ LIỆU
    // =========================

    private void loadCustomers() {
        loading = true;
        model.setRowCount(0);

        DefaultTableModel data = customerDAO.getAllCustomers();

        for (int i = 0; i < data.getRowCount(); i++) {
            model.addRow(new Object[]{
                data.getValueAt(i, 0),
                data.getValueAt(i, 1),
                data.getValueAt(i, 2),
                data.getValueAt(i, 3),
                data.getValueAt(i, 4),
                data.getValueAt(i, 5)
            });
        }

        hideIdColumn();
        loading = false;
        applySearch();
    }

    private void loadEmployees() {
        loading = true;
        model.setRowCount(0);

        DefaultTableModel data = employeeDAO.getAllEmployees();

        for (int i = 0; i < data.getRowCount(); i++) {
            model.addRow(new Object[]{
                data.getValueAt(i, 0),
                data.getValueAt(i, 1),
                data.getValueAt(i, 2),
                data.getValueAt(i, 3),
                data.getValueAt(i, 4),
                data.getValueAt(i, 5)
            });
        }

        hideIdColumn();
        loading = false;
        applySearch();
    }

    private void loadServices() {
        loading = true;
        model.setRowCount(0);

        DefaultTableModel data = serviceDAO.getAllServices();

        for (int i = 0; i < data.getRowCount(); i++) {
            model.addRow(new Object[]{
                data.getValueAt(i, 0),
                data.getValueAt(i, 1),
                data.getValueAt(i, 2),
                data.getValueAt(i, 3),
                data.getValueAt(i, 4),
                data.getValueAt(i, 5),
                data.getValueAt(i, 6)
            });
        }

        hideIdColumn();
        loading = false;
        applySearch();
    }

    private void loadBookingsFiltered() {
        if (!module.equals("Lịch chụp") || loading) {
            return;
        }

        LocalDate selectedDate = null;
        String dateText = dateFilter.getText().trim();

        if (!dateText.isEmpty()) {
            try {
                selectedDate = LocalDate.parse(dateText);
                dateFilter.setBorder(UIManager.getBorder("TextField.border"));
            } catch (DateTimeParseException e) {
                dateFilter.setBorder(
                    BorderFactory.createLineBorder(Color.RED)
                );
                return;
            }
        } else {
            dateFilter.setBorder(UIManager.getBorder("TextField.border"));
        }

        String status = (String) statusFilter.getSelectedItem();

        DefaultTableModel data = bookingDAO.getBookings(
            search.getText().trim(),
            selectedDate,
            status
        );

        loading = true;
        model.setRowCount(0);

        for (int i = 0; i < data.getRowCount(); i++) {
            model.addRow(new Object[]{
                data.getValueAt(i, 0),
                data.getValueAt(i, 1),
                data.getValueAt(i, 2),
                data.getValueAt(i, 3),
                data.getValueAt(i, 4),
                data.getValueAt(i, 5),
                data.getValueAt(i, 6),
                data.getValueAt(i, 7)
            });
        }

        hideIdColumn();
        loading = false;
    }

    private void hideIdColumn() {
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);

            if (column.getModelIndex() == 0) {
                table.removeColumn(column);
                return;
            }
        }
    }

    // =========================
    // THÊM DỮ LIỆU
    // =========================

    private void addRecord() {
        switch (module) {
            case "Khách hàng" -> addCustomer();
            case "Nhân viên" -> addEmployee();
            case "Dịch vụ chụp" -> addService();
            case "Lịch chụp" -> addBooking();
            default -> addSampleRecord();
        }
    }

    private void addSampleRecord() {
        JTextField[] fields = new JTextField[model.getColumnCount()];
        JPanel form = new JPanel(
            new GridLayout(fields.length, 2, 8, 8)
        );

        for (int i = 0; i < fields.length; i++) {
            form.add(new JLabel(model.getColumnName(i)));
            fields[i] = new JTextField();
            form.add(fields[i]);
        }

        int result = JOptionPane.showConfirmDialog(
            this, form, "Thêm " + module,
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            Object[] row = new Object[fields.length];

            for (int i = 0; i < fields.length; i++) {
                row[i] = fields[i].getText().trim();
            }

            model.addRow(row);
        }
    }

    private void addCustomer() {
        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        JTextField address = new JTextField();
        JTextField note = new JTextField();

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));

        addField(form, "Mã khách hàng:", code);
        addField(form, "Họ và tên:", name);
        addField(form, "Số điện thoại:", phone);
        addField(form, "Email:", email);
        addField(form, "Địa chỉ:", address);
        addField(form, "Ghi chú:", note);

        int result = JOptionPane.showConfirmDialog(
            this, form, "Thêm khách hàng",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (code.getText().trim().isEmpty()
                || name.getText().trim().isEmpty()
                || phone.getText().trim().isEmpty()) {
            showError("Mã khách, họ tên và số điện thoại không được để trống.");
            return;
        }

        boolean success = customerDAO.addCustomer(
            code.getText().trim(),
            name.getText().trim(),
            phone.getText().trim(),
            email.getText().trim(),
            address.getText().trim(),
            note.getText().trim()
        );

        if (success) {
            showSuccess("Thêm khách hàng thành công!");
            loadCustomers();
        } else {
            showError("Không thể thêm khách hàng. Kiểm tra mã hoặc số điện thoại.");
        }
    }

    private void addEmployee() {
        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField role = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();

        JComboBox<String> status = new JComboBox<>(
            new String[]{"Đang làm", "Tạm nghỉ", "Đã nghỉ"}
        );

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));

        addField(form, "Mã nhân viên:", code);
        addField(form, "Họ và tên:", name);
        addField(form, "Vai trò:", role);
        addField(form, "Số điện thoại:", phone);
        addField(form, "Email:", email);
        addField(form, "Trạng thái:", status);

        int result = JOptionPane.showConfirmDialog(
            this, form, "Thêm nhân viên",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (code.getText().trim().isEmpty()
                || name.getText().trim().isEmpty()) {
            showError("Mã nhân viên và họ tên không được để trống.");
            return;
        }

        boolean success = employeeDAO.addEmployee(
            code.getText().trim(),
            name.getText().trim(),
            role.getText().trim(),
            phone.getText().trim(),
            email.getText().trim(),
            status.getSelectedItem().toString()
        );

        if (success) {
            showSuccess("Thêm nhân viên thành công!");
            loadEmployees();
        } else {
            showError("Không thể thêm nhân viên. Kiểm tra mã nhân viên.");
        }
    }

    private void addService() {
        JTextField code = new JTextField();
        JTextField name = new JTextField();
        JTextField price = new JTextField();
        JTextField duration = new JTextField();
        JTextField description = new JTextField();

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));

        addField(form, "Mã dịch vụ:", code);
        addField(form, "Tên dịch vụ:", name);
        addField(form, "Giá (VNĐ):", price);
        addField(form, "Thời lượng (phút):", duration);
        addField(form, "Mô tả:", description);

        int result = JOptionPane.showConfirmDialog(
            this, form, "Thêm dịch vụ chụp",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            if (code.getText().trim().isEmpty()
                    || name.getText().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Mã dịch vụ và tên dịch vụ không được để trống."
                );
            }

            double p = Double.parseDouble(price.getText().trim());
            int d = Integer.parseInt(duration.getText().trim());

            if (p < 0 || d <= 0) {
                throw new IllegalArgumentException(
                    "Giá phải từ 0 trở lên và thời lượng phải lớn hơn 0."
                );
            }

            boolean success = serviceDAO.addService(
                code.getText().trim(),
                name.getText().trim(),
                p,
                d,
                description.getText().trim()
            );

            if (success) {
                showSuccess("Thêm dịch vụ thành công!");
                loadServices();
            } else {
                showError("Không thể thêm dịch vụ. Kiểm tra mã dịch vụ.");
            }

        } catch (NumberFormatException e) {
            showError("Giá và thời lượng phải là số hợp lệ.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    // =========================
    // THÊM LỊCH CHỤP
    // =========================

    private void addBooking() {
        BookingForm form = createBookingForm(null);

        if (form == null) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            form.panel,
            "Thêm lịch chụp",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            BookingInput input = readBookingForm(form);

            if (input.employeeId != null
                    && isActiveBookingStatus(input.status)
                    && bookingDAO.hasEmployeeConflict(
                        input.employeeId,
                        input.serviceId,
                        input.scheduledAt,
                        null
                    )) {
                showError(
                    "Nhân viên này đã có lịch chụp bị trùng thời gian.\n"
                    + "Hãy chọn nhân viên khác hoặc đổi thời gian."
                );
                return;
            }

            String bookingCode = "LC" + System.currentTimeMillis();

            boolean success = bookingDAO.addBooking(
                bookingCode,
                input.customerId,
                input.serviceId,
                input.employeeId,
                input.scheduledAt,
                input.location,
                input.status,
                input.note
            );

            if (success) {
                showSuccess("Thêm lịch chụp thành công!");
                loadBookingsFiltered();
            } else {
                showError(
                    "Không thể thêm lịch chụp. "
                    + "Có thể dữ liệu không hợp lệ hoặc lịch bị trùng."
                );
            }

        } catch (DateTimeParseException e) {
            showError(
                "Ngày hoặc giờ không hợp lệ.\n"
                + "Ngày: yyyy-MM-dd, ví dụ 2026-11-20\n"
                + "Giờ: HH:mm, ví dụ 09:30"
            );
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // =========================
    // FORM LỊCH CHỤP DÙNG CHUNG
    // =========================

    private static class ComboItem {
        private final int id;
        private final String name;

        public ComboItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class BookingForm {
        JPanel panel;
        JComboBox<ComboItem> customerBox;
        JComboBox<ComboItem> serviceBox;
        JComboBox<ComboItem> employeeBox;
        JTextField date;
        JTextField time;
        JTextField location;
        JComboBox<String> status;
        JTextField note;
    }

    private static class BookingInput {
        int customerId;
        int serviceId;
        Integer employeeId;
        LocalDateTime scheduledAt;
        String location;
        String status;
        String note;
    }

    private BookingForm createBookingForm(BookingInfo info) {

        DefaultTableModel customers = bookingDAO.getCustomerOptions();
        DefaultTableModel services = bookingDAO.getServiceOptions();
        DefaultTableModel employees = bookingDAO.getEmployeeOptions();

        if (customers.getRowCount() == 0) {
            showError("Chưa có khách hàng. Hãy thêm khách hàng trước.");
            return null;
        }

        if (services.getRowCount() == 0) {
            showError("Chưa có dịch vụ đang hoạt động.");
            return null;
        }

        BookingForm form = new BookingForm();

        form.customerBox = new JComboBox<>();
        form.serviceBox = new JComboBox<>();
        form.employeeBox = new JComboBox<>();

        for (int i = 0; i < customers.getRowCount(); i++) {
            form.customerBox.addItem(new ComboItem(
                Integer.parseInt(customers.getValueAt(i, 0).toString()),
                customers.getValueAt(i, 1).toString()
            ));
        }

        for (int i = 0; i < services.getRowCount(); i++) {
            form.serviceBox.addItem(new ComboItem(
                Integer.parseInt(services.getValueAt(i, 0).toString()),
                services.getValueAt(i, 1).toString()
            ));
        }

        form.employeeBox.addItem(new ComboItem(0, "Không phân công"));

        for (int i = 0; i < employees.getRowCount(); i++) {
            form.employeeBox.addItem(new ComboItem(
                Integer.parseInt(employees.getValueAt(i, 0).toString()),
                employees.getValueAt(i, 1).toString()
            ));
        }

        form.date = new JTextField(
            info == null
                ? LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                : info.scheduledAt.toLocalDate().toString()
        );

        form.time = new JTextField(
            info == null
                ? "09:00"
                : info.scheduledAt.toLocalTime().format(
                    DateTimeFormatter.ofPattern("HH:mm")
                )
        );

        form.location = new JTextField(
            info == null ? "" : safe(info.location)
        );

        form.status = new JComboBox<>(BOOKING_STATUSES);

        form.note = new JTextField(
            info == null ? "" : safe(info.note)
        );

        if (info == null) {
            form.status.setSelectedItem("Chờ xác nhận");
        } else {
            selectComboItem(form.customerBox, info.customerId);
            selectComboItem(form.serviceBox, info.serviceId);

            if (info.employeeId != null) {
                addMissingEmployeeIfNeeded(
                    form.employeeBox,
                    info.employeeId
                );
                selectComboItem(form.employeeBox, info.employeeId);
            }

            form.status.setSelectedItem(info.status);
        }

        JPanel fields = new JPanel(new GridLayout(8, 2, 8, 8));

        addField(fields, "Khách hàng:", form.customerBox);
        addField(fields, "Dịch vụ:", form.serviceBox);
        addField(fields, "Nhân viên:", form.employeeBox);
        addField(fields, "Ngày chụp (yyyy-MM-dd):", form.date);
        addField(fields, "Giờ chụp (HH:mm):", form.time);
        addField(fields, "Địa điểm:", form.location);
        addField(fields, "Trạng thái:", form.status);
        addField(fields, "Ghi chú:", form.note);

        form.panel = fields;

        return form;
    }

    private void addMissingEmployeeIfNeeded(
            JComboBox<ComboItem> box,
            int employeeId) {

        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).id == employeeId) {
                return;
            }
        }

        // Nhân viên cũ có thể đã chuyển sang trạng thái nghỉ.
        // Vẫn giữ lựa chọn hiện tại để không làm mất dữ liệu lịch.
        DefaultTableModel allEmployees = employeeDAO.getAllEmployees();

        for (int i = 0; i < allEmployees.getRowCount(); i++) {
            int id = Integer.parseInt(
                allEmployees.getValueAt(i, 0).toString()
            );

            if (id == employeeId) {
                box.addItem(new ComboItem(
                    id,
                    allEmployees.getValueAt(i, 2).toString()
                ));
                return;
            }
        }
    }

    private void selectComboItem(
            JComboBox<ComboItem> box,
            int id) {

        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).id == id) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private BookingInput readBookingForm(BookingForm form) {

        ComboItem customer =
            (ComboItem) form.customerBox.getSelectedItem();

        ComboItem service =
            (ComboItem) form.serviceBox.getSelectedItem();

        ComboItem employee =
            (ComboItem) form.employeeBox.getSelectedItem();

        if (customer == null || service == null) {
            throw new IllegalArgumentException(
                "Hãy chọn khách hàng và dịch vụ."
            );
        }

        if (form.location.getText().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Địa điểm chụp không được để trống."
            );
        }

        LocalDate selectedDate = LocalDate.parse(
            form.date.getText().trim()
        );

        LocalTime selectedTime = LocalTime.parse(
            form.time.getText().trim()
        );

        BookingInput input = new BookingInput();

        input.customerId = customer.id;
        input.serviceId = service.id;
        input.employeeId =
            employee == null || employee.id == 0
                ? null
                : employee.id;

        input.scheduledAt = LocalDateTime.of(
            selectedDate, selectedTime
        );

        input.location = form.location.getText().trim();
        input.status = form.status.getSelectedItem().toString();
        input.note = form.note.getText().trim();

        return input;
    }

    private boolean isActiveBookingStatus(String status) {
        return !status.equals("Đã hủy")
            && !status.equals("Đã hoàn thành");
    }

    // =========================
    // SỬA DỮ LIỆU
    // =========================

    private void editRecord() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            showError("Hãy chọn một dòng trước.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);

        switch (module) {
            case "Nhân viên" -> editEmployee(row);
            case "Dịch vụ chụp" -> editService(row);
            case "Lịch chụp" -> editBooking(row);
            default -> showError(
                "Chức năng sửa chưa được triển khai cho mục này."
            );
        }
    }

    private void editEmployee(int row) {
        int id = ((Number) model.getValueAt(row, 0)).intValue();

        JTextField name = new JTextField(
            safe(model.getValueAt(row, 2))
        );

        JTextField role = new JTextField(
            safe(model.getValueAt(row, 3))
        );

        JTextField phone = new JTextField(
            safe(model.getValueAt(row, 4))
        );

        JComboBox<String> status = new JComboBox<>(
            new String[]{"Đang làm", "Tạm nghỉ", "Đã nghỉ"}
        );

        status.setSelectedItem(model.getValueAt(row, 5));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));

        addField(form, "Họ và tên:", name);
        addField(form, "Vai trò:", role);
        addField(form, "Số điện thoại:", phone);
        addField(form, "Trạng thái:", status);

        int result = JOptionPane.showConfirmDialog(
            this, form, "Sửa thông tin nhân viên",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (name.getText().trim().isEmpty()) {
            showError("Họ tên không được để trống.");
            return;
        }

        boolean success = employeeDAO.updateEmployee(
            id,
            name.getText().trim(),
            role.getText().trim(),
            phone.getText().trim(),
            "",
            status.getSelectedItem().toString()
        );

        if (success) {
            showSuccess("Cập nhật nhân viên thành công!");
            loadEmployees();
        } else {
            showError("Không thể cập nhật nhân viên.");
        }
    }

    private void editService(int row) {
        int id = ((Number) model.getValueAt(row, 0)).intValue();

        JTextField name = new JTextField(
            safe(model.getValueAt(row, 2))
        );

        JTextField price = new JTextField(
            safe(model.getValueAt(row, 3))
        );

        JTextField duration = new JTextField(
            safe(model.getValueAt(row, 4))
        );

        JTextField description = new JTextField(
            safe(model.getValueAt(row, 5))
        );

        JComboBox<String> status = new JComboBox<>(
            new String[]{"Đang hoạt động", "Ngừng hoạt động"}
        );

        status.setSelectedItem(model.getValueAt(row, 6));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));

        addField(form, "Tên dịch vụ:", name);
        addField(form, "Giá (VNĐ):", price);
        addField(form, "Thời lượng (phút):", duration);
        addField(form, "Mô tả:", description);
        addField(form, "Trạng thái:", status);

        int result = JOptionPane.showConfirmDialog(
            this, form, "Sửa dịch vụ chụp",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            if (name.getText().trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Tên dịch vụ không được để trống."
                );
            }

            double p = Double.parseDouble(price.getText().trim());
            int d = Integer.parseInt(duration.getText().trim());

            if (p < 0 || d <= 0) {
                throw new IllegalArgumentException(
                    "Giá phải từ 0 trở lên và thời lượng phải lớn hơn 0."
                );
            }

            boolean active = status.getSelectedIndex() == 0;

            boolean success = serviceDAO.updateService(
                id,
                name.getText().trim(),
                p,
                d,
                description.getText().trim(),
                active
            );

            if (success) {
                showSuccess("Cập nhật dịch vụ thành công!");
                loadServices();
            } else {
                showError("Không thể cập nhật dịch vụ.");
            }

        } catch (NumberFormatException e) {
            showError("Giá và thời lượng phải là số hợp lệ.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void editBooking(int row) {
        int id = ((Number) model.getValueAt(row, 0)).intValue();

        BookingInfo info = bookingDAO.getBookingById(id);

        if (info == null) {
            showError("Không tìm thấy thông tin lịch chụp.");
            return;
        }

        BookingForm form = createBookingForm(info);

        if (form == null) {
            return;
        }

        int result = JOptionPane.showConfirmDialog(
            this,
            form.panel,
            "Chỉnh sửa lịch chụp " + info.bookingCode,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            BookingInput input = readBookingForm(form);

            if (input.employeeId != null
                    && isActiveBookingStatus(input.status)
                    && bookingDAO.hasEmployeeConflict(
                        input.employeeId,
                        input.serviceId,
                        input.scheduledAt,
                        id
                    )) {
                showError(
                    "Nhân viên này đã có lịch chụp bị trùng thời gian.\n"
                    + "Hãy chọn nhân viên khác hoặc đổi thời gian."
                );
                return;
            }

            boolean success = bookingDAO.updateBooking(
                id,
                input.customerId,
                input.serviceId,
                input.employeeId,
                input.scheduledAt,
                input.location,
                input.status,
                input.note
            );

            if (success) {
                showSuccess("Cập nhật lịch chụp thành công!");
                loadBookingsFiltered();
            } else {
                showError(
                    "Không thể cập nhật lịch chụp. "
                    + "Kiểm tra dữ liệu hoặc lịch bị trùng."
                );
            }

        } catch (DateTimeParseException e) {
            showError(
                "Ngày hoặc giờ không hợp lệ.\n"
                + "Ngày: yyyy-MM-dd\n"
                + "Giờ: HH:mm"
            );
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (RuntimeException e) {
            showError(e.getMessage());
        }
    }

    // =========================
    // XÓA DỮ LIỆU
    // =========================

    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            showError("Hãy chọn một dòng trước.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);

        if (module.equals("Khách hàng")) {
            int id = ((Number) model.getValueAt(row, 0)).intValue();

            if (confirmDelete("Mày có chắc muốn xóa khách hàng này?")) {
                if (customerDAO.deleteCustomer(id)) {
                    showSuccess("Đã xóa khách hàng!");
                    loadCustomers();
                } else {
                    showError(
                        "Không thể xóa. Khách hàng có thể đang liên kết với dữ liệu khác."
                    );
                }
            }

        } else if (module.equals("Nhân viên")) {
            int id = ((Number) model.getValueAt(row, 0)).intValue();

            if (confirmDelete("Mày có chắc muốn xóa nhân viên này?")) {
                if (employeeDAO.deleteEmployee(id)) {
                    showSuccess("Đã xóa nhân viên!");
                    loadEmployees();
                } else {
                    showError(
                        "Không thể xóa nhân viên. "
                        + "Có thể nhân viên đang liên kết với dữ liệu khác."
                    );
                }
            }

        } else if (module.equals("Dịch vụ chụp")) {
            int id = ((Number) model.getValueAt(row, 0)).intValue();

            if (confirmDelete("Mày có chắc muốn xóa dịch vụ này?")) {
                if (serviceDAO.deleteService(id)) {
                    showSuccess("Đã xóa dịch vụ!");
                    loadServices();
                } else {
                    showError(
                        "Không thể xóa dịch vụ. "
                        + "Có thể dịch vụ đang được sử dụng."
                    );
                }
            }

        } else if (module.equals("Lịch chụp")) {
            int id = ((Number) model.getValueAt(row, 0)).intValue();

            if (confirmDelete("Mày có chắc muốn xóa lịch chụp này?")) {
                if (bookingDAO.deleteBooking(id)) {
                    showSuccess("Đã xóa lịch chụp!");
                    loadBookingsFiltered();
                } else {
                    showError("Không thể xóa lịch chụp.");
                }
            }

        } else {
            model.removeRow(row);
        }
    }

    private boolean confirmDelete(String message) {
        return JOptionPane.showConfirmDialog(
            this,
            message,
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
    }

    // =========================
    // TIỆN ÍCH GIAO DIỆN
    // =========================

    private void addField(
            JPanel panel,
            String label,
            Component component) {

        panel.add(new JLabel(label));
        panel.add(component);
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Thông báo",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "Thông báo",
            JOptionPane.WARNING_MESSAGE
        );
    }

    // =========================
    // CẤU HÌNH BẢNG
    // =========================

    private String[] columnsFor(String m) {
        return switch (m) {
            case "Khách hàng" -> new String[]{
                "ID", "Mã khách", "Họ tên",
                "Số điện thoại", "Email", "Địa chỉ"
            };

            case "Nhân viên" -> new String[]{
                "ID", "Mã nhân viên", "Họ tên",
                "Vai trò", "Số điện thoại", "Trạng thái"
            };

            case "Dịch vụ chụp" -> new String[]{
                "ID", "Mã dịch vụ", "Tên dịch vụ",
                "Giá", "Thời lượng", "Mô tả", "Trạng thái"
            };

            case "Lịch chụp" -> new String[]{
                "ID", "Mã lịch", "Khách hàng", "Dịch vụ",
                "Nhân viên", "Thời gian", "Địa điểm", "Trạng thái"
            };

            case "Đơn chụp" -> new String[]{
                "Mã đơn", "Khách hàng", "Ngày tạo",
                "Tổng tiền", "Trạng thái"
            };

            case "Thanh toán" -> new String[]{
                "Mã thanh toán", "Mã đơn",
                "Ngày thanh toán", "Số tiền", "Phương thức"
            };

            case "Giao ảnh" -> new String[]{
                "Mã giao ảnh", "Mã đơn", "Ngày giao",
                "Hình thức", "Trạng thái"
            };

            default -> new String[]{
                "Mã", "Tên", "Thông tin", "Ngày tạo", "Trạng thái"
            };
        };
    }

    private Object[][] sampleFor(String m) {
        return switch (m) {
            case "Đơn chụp" -> new Object[][]{
                {"DC001", "Nguyễn Văn An", "17/11/2026", "1500000", "Đang xử lý"}
            };

            case "Thanh toán" -> new Object[][]{
                {"TT001", "DC001", "17/11/2026", "750000", "Chuyển khoản"}
            };

            case "Giao ảnh" -> new Object[][]{
                {"GA001", "DC001", "21/11/2026", "Google Drive", "Đang chuẩn bị"}
            };

            default -> new Object[][]{
                {"—", "Chưa có dữ liệu thật", "Dữ liệu minh họa", "—", "—"}
            };
        };
    }
}
