
USE huythanh_studio;

-- =========================================
-- 1. DANH SACH KHACH HANG
-- =========================================

SELECT
    customer_id,
    customer_code,
    full_name,
    phone,
    email,
    address
FROM customers
ORDER BY customer_id DESC;


-- =========================================
-- 2. DANH SACH NHAN VIEN
-- =========================================

SELECT *
FROM employees
ORDER BY employee_id DESC;


-- =========================================
-- 3. DANH SACH DICH VU
-- =========================================

SELECT *
FROM services
WHERE is_active = TRUE
ORDER BY service_id DESC;


-- =========================================
-- 4. DANH SACH LICH DAT
-- =========================================

SELECT
    b.booking_code,
    c.full_name AS customer_name,
    s.service_name,
    e.full_name AS employee_name,
    b.scheduled_at,
    b.location,
    b.status
FROM bookings b
JOIN customers c ON b.customer_id = c.customer_id
JOIN services s ON b.service_id = s.service_id
LEFT JOIN employees e ON b.employee_id = e.employee_id
ORDER BY b.scheduled_at DESC;


-- =========================================
-- 5. DANH SACH DON HANG
-- =========================================

SELECT
    o.order_code,
    c.full_name AS customer_name,
    o.order_date,
    o.subtotal,
    o.discount,
    o.total_amount,
    o.status
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
ORDER BY o.order_date DESC;


-- =========================================
-- 6. TONG DOANH THU
-- Chi tinh don hang hoan thanh
-- =========================================

SELECT
    COALESCE(SUM(total_amount), 0) AS total_revenue
FROM orders
WHERE status = 'Hoàn thành';


-- =========================================
-- 7. DOANH THU THEO THANG
-- =========================================

SELECT
    YEAR(order_date) AS year,
    MONTH(order_date) AS month,
    SUM(total_amount) AS revenue
FROM orders
WHERE status = 'Hoàn thành'
GROUP BY YEAR(order_date), MONTH(order_date)
ORDER BY year DESC, month DESC;


-- =========================================
-- 8. THONG KE DON HANG THEO TRANG THAI
-- =========================================

SELECT
    status,
    COUNT(*) AS total_orders
FROM orders
GROUP BY status;


-- =========================================
-- 9. LICH SU THANH TOAN
-- =========================================

SELECT
    p.payment_code,
    o.order_code,
    c.full_name AS customer_name,
    p.paid_at,
    p.amount,
    p.method,
    p.status
FROM payments p
JOIN orders o ON p.order_id = o.order_id
JOIN customers c ON o.customer_id = c.customer_id
ORDER BY p.paid_at DESC;


-- =========================================
-- 10. DANH SACH BAN GIAO ANH
-- =========================================

SELECT
    d.delivery_code,
    o.order_code,
    c.full_name AS customer_name,
    d.delivery_date,
    d.method,
    d.delivery_link,
    d.status
FROM deliveries d
JOIN orders o ON d.order_id = o.order_id
JOIN customers c ON o.customer_id = c.customer_id
ORDER BY d.delivery_date DESC;


-- =========================================
-- 11. SO LUONG KHACH HANG
-- =========================================

SELECT COUNT(*) AS total_customers
FROM customers;


-- =========================================
-- 12. SO LUONG DICH VU
-- =========================================

SELECT COUNT(*) AS total_services
FROM services
WHERE is_active = TRUE;
