
USE huythanh_studio;

-- =========================================
-- 1. DU LIEU MAU KHACH HANG
-- =========================================

INSERT INTO customers
(customer_code, full_name, phone, email, address, note)
VALUES
('KH001', 'Nguyễn Minh Anh', '0988123001', 'minhanh@gmail.com', 'Hà Nội', 'Khách hàng chụp ảnh cá nhân'),
('KH002', 'Trần Hoàng Nam', '0988123002', 'hoangnam@gmail.com', 'Cầu Giấy, Hà Nội', 'Khách hàng chụp ảnh kỷ yếu'),
('KH003', 'Lê Thu Hà', '0988123003', 'thuha@gmail.com', 'Đống Đa, Hà Nội', 'Khách hàng chụp ảnh chân dung'),
('KH004', 'Phạm Đức Anh', '0988123004', 'ducanh@gmail.com', 'Hai Bà Trưng, Hà Nội', 'Khách hàng thân thiết'),
('KH005', 'Vũ Ngọc Mai', '0988123005', 'ngocmai@gmail.com', 'Thanh Xuân, Hà Nội', 'Khách hàng mới');

-- =========================================
-- 2. DU LIEU MAU NHAN VIEN
-- =========================================

INSERT INTO employees
(employee_code, full_name, role, phone, email, status)
VALUES
('NV001', 'Nguyễn Huy Thành', 'Quản lý', '0912000001', 'admin@huythanhstudio.vn', 'Đang làm'),
('NV002', 'Trần Minh Quân', 'Nhiếp ảnh gia', '0912000002', 'quan@huythanhstudio.vn', 'Đang làm'),
('NV003', 'Lê Hoàng Long', 'Hậu kỳ ảnh', '0912000003', 'long@huythanhstudio.vn', 'Đang làm'),
('NV004', 'Phạm Thu Trang', 'Tư vấn viên', '0912000004', 'trang@huythanhstudio.vn', 'Đang làm');

-- =========================================
-- 3. DU LIEU MAU DICH VU
-- =========================================

INSERT INTO services
(service_code, service_name, price, duration_minutes, description, is_active)
VALUES
('DV001', 'Chụp ảnh chân dung', 500000, 60, 'Chụp ảnh chân dung cá nhân', TRUE),
('DV002', 'Chụp ảnh kỷ yếu', 1500000, 120, 'Chụp ảnh kỷ yếu tập thể', TRUE),
('DV003', 'Chụp ảnh sự kiện', 2000000, 180, 'Chụp ảnh sự kiện và chương trình', TRUE),
('DV004', 'Chụp ảnh sản phẩm', 800000, 90, 'Chụp ảnh sản phẩm thương mại', TRUE),
('DV005', 'Chỉnh sửa ảnh', 200000, 30, 'Dịch vụ hậu kỳ và chỉnh sửa ảnh', TRUE),
('DV006', 'Chụp ảnh cá nhân ngoài trời', 700000, 90, 'Chụp ảnh ngoại cảnh cá nhân', TRUE);

-- =========================================
-- 4. DU LIEU MAU ANH THU VIEN
-- =========================================

INSERT INTO gallery
(title, category, image_path, description, is_visible)
VALUES
('Chân dung cá nhân', 'Chân dung', 'images/portrait.jpg', 'Bộ ảnh chân dung cá nhân', TRUE),
('Thanh xuân học đường', 'Kỷ yếu', 'images/yearbook.jpg', 'Bộ ảnh kỷ yếu và thanh xuân', TRUE),
('Ảnh sự kiện', 'Sự kiện', 'images/event.jpg', 'Bộ ảnh sự kiện', TRUE),
('Ảnh sản phẩm', 'Sản phẩm', 'images/product.jpg', 'Bộ ảnh sản phẩm studio', TRUE);

-- =========================================
-- 5. KIEM TRA DU LIEU
-- =========================================

SELECT * FROM customers;
SELECT * FROM employees;
SELECT * FROM services;
SELECT * FROM gallery;
