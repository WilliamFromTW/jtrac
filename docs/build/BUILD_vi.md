# Hướng dẫn biên dịch và đóng gói JTrac (Tiếng Việt)

Tài liệu này cung cấp hướng dẫn chi tiết về cách thiết lập môi trường, biên dịch và đóng gói dự án JTrac, đồng thời giải thích cơ chế quản lý thư viện của Maven và cấu trúc đóng gói tệp WAR.

---

## 1. Yêu cầu môi trường

Trước khi biên dịch, hãy đảm bảo môi trường máy tính của bạn đáp ứng các yêu cầu sau:

- **Hệ điều hành**: Windows / Linux / macOS
- **Bộ phát triển Java (JDK)**: JDK 8 hoặc JDK 11 (Khuyến nghị dùng JDK 11, ví dụ: `W:\developer\jdk-11.0.25.9-hotspot`)
- **Apache Maven**: Phiên bản 3.9.x trở lên (ví dụ: `W:\developer\apache-maven-3.9.9`)

### Thiết lập môi trường trên Windows
Trên Windows, vui lòng chạy tập lệnh cấu hình trong Command Prompt (CMD) trước khi chạy lệnh Maven:
```cmd
call W:\developer\maven.bat
```
Tập lệnh này sẽ tự động thiết lập `PATH` và `JAVA_HOME` cho phiên làm việc hiện tại.

Kiểm tra môi trường:
```cmd
mvn -version
```
Màn hình sẽ hiển thị phiên bản Maven và Java 11 chính xác.

---

## 2. Các lệnh biên dịch và đóng gói thông dụng

Thực thi các lệnh sau tại thư mục gốc của dự án JTrac (nơi chứa tệp `pom.xml`):

| Lệnh | Mô tả |
|---|---|
| `mvn compile` | Biên dịch 137 tệp mã nguồn Java trong `src/main/java` và xử lý tài nguyên |
| `mvn test-compile` | Biên dịch các lớp kiểm thử trong `src/test/java` |
| `mvn test` | Chạy kiểm thử đơn vị (sử dụng cơ sở dữ liệu nhúng HSQLDB trong bộ nhớ, không cần DB bên ngoài) |
| `mvn package` | Kiểm thử và đóng gói ứng dụng web hoàn chỉnh thành tệp `target/jtrac.war` |
| `mvn package -DskipTests` | Bỏ qua kiểm thử đơn vị, đóng gói nhanh tệp `target/jtrac.war` |
| `mvn clean` | Dọn dẹp thư mục `target/` và các tệp biên dịch tạm thời |
| `mvn clean compile` | Xóa sạch bản dựng cũ và biên dịch lại toàn bộ từ đầu |

---

## 3. Cơ chế Maven tự động tải thư viện phụ thuộc (`~/.m2/repository`)

JTrac được xây dựng theo chuẩn Maven. Tất cả các thư viện bên thứ ba (bao gồm Spring Framework, Apache Wicket, Hibernate, Acegi Security, Lucene, v.v.) đều đã được khai báo trong [`pom.xml`](../../pom.xml).

