# JTrac (Phiên bản Fork Nâng cao)

[English](README.md) | [繁體中文](README_zh-TW.md) | [简体中文](README_zh-CN.md) | [日本語](README_ja.md) | [Tiếng Việt](README_vi.md) | [Deutsch](README_de.md) | [Español](README_es.md) | [Français](README_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.0.0-blue.svg)](pom.xml)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)
[![OpenSpec](https://img.shields.io/badge/OpenSpec-v1.12.0-brightgreen.svg)](openspec/specs/README.md)

Dự án này bắt nguồn từ phiên bản [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Dự án nỗ lực cung cấp một hệ thống theo dõi và ghi chép văn bản Q&A gọn nhẹ, độ tương thích cao, hỗ trợ lưu trữ tĩnh ngoại tuyến và giao diện người dùng thân thiện, rất thích hợp cho mục đích quản lý tri thức (Knowledge Management), kèm theo tệp đính kèm cho các quy trình phức tạp. Quá trình phát triển dự án tuân theo quy trình điều khiển bởi đặc tả OpenSpec v1.12.0 và được hỗ trợ bởi Antigravity trong việc tái cấu trúc kiến trúc và đảm bảo chất lượng.

---

## Hướng dẫn Biên dịch Đa ngôn ngữ / Multilingual Build Guides

| Ngôn ngữ / Language | Hướng dẫn / Build Guide |
|---|---|
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn biên dịch và đóng gói Tiếng Việt](docs/build/BUILD_vi.md) |
| **English** | [English Build & Compilation Guide](docs/build/BUILD_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文編譯與建置完整指南](docs/build/BUILD_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文编译与构建完整指南](docs/build/BUILD_zh-CN.md) |
| **日本語 (Japanese)** | [日本語ビルド・コンパイル詳細ガイド](docs/build/BUILD_ja.md) |
| **Deutsch (German)** | [Deutscher Kompilierungs- und Build-Leitfaden](docs/build/BUILD_de.md) |
| **Español (Spanish)** | [Guía de compilación y construcción en español](docs/build/BUILD_es.md) |
| **Français (French)** | [Guide complet de compilation et de construction en français](docs/build/BUILD_fr.md) |

---

## Hướng dẫn Quản trị Hệ thống Đa ngôn ngữ / Multilingual Administrator Guides

| Ngôn ngữ / Language | Hướng dẫn Quản trị / Admin Guide |
|---|---|
| **Tiếng Việt (Vietnamese)** | [Hướng dẫn Quản trị viên và Cấu hình Hệ thống](docs/admin/ADMIN_GUIDE_vi.md) |
| **English** | [English Administrator & System Configuration Guide](docs/admin/ADMIN_GUIDE_en.md) |
| **繁體中文 (Traditional Chinese)** | [繁體中文系統管理者完整指南](docs/admin/ADMIN_GUIDE_zh-TW.md) |
| **简体中文 (Simplified Chinese)** | [简体中文系统管理员完整指南](docs/admin/ADMIN_GUIDE_zh-CN.md) |
| **日本語 (Japanese)** | [日本語システム管理者総合ガイド](docs/admin/ADMIN_GUIDE_ja.md) |
| **Deutsch (German)** | [Deutscher Systemadministrator- und Konfigurationsleitfaden](docs/admin/ADMIN_GUIDE_de.md) |
| **Español (Spanish)** | [Guía del Administrador del Sistema y Configuración en Español](docs/admin/ADMIN_GUIDE_es.md) |
| **Français (French)** | [Guide d'administration et de configuration du système en français](docs/admin/ADMIN_GUIDE_fr.md) |

---

## Nhật ký Cập nhật Quan trọng (Changelog & Major Updates)

### 🚀 Phiên bản 2.3.3-2.0.0 Nâng cấp Lớn (Major Architecture Upgrade)

1. **Nâng cấp Kiến trúc Backend (Spring 5.3 + Hibernate 5.6 + JUnit 5)**:
   - Nâng cấp lên Spring Framework 5.3.37, loại bỏ hoàn toàn `HibernateTemplate` và `TimerFactoryBean`.
   - Nâng cấp lên Hibernate ORM 5.6.15.Final với quản lý `SessionFactory` nguyên bản và truy vấn chuẩn JPA.
   - Chuyển đổi tìm kiếm toàn văn sang API Lucene nguyên bản, tách biệt khỏi thư viện cũ `spring-modules-lucene`.
   - Nâng cấp kiểm thử đơn vị sang JUnit 5 (Jupiter).
2. **Cải tiến Bảo mật Toàn diện (Spring Security 5.8 + Tự động chuyển đổi BCrypt)**:
   - Thay thế Acegi Security 1.0.7 cũ kỹ bằng Spring Security 5.8.14 chuẩn mực.
   - Cung cấp `JtracHybridPasswordEncoder`: tương thích mã băm MD5 cũ và tự động tái băm sang BCrypt an toàn ngay khi người dùng đăng nhập thành công.
3. **Nâng cấp Tầng Web (Apache Wicket 9.16.0)**:
   - Thay thế Wicket 1.3.7 từ năm 2008 bằng Wicket 9.16.0 với Generics toàn diện (`IModel<T>`).
   - Tương thích hoàn hảo với các Servlet Container chuẩn Servlet 4.0 (Jetty 10.0.26, Jetty 12, Tomcat 9, Tomcat 10+).
4. **Phân trang Danh sách Người dùng & Không gian Dự án (Pagination & Settings)**:
   - Danh sách người dùng (`UserListPage`) và không gian (`SpaceListPage`) hỗ trợ phân trang linh hoạt (10, 25, 50, 100, Tất cả).
   - Thêm tham số `users.list.pageSize` và `spaces.list.pageSize` vào bảng `config`.
5. **Sửa lỗi Sự kiện Ajax Phân quyền Dự án**:
   - Chuyển đổi sự kiện Ajax sang chuẩn DOM `"change"`, xử lý chống lỗi khi bỏ chọn quyền.
6. **Bộ lọc Tài nguyên Tĩnh Toàn cục (StaticResourceFilter)**:
   - Giải quyết triệt để lỗi 404 hình ảnh `../resources/*` ở các đường dẫn URL lồng nhau và bổ sung các biểu tượng còn thiếu.
7. **Sửa lỗi Gắn kết Model cho Upload Tệp (FileUpload Model Binding)**:
   - Gắn kết `ListModel` độc lập cho `FileUploadField` trong `ItemFormPage` và `ItemViewFormPanel`, loại bỏ lỗi ngoại lệ thuộc tính `file`.
8. **Nâng cấp Cơ sở dữ liệu và Kịch bản SQL**:
   - Cung cấp kịch bản nâng cấp [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql) cho MySQL, PostgreSQL, SQL Server, Oracle.
   - Tích hợp `HsqldbDatabaseMigrator` tự động sao lưu và nâng cấp HSQLDB 1.8 lên 2.x khi khởi động.
9. **Xóa Bỏ Mô-đun Excel & Thu Nhỏ Kích Thước Gói WAR (Excel Module Removal & POI Deprecation)**:
   - Loại bỏ hoàn toàn tính năng nhập/xuất Excel và thư viện Apache POI, giảm kích thước gói WAR hơn 3 MB.
10. **Nâng Cấp Gói Sao Lưu Toàn Bộ Hệ Thống (`jtrac-dump.sql`)**:
    - Gói ZIP sao lưu toàn bộ hệ thống hiện bao gồm tệp kết xuất SQL độc lập `jtrac-dump.sql` (chứa ANSI DDL, chú thích phương ngữ cho MySQL/PostgreSQL/HSQLDB, các câu lệnh INSERT sắp xếp theo khóa ngoại và lệnh đặt lại sequence) phục vụ di chuyển dữ liệu và khôi phục sự cố.
11. **Phân Vùng Tệp Đính Kèm Theo ID Dự Án & Lập Chỉ Mục Toàn Văn Lucene**:
    - **Cấu Trúc Thư Mục Phân Vùng Theo ID Số (Tùy Chọn C)**: Tệp đính kèm được lưu theo ID dự án dạng số thuần túy (`${jtrac.home}/attachments/{spaceId}/{prefix}_{filename}`), loại bỏ hoàn toàn rủi ro khi đổi tên dự án.
    - **Cơ Chế Đọc Dự Phòng Kép (Dual-Read Fallback)**: Tự động chuyển hướng về thư mục gốc và thư mục cách ly (`attachments/0_ORPHAN/`), bảo đảm 0% lỗi liên kết tải xuống 404.
    - **Tự Động Di Chuyển Khi Khởi Động**: Quét và tự động di chuyển tệp đính kèm cũ vào các thư mục dự án khi máy chủ khởi động, kèm tệp đánh dấu hoàn tất (`.attachment_migrated`).
    - **Trích Xuất Văn Bản Đa Định Dạng**: Hỗ trợ `.xlsx`, `.docx` (bộ phân tích OpenXML thuần JDK), `.pdf` (Apache PDFBox 2.0.31), `.txt`, `.csv`, `.md`, `.log` cùng công cụ nhận diện bảng mã `SmartCharsetDetector`.
    - **Giới Hạn Bảo Vệ & Hàng Đợi Bất Đồng Bộ**: Giới hạn 10MB mỗi tệp và 50.000 ký tự; luồng xử lý nền (`ExecutorService`) giúp phản hồi tải lên tức thì.

---

## Công nghệ & Kiến trúc (Technologies & Architecture)

- **Ngôn ngữ chính**: Java 11 / 17
- **Web Framework**: Apache Wicket 9.16.0
- **IoC Container**: Spring Framework 5.3.37
- **Bảo mật**: Spring Security 5.8.14 (Mã hóa BCrypt)
- **ORM & Persistence**: Hibernate ORM 5.6.15.Final
- **Cơ sở dữ liệu hỗ trợ**: HSQLDB 2.x (mặc định), MySQL / MariaDB, PostgreSQL, Microsoft SQL Server, Oracle
- **Máy chủ Web hỗ trợ**:
  - **Jetty 10.x** (Hỗ trợ trực tiếp, đã kiểm thử thực tế trên Jetty 10.0.26)
  - **Jetty 12.x** (Bật module `ee8` để chạy trực tiếp)
  - **Tomcat 9.x** (Hỗ trợ trực tiếp)
  - **Tomcat 10.x / 11.x** (Hỗ trợ thông qua thư mục tự động chuyển đổi `webapps-javaee/` hoặc công cụ `jakartaee-migration`)
- **Công cụ đóng gói**: Apache Maven 3.9+
- **Quy chuẩn phát triển**: OpenSpec v1.12.0, Antigravity

---

## Bắt đầu Nhanh (Quick Start)

### 1. Đóng gói Ứng dụng Web (WAR)
```bash
# Biên dịch mã nguồn (yêu cầu JDK 11 hoặc JDK 17)
mvn clean compile

# Chạy kiểm thử và đóng gói WAR
mvn package

# Đóng gói nhanh (bỏ qua kiểm thử)
mvn package -DskipTests
```
Tệp gói xuất ra: `target/jtrac.war`.

### 2. Nâng cấp Cơ sở dữ liệu (Khi nâng cấp từ 2.3.3-1.0.0)
- Với MySQL / PostgreSQL / SQL Server / Oracle: Chạy tệp [`etc/sql/upgrade-to-2.0.0.sql`](etc/sql/upgrade-to-2.0.0.sql).
- Với HSQLDB nhúng: Hệ thống sẽ tự động sao lưu và nâng cấp cấu trúc dữ liệu khi khởi động máy chủ.

### 3. Đóng gói Công cụ Xuất HTML Độc lập (CLI)
```bash
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
# Đầu ra: tools/jtrac-exporter.jar
```

---

## Giấy phép (License)

JTrac là phần mềm mã nguồn mở theo giấy phép [Apache Software License, Version 2.0](license.txt).
