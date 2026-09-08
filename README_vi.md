# JTrac (Bản Fork Nâng Cấp)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](pom.xml)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Dự án này là phiên bản fork hiện đại hóa và nâng cấp từ [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Dự án hướng đến việc cung cấp một hệ thống theo dõi vấn đề (Issue Tracking) gọn nhẹ, tương thích cao, hỗ trợ lưu trữ tĩnh ngoại tuyến và giao diện hiện đại. Quá trình phát triển được điều phối theo quy chuẩn OpenSpec v1.12.0 với sự hỗ trợ của Antigravity 1.1.27 trong việc tái cấu trúc, kiểm thử và đảm bảo chất lượng.

---

## Hướng Dẫn Biên Dịch Đa Ngôn Ngữ / Multilingual Build Guides

| Ngôn ngữ / Language | Hướng dẫn / Build Guide |
|---|---|
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |

---

## Các Cập Nhật Quan Trọng (Changelog & Major Updates)

### 1. Dọn dẹp mô-đun: Loại bỏ hoàn toàn Wiki (Removed Wiki Module)
- Loại bỏ hoàn toàn mã nguồn, trang giao diện và biểu tượng menu của mô-đun Wiki cũ để tinh giản hệ thống, tập trung tối đa vào hiệu năng theo dõi vấn đề cốt lõi.

### 2. Tính năng mới (New Features)
- **Xuất HTML hàng loạt và tải ZIP trực tiếp trên Web**:
  - Tích hợp tính năng "Xuất HTML" trên thanh điều hướng chính. Cho phép người dùng chọn nhiều không gian dự án (hỗ trợ "Chọn tất cả / Bỏ chọn tất cả") và tải về tệp nén ZIP chứa toàn bộ lịch sử thảo luận HTML cùng tệp đính kèm.
- **Kiểm soát phân quyền dự án nghiêm ngặt (Permission Guardrails)**:
  - Bảo vệ hai lớp ở cả phía máy chủ và giao diện: người dùng thông thường chỉ có thể thấy, chọn và tải dữ liệu từ các dự án mình có quyền thành viên (quản trị viên hệ thống giữ toàn quyền). Bổ sung cơ chế chặn khi danh sách rỗng và thông báo thân thiện.
- **Công cụ dòng lệnh xuất HTML độc lập (`tools/jtrac-exporter.jar`)**:
  - Không cần khởi động máy chủ Web, kết nối trực tiếp qua JDBC để xuất toàn bộ dữ liệu ra báo cáo HTML phản hồi nhanh và đa ngôn ngữ.
  - Tích hợp chế độ nền tối (Dark Mode toggle) 100% ngoại tuyến bằng CSS thuần, hoàn toàn không phụ thuộc vào CDN bên ngoài hay Internet.
- **Hoàn thiện bản địa hóa đa ngôn ngữ (Full i18n Coverage)**:
  - Hiệu đính và bổ sung đầy đủ bản dịch tiếng Trung phồn thể (`zh_TW`) và giản thể (`zh_CN`).
  - Bổ sung các nhãn còn thiếu trên tất cả các ngôn ngữ (như trạng thái kích hoạt không gian `space_form.isActive`).

### 3. Sửa lỗi (Bug Fixes)
- **Căn giữa các nút và nhãn trên thanh điều hướng (Navigation Header Centering)**:
  - Khắc phục lỗi lệch trục thẳng đứng và bị ép sang phải của các nút như "Bảng điều khiển", "Tìm kiếm", "Xuất HTML", "Tùy chọn", "Đăng xuất", tên tài khoản.
  - Áp dụng cấu trúc Flexbox inline-flex căn giữa hoàn hảo cả chiều ngang và chiều dọc, bổ sung bo góc viền 3px, hiệu ứng đổi màu khi rê chuột (hover) và phân tách nhãn thông tin.
- **Tối ưu lịch sử luồng thảo luận (Thread History Cleanup)**:
  - Loại bỏ bản ghi trạng thái "Open" ban đầu không có ghi chú hoặc thay đổi thực tế khi xuất HTML, chỉ giữ lại các nội dung thảo luận và biến động trạng thái có ý nghĩa.
- **Xử lý dứt điểm lỗi Hibernate `LazyInitializationException`**:
  - Khắc phục lỗi đóng phiên truy cập (Session) khi tải siêu dữ liệu không gian (`space.metadata`) bằng phương thức tải trước tích cực (Eager Fetch / Initialize).
- **Sửa lỗi lọc tài nguyên Maven UTF-8 và hỏng tệp nhị phân**:
  - Loại bỏ phương thức `native2ascii` cũ, chuẩn hóa 100% mã hóa UTF-8.
  - Sửa lỗi cấu hình Maven lọc tài nguyên khiến các tệp hình ảnh nhị phân (gif, png, jar) bị lỗi khi đóng gói.

---

## Công Nghệ & Kiến Trúc Phát Triển (Technologies & Architecture)

- **Ngôn ngữ chính**: Java 1.8 / 11
- **Khung Web**: Apache Wicket 1.3
- **IoC & Container**: Spring Framework 2.5
- **ORM & Cơ sở dữ liệu**: Hibernate 3 / HSQLDB tích hợp; hỗ trợ MySQL, PostgreSQL, MS SQL Server, Oracle
- **Công cụ đóng gói**: Apache Maven 3.9+ (định dạng WAR)
- **Công cụ hỗ trợ phát triển & quy chuẩn**: OpenSpec v1.12.0, Antigravity 1.1.27
- **Bộ mã ký tự**: 100% UTF-8

---

## Bắt Đầu Nhanh: Biên Dịch & Triển Khai (Quick Start)

### 1. Đóng gói ứng dụng chính (WAR)
```bash
# Biên dịch mã nguồn
mvn compile

# Đóng gói tệp WAR (bỏ qua kiểm thử)
mvn package -DskipTests
```
Tệp gói hoàn chỉnh nằm tại: `target/jtrac.war`, sẵn sàng triển khai trên Jetty hoặc Tomcat.

### 2. Đóng gói và chạy công cụ xuất HTML (CLI)
```bash
# Đóng gói tệp JAR thực thi độc lập
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Kết quả: tools/jtrac-exporter.jar

# Ví dụ chạy xuất dữ liệu từ HSQLDB cục bộ
java -jar tools/jtrac-exporter.jar \
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" \
  --attachments-dir="./data/attachments" \
  --out="./export-output" \
  --lang=vi
```

---

## Tài Liệu & Quy Chuẩn Dự Án (Specifications)

Dự án phát triển dựa trên tài liệu đặc tả OpenSpec:
- [Mục lục thông số kỹ thuật](openspec/specs/README.md)
- [Đặc tả công cụ xuất HTML](openspec/specs/html-exporter/spec.md)
- [Đặc tả tài nguyên đa ngôn ngữ](openspec/specs/i18n-resources/spec.md)
- [Đặc tả tài liệu biên dịch](openspec/specs/build-documentation/spec.md)
- [Quy tắc phát triển](.agents/AGENTS.md)

---

## Giấy Phép (License)

JTrac là phần mềm mã nguồn mở phát hành theo [Giấy phép Phần mềm Apache, Phiên bản 2.0](license.txt).