### Quy trình tự động tải và lưu bộ nhớ đệm:
1. Khi bạn chạy lệnh `mvn compile` hoặc `mvn package` lần đầu tiên, Maven sẽ tự động kết nối với kho lưu trữ trung tâm (Maven Central).
2. Toàn bộ các thư viện cần thiết sẽ được tự động tải về thư mục lưu đệm cục bộ của người dùng:
   - **Windows**: `%USERPROFILE%\.m2\repository\` (ví dụ: `C:\Users\username\.m2\repository\`)
   - **Linux / macOS**: `~/.m2/repository/`
3. Các lần biên dịch tiếp theo sẽ đọc trực tiếp từ bộ nhớ đệm `.m2` này. **Lập trình viên hoàn toàn KHÔNG cần phải tự tìm kiếm, tải xuống hoặc cài đặt bất kỳ tệp JAR nào bằng tay.**

---

## 4. Cơ chế đóng gói thư viện bên thứ ba vào tệp WAR (`WEB-INF/lib/`)

Nhiều người thường băn khoăn: "Khi triển khai JTrac lên máy chủ Jetty hoặc Tomcat, có cần phải sao chép các tệp JAR bên thứ ba vào thư mục `lib/` của máy chủ không?"

**Câu trả lời: Hoàn toàn KHÔNG cần thiết!**

### Phân tích cấu trúc tệp WAR:
Khi bạn chạy lệnh `mvn package`, Maven sẽ tự động đóng gói toàn bộ ứng dụng thành một tệp WAR độc lập: [`target/jtrac.war`](../../target/jtrac.war). Cấu trúc bên trong như sau:

```text
jtrac.war
├── META-INF/
│   └── MANIFEST.MF
├── WEB-INF/
│   ├── classes/                 <-- Các tệp class đã biên dịch và tài nguyên UTF-8 của JTrac
│   │   ├── info/jtrac/...
│   │   └── messages*.properties
│   ├── lib/                     <-- [QUAN TRỌNG: Toàn bộ 53 tệp JAR bên thứ ba đều nằm ở đây!]
│   │   ├── spring-2.5.6.jar
│   │   ├── wicket-1.3.7.jar
│   │   ├── hibernate-3.2.7.ga.jar
│   │   ├── stringtree.jar       <-- Thư viện đính kèm của dự án cũng được đóng gói tại đây
│   │   ├── hsqldb-2.7.2.jar
│   │   └── ... (tất cả các thư viện phụ thuộc khác)
│   └── web.xml                  <-- Cấu hình tiêu chuẩn Servlet 3.1
└── resources/
```

### Lưu ý khi triển khai:
- **Tính độc lập và cô lập**: Bộ chứa Servlet (như Jetty 9.4, Jetty 12 chế độ `ee8`, Tomcat 9) khi khởi động sẽ tự động nạp toàn bộ các tệp JAR trong `WEB-INF/lib/` của riêng ứng dụng đó.
- **Giữ máy chủ sạch sẽ**: Thư mục `lib/` của chính máy chủ cần được giữ nguyên bản, **không được sao chép các thư viện của ứng dụng vào đó**.
- **Triển khai cực kỳ đơn giản**: Bạn chỉ cần sao chép tệp `target/jtrac.war` vào thư mục `webapps/` của máy chủ (hoặc đổi tên thành `ROOT.war`), máy chủ sẽ tự động chạy!

---

## 5. Chạy thử nghiệm trên môi trường máy cục bộ

Sau khi đóng gói xong, bạn có thể chạy thử JTrac trên máy cục bộ bằng Jetty có sẵn:

1. Sao chép `target/jtrac.war` thành `W:\developer\jtrac-2.3.3\webapps\ROOT.war`.
2. Chạy tệp `W:\developer\jtrac-2.3.3\start.bat`.
3. Mở trình duyệt và truy cập: `http://localhost:8888` (Tài khoản quản trị viên mặc định: `admin` / `admin`).

---

## 6. Xây dựng và chạy công cụ xuất thảo luận CLI độc lập (jtrac-exporter)

Dự án tích hợp sẵn công cụ dòng lệnh `jtrac-exporter` hoàn toàn độc lập, không phụ thuộc vào các framework cũ, cho phép kết nối trực tiếp qua chuỗi kết nối JDBC tới cơ sở dữ liệu để xuất vấn đề, luồng thảo luận và tệp đính kèm thành báo cáo HTML tĩnh đa ngôn ngữ.

### 6.1 Biên dịch và đóng gói công cụ (Fat JAR)
Chạy lệnh sau tại thư mục gốc của dự án (không cần chuyển thư mục):
```cmd
mvn clean package -f tools/jtrac-exporter/pom.xml -DskipTests
```
Sau khi biên dịch thành công, tệp thực thi sẽ được lưu trực tiếp tại:
`tools/jtrac-exporter.jar`

### 6.2 Chạy xuất dữ liệu (Command Mode)
Từ thư mục gốc dự án, bạn có thể sử dụng đường dẫn tương đối để kết nối tới HSQLDB cục bộ:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:hsqldb:file:./data/db/jtrac;shutdown=true;readonly=true" ^
  --attachments-dir="./data/attachments" ^
  --out="./export-hsqldb" ^
  --lang=vi
```

Đồng thời hỗ trợ kết nối tới cơ sở dữ liệu từ xa như MySQL hoặc PostgreSQL:
```cmd
java -jar tools/jtrac-exporter.jar ^
  --db-url="jdbc:mysql://192.168.1.100:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" ^
  --db-user="jtrac" ^
  --db-password="your_password" ^
  --attachments-dir="/path/to/attachments" ^
  --out="./export-mysql" ^
  --lang=vi
```
Xem hướng dẫn chi tiết các tùy chọn: `java -jar tools/jtrac-exporter.jar --help`.

