
USE huythanh_studio;

-- 1. KHACH HANG
CREATE TABLE customers (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100),
    address VARCHAR(255),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. NHAN VIEN
CREATE TABLE employees (
    employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status ENUM('Đang làm', 'Tạm nghỉ', 'Đã nghỉ')
        DEFAULT 'Đang làm',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 3. DICH VU
CREATE TABLE services (
    service_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_code VARCHAR(20) NOT NULL UNIQUE,
    service_name VARCHAR(150) NOT NULL,
    price DECIMAL(15,2) NOT NULL DEFAULT 0,
    duration_minutes INT DEFAULT 0,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. LICH DAT
CREATE TABLE bookings (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    employee_id BIGINT,
    scheduled_at DATETIME NOT NULL,
    location VARCHAR(255),
    status ENUM(
        'Chờ xác nhận',
        'Đã xác nhận',
        'Đã hoàn thành',
        'Đã hủy'
    ) DEFAULT 'Chờ xác nhận',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bookings_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),

    CONSTRAINT fk_bookings_service
        FOREIGN KEY (service_id)
        REFERENCES services(service_id),

    CONSTRAINT fk_bookings_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
) ENGINE=InnoDB;

-- 5. DON HANG
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(20) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    booking_id BIGINT,
    created_by BIGINT,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(15,2) DEFAULT 0,
    discount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) DEFAULT 0,
    status ENUM(
        'Mới tạo',
        'Đang thực hiện',
        'Chờ giao ảnh',
        'Hoàn thành',
        'Đã hủy'
    ) DEFAULT 'Mới tạo',
    note TEXT,

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id),

    CONSTRAINT fk_orders_booking
        FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id),

    CONSTRAINT fk_orders_employee
        FOREIGN KEY (created_by)
        REFERENCES employees(employee_id)
) ENGINE=InnoDB;

-- 6. CHI TIET DON HANG
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_service
        FOREIGN KEY (service_id)
        REFERENCES services(service_id)
) ENGINE=InnoDB;

-- 7. THANH TOAN
CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_code VARCHAR(20) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    paid_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(15,2) NOT NULL,
    method ENUM(
        'Tiền mặt',
        'Chuyển khoản',
        'Thẻ',
        'Khác'
    ) DEFAULT 'Tiền mặt',
    status ENUM(
        'Đã thanh toán',
        'Chờ xử lý',
        'Hoàn tiền'
    ) DEFAULT 'Đã thanh toán',
    reference_note VARCHAR(255),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. BAN GIAO ANH
CREATE TABLE deliveries (
    delivery_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_code VARCHAR(20) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    delivery_date DATETIME,
    method ENUM(
        'Google Drive',
        'USB',
        'Trực tiếp',
        'Khác'
    ) DEFAULT 'Google Drive',
    delivery_link TEXT,
    status ENUM(
        'Đang chuẩn bị',
        'Đã gửi',
        'Khách đã nhận'
    ) DEFAULT 'Đang chuẩn bị',
    note TEXT,

    CONSTRAINT fk_deliveries_order
        FOREIGN KEY (order_id)
        REFERENCES orders(order_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. ANH MAU / THU VIEN
CREATE TABLE gallery (
    gallery_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    image_path TEXT,
    description TEXT,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- KIEM TRA CAC BANG DA TAO
SHOW TABLES;
