# HUYTHANH STUDIO – Java Swing starter project

## Yêu cầu
- JDK 17 trở lên
- Apache NetBeans có hỗ trợ Maven (hoặc Maven CLI)

## Mở project
1. Giải nén `HuyThanhStudio.zip`.
2. Trong NetBeans chọn **File > Open Project**.
3. Chọn thư mục `HuyThanhStudio` (thư mục có `pom.xml`).
4. Chờ Maven tải/cấu hình project, sau đó Run Project (F6).

## Những phần đã có
- MainFrame với sidebar và điều hướng các màn hình.
- Dashboard tổng quan, đồng hồ hệ thống.
- Thư viện Ảnh mẫu: chọn nhiều ảnh từ máy, xem ảnh lớn và xóa ảnh khỏi danh sách hiện tại.
- Các màn hình quản lý Khách hàng, Nhân viên, Dịch vụ, Lịch chụp, Đơn chụp, Thanh toán, Giao ảnh: bảng mẫu, tìm kiếm, thêm dòng và xóa dòng.
- Giao diện dùng Java Swing, không cần thư viện giao diện bên ngoài.

## Lưu ý quan trọng
Đây là **bản giao diện/prototype chạy độc lập**, chưa kết nối MySQL. Dữ liệu bảng hiện là dữ liệu mẫu và các thay đổi trong bảng chưa được lưu sau khi đóng ứng dụng. Ảnh được chọn trong màn Ảnh mẫu cũng chỉ tồn tại trong phiên chạy hiện tại; chưa sao chép vào thư mục dữ liệu hay lưu metadata vào database.

Bước tiếp theo để thành phần mềm quản lý thực sự là thống nhất schema MySQL, cấu hình kết nối, rồi triển khai DAO/CRUD và lưu trữ ảnh bền vững.
