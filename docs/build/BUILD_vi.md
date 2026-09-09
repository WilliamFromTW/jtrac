# Hướng dẫn Biên dịch và Đóng gói JTrac (Tiếng Việt)

[English](BUILD_en.md) | [繁體中文](BUILD_zh-TW.md) | [简体中文](BUILD_zh-CN.md) | [日本語](BUILD_ja.md) | [Tiếng Việt](BUILD_vi.md) | [Deutsch](BUILD_de.md) | [Español](BUILD_es.md) | [Français](BUILD_fr.md)

Tài liệu này cung cấp hướng dẫn chi tiết về cách xây dựng, biên dịch và đóng gói dự án JTrac 2.3.3-2.0.0, cấu trúc tệp WAR và triển khai trên các máy chủ Web (Jetty 10/12, Tomcat 9/10/11).

---

## 1. Yêu cầu Môi trường

- **Hệ điều hành**: Windows / Linux / macOS
- **Bộ phát triển Java (JDK)**: **JDK 11 hoặc JDK 17** (khuyến nghị JDK 17, ví dụ: `W:\developer\jdk-17.0.9` hoặc JDK 11 `W:\developer\jdk-11.0.28`)
  > [!IMPORTANT]
  > Dự án đã nâng cấp lên Spring 5.3, Hibernate 5.6 và Wicket 9 với mục tiêu Java 11. **JDK 8 không còn được hỗ trợ**.
- **Apache Maven**: Phiên bản 3.9.x trở lên

### Thiết lập Môi trường Windows
```powershell
$env:JAVA_HOME = "W:\developer\jdk-17.0.9"
$env:PATH = "W:\developer\apache-maven-3.9.9\bin;$env:PATH"
```

Kiểm tra:
```bash
mvn -version
```

---

## 2. Các Lệnh Đóng gói Thường dùng

| Lệnh | Mô tả |
|---|---|
| `mvn clean compile` | Xóa bộ nhớ đệm và biên dịch lại `src/main/java` |
| `mvn test-compile` | Biên dịch các lớp kiểm thử |
| `mvn test` | Chạy toàn bộ kiểm thử đơn vị (JUnit 5 + HSQLDB tích hợp) |
| `mvn package` | Kiểm thử và đóng gói tệp ứng dụng WAR (`target/jtrac.war`) |
| `mvn package -DskipTests` | Đóng gói nhanh (bỏ qua kiểm thử) |
| `mvn clean` | Xóa thư mục `target/` |

---

## 3. Cấu trúc Gói WAR (`WEB-INF/lib/`)

Cấu trúc [`target/jtrac.war`](../../target/jtrac.war):

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/
│   ├── lib/                     <-- Toàn bộ thư viện phụ thuộc hiện đại
│   │   ├── spring-core-5.3.37.jar
│   │   ├── wicket-core-9.16.0.jar
│   │   ├── hibernate-core-5.6.15.Final.jar
│   │   ├── spring-security-core-5.8.14.jar
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ...
│   └── web.xml                  <-- Chuẩn Servlet 4.0
└── resources/
```

---

## 4. Ma trận Máy chủ Web và Triển khai

JTrac 2.3.3-2.0.0 hỗ trợ chuẩn Servlet 4.0 (`javax.servlet`):

| Máy chủ Web | Phiên bản | Cách Triển khai |
|---|---|---|
| **Jetty 10.x** | 10.0.x (Khuyến nghị) | **Trực tiếp**: Sao chép `target/jtrac.war` thành `webapps/ROOT.war`. |
| **Jetty 12.x** | 12.0.x (Mới nhất) | **Nguyên bản**: Kích hoạt module `ee8`:<br/>`java -jar start.jar --add-modules=server,http,ee8-deploy,ee8-webapp` |
| **Tomcat 9.x** | 9.0.x (Khuyến nghị) | **Trực tiếp**: Sao chép `target/jtrac.war` thành `webapps/ROOT.war`. |
| **Tomcat 10.x / 11.x** | 10.1.x / 11.0.x | **Chuyển đổi tự động**: Đặt vào thư mục `webapps-javaee/` hoặc dùng công cụ `jakartaee-migration`. |

---

## 5. Nâng cấp Cơ sở Dữ liệu & Lưu trữ
 
Khi nâng cấp từ 2.3.3-1.0.0:
- Cơ sở dữ liệu bên ngoài (MySQL, PostgreSQL, v.v.): Chạy [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql).
- HSQLDB nhúng: Tự động sao lưu và nâng cấp lên 2.x khi khởi động máy chủ.
- Di chuyển tệp đính kèm: `AttachmentStorageMigrator` tự động di chuyển tệp cũ vào thư mục theo ID dự án (`attachments/{spaceId}/`), cách ly tệp mồ côi vào `attachments/0_ORPHAN/`.
- Tìm kiếm toàn văn: Tích hợp trích xuất văn bản cho `.xlsx`, `.docx` (bộ phân tích OpenXML thuần JDK), `.pdf` (Apache PDFBox 2.0.31), `.txt`, `.csv`, `.md`, `.log` cùng `SmartCharsetDetector`.

---

## 6. Công cụ Xuất HTML Độc lập (`jtrac-exporter`)

```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-output" ^
  --lang=vi
```
