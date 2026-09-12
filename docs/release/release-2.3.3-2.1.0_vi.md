# Ghi chú Phát hành JTrac (Release Notes) - 2.3.3-2.1.0-beta

[English](release-2.3.3-2.1.0_en.md) | [繁體中文](release-2.3.3-2.1.0_zh-TW.md) | [简体中文](release-2.3.3-2.1.0_zh-CN.md) | [日本語](release-2.3.3-2.1.0_ja.md) | [Tiếng Việt](release-2.3.3-2.1.0_vi.md) | [Deutsch](release-2.3.3-2.1.0_de.md) | [Español](release-2.3.3-2.1.0_es.md) | [Français](release-2.3.3-2.1.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-11%20%7C%2017-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../../license.txt)
[![Version](https://img.shields.io/badge/Version-2.3.3--2.1.0--beta-orange.svg)](../../pom.xml)
[![Status](https://img.shields.io/badge/Status-Beta%20Preview-yellow.svg)](release-2.3.3-2.1.0_vi.md)
[![Wicket](https://img.shields.io/badge/Wicket-9.16.0-blue.svg)](https://wicket.apache.org/)
[![Spring](https://img.shields.io/badge/Spring-5.3.37-brightgreen.svg)](https://spring.io/)

> [!NOTE]
> **Trạng thái hiện tại: Bản xem trước thử nghiệm Beta (Pre-release / Beta Preview)**  
> Tài liệu này là nhật ký phát hành động (Living Release Notes). Trong giai đoạn thử nghiệm Beta, mọi tính năng bổ sung, tinh chỉnh cấu hình và sửa lỗi tiếp theo sẽ được tự động cập nhật liên tục vào đây.

---

## Mục lục
1. [Tổng quan về các điểm nổi bật](#1-tổng-quan-về-các-điểm-nổi-bật)
2. [🤖 Trợ lý truy vấn Email AI (AI Query Copilot với Ollama)](#2--trợ-lý-truy-vấn-email-ai-ai-query-copilot-với-ollama)
3. [📦 Nâng cấp thư viện cốt lõi & Loại bỏ cảnh báo Java 11](#3--nâng-cấp-thư-viện-cốt-lõi--loại-bỏ-cảnh-báo-java-11)
4. [🎨 Hiện đại hóa giao diện, Phóng to chữ & Chuyển đổi giao diện](#4--hiện-đại-hóa-giao-diện-phóng-to-chữ--chuyển-đổi-giao-diện)
5. [🛡️ Tăng cường bảo mật môi trường Production](#5--tăng-cường-bảo-mật-môi-trường-production)
6. [⚙️ Cấu hình hệ thống và cải thiện độ ổn định](#6--cấu-hình-hệ-thống-và-cải-thiện-độ-ổn-định)
7. [Hướng dẫn nâng cấp và tính tương thích](#7-hướng-dẫn-nâng-cấp-và-tính-tương-thích)

---

## 1. Tổng quan về các điểm nổi bật

Kế thừa kiến trúc hiện đại từ phiên bản 2.0.0, JTrac 2.3.3-2.1.0-beta giới thiệu **Trợ lý truy vấn Email AI (AI Query Copilot tích hợp Ollama)**, nâng cấp trình phân tích cú pháp XML để loại bỏ hoàn toàn cảnh báo truy cập phản chiếu trên Java 11, tăng cường khả năng tiếp cận (chế độ phóng chữ 4 mức với A+++, chuyển đổi theme sáng/tối 3 trạng thái) và củng cố bảo mật toàn diện.

---

## 2. 🤖 Trợ lý truy vấn Email AI (AI Query Copilot với Ollama)

1. **Mở rộng truy vấn 2 giai đoạn & Chống Prompt Injection**:
   - Tích hợp mô hình Ollama LLM, tự động phân tích email để trích xuất từ khóa thực thể và từ đồng nghĩa kỹ thuật.
   - Thiết lập vùng đệm an toàn `<untrusted_user_query>` để ngăn chặn đánh cắp chỉ lệnh và mã độc.
2. **Truy vấn trọng số kết hợp & Điểm thưởng song ngữ**:
   - Tính điểm chính xác theo Tóm tắt (+3), Chi tiết (+1), Bình luận (+1) và Tệp đính kèm (+1), cộng thêm 5 điểm cho khớp nối song ngữ Anh - Việt.
   - Thêm tham số `llm.retrieval.max_tickets` trong bảng `config` (mặc định 50).
3. **Quy trình xử lý Map-Reduce phân tán**:
   - **Giai đoạn Map**: Phân tích từng ticket và tệp đính kèm (hỗ trợ tới 100.000 ký tự mỗi tệp, đọc file PDF, Word, Excel, TXT, LOG, CSV) thành bản tóm tắt trung gian.
   - **Giai đoạn Reduce**: Tổng hợp các tóm tắt thành 3 phần rõ ràng: Tóm tắt điều hành, Phát hiện chính & Giải pháp, Khuyến nghị hành động.
   - Đảm bảo khối `finally` dọn sạch thư mục tạm, không để lại rác trên đĩa cứng.
4. **Đính kèm báo cáo HTML ngoại tuyến hoàn chỉnh (`JTrac-AI-Report-[yyyyMMdd-HHmm].html`)**:
   - **Nội dung email siêu tinh gọn**: Chỉ hiển thị bảng tóm tắt ticket và đường dẫn, ngăn ngừa vỡ giao diện trên Outlook/Gmail di động.
   - **Báo cáo HTML độc lập**: Tạo trực tiếp trong bộ nhớ qua `ByteArrayResource` (< 3ms, không tốn I/O đĩa).
   - **Thiết kế hiện đại**: Đường viền bảng rõ nét (`border-collapse: collapse`), thẻ `<details>` đóng mở mượt mà, tự động chuyển Dark Mode và định dạng in ấn tối ưu.
5. **Hướng dẫn viết Prompt đa ngôn ngữ**: Cung cấp tài liệu [`docs/llm/PROMPT_EXAMPLES_*.md`](../llm/PROMPT_EXAMPLES_vi.md) với 4 kịch bản thực chiến.
6. **Nhóm theo Space, sắp xếp ID giảm dần & Tích hợp Mermaid.js ngoại tuyến 100%**:
   - **Bảng phân nhóm theo Space & Sắp xếp mới nhất trước (ID DESC)**: Tái cấu trúc bảng tóm tắt trong email và báo cáo HTML theo từng Space được cấp quyền kèm số lượng ticket; các ticket trong mỗi Space được sắp xếp theo số hiệu ID giảm dần (`ID DESC`). Nội dung email được giữ tối giản, không kèm cảnh báo Mermaid dư thừa.
   - **Công cụ Mermaid.js ngoại tuyến hoàn toàn**: Nhúng trực tiếp gói Mermaid.js (v10.9.1) vào Classpath và báo cáo HTML, loại bỏ hoàn toàn sự phụ thuộc vào CDN bên ngoài. Hỗ trợ tự động đổi giao diện sáng/tối (`prefers-color-scheme`) và cơ chế phục hồi lỗi cú pháp.
   - **Ràng buộc Prompt lưu đồ 2 cấp độ với chuẩn đóng ngoặc kép**: Bắt buộc tạo lưu đồ `flowchart TD/LR` ở cả giai đoạn Map (quy trình tái hiện/xử lý sự cố) và Reduce (tóm tắt tổng quan & kế hoạch hành động), yêu cầu nhãn nốt phải đặt trong dấu ngoặc kép để ngăn ngừa lỗi định dạng.

---

## 3. 📦 Nâng cấp thư viện cốt lõi & Loại bỏ cảnh báo Java 11

1. **Nâng cấp `dom4j` lên `2.1.4`**:
   - Nâng cấp thư viện cũ `dom4j:1.6.1` lên bản `org.dom4j:dom4j:2.1.4`.
   - Giải quyết triệt để cảnh báo `WARNING: An illegal reflective access operation has occurred` khi chạy trên Tomcat 9 và JDK 11.

---

## 4. 🎨 Hiện đại hóa giao diện, Phóng to chữ & Chuyển đổi giao diện

1. **Chu kỳ phóng to cỡ chữ 4 giai đoạn**:
   - Hỗ trợ 100% (Tiêu chuẩn), 115% (Dễ chịu), 130% (Rõ nét) và **Chế độ cực lớn A+++ (145%)**.
   - Tích hợp cơ chế chống chớp nháy (Anti-FOUC) và bảo vệ cấu trúc bảng, lưu cấu hình vào `localStorage`.
2. **Chuyển đổi giao diện 3 trạng thái (Theme Switcher)**:
   - Chuyển đổi mượt mà giữa Tự động (Theo OS), Sáng (Light) và Tối (Dark) bằng một nút bấm.
3. **Thanh tìm kiếm thống nhất & Điều hướng thông minh**:
   - Tích hợp nút tìm kiếm bên trong ô nhập, nhận diện nhanh mã ticket (nhập `PROJ-123` chuyển thẳng đến ticket), hỗ trợ tìm kiếm toàn cục cho quản trị viên.
4. **Tối ưu hóa di động (Mobile RWD)**:
   - Menu trượt (Drawer) trên thiết bị di động, modal xem lịch sử dạng Bottom-Sheet, phân trang capsule căn giữa.

---

## 5. 🛡️ Tăng cường bảo mật môi trường Production

1. **Bộ lọc tiêu đề bảo mật toàn cục**: Bổ sung `X-Frame-Options`, `X-Content-Type-Options`, `Strict-Transport-Security`, `Content-Security-Policy`.
2. **Ngăn chặn công cụ tìm kiếm (`robots.txt`)**: Ngăn bot mạng thu thập thông tin ticket nội bộ.
3. **Chống giả mạo**: Cảnh báo quyền Guest, kiểm tra whitelist tham số URL, chống nhấp đúp gửi form nhiều lần.

---

## 6. ⚙️ Cấu hình hệ thống và cải thiện độ ổn định

1. Bổ sung `status.nullValid = ` trên toàn bộ 8 ngôn ngữ để loại bỏ log debug Wicket.
2. Nâng cấp nút bật/tắt Boolean trong cài đặt thành `IndicatingDropDownChoice`.
3. Đăng ký rõ ràng Driver JDBC cho các kết nối cơ sở dữ liệu.
4. Tự động nhận diện UTF-8 cho tệp đính kèm văn bản và xử lý đường dẫn Logo tương đối.

---

## 7. Hướng dẫn nâng cấp và tính tương thích

- **Cơ sở dữ liệu**: Tương thích hoàn toàn với bản 2.3.3-2.0.0; **không cần chạy bất kỳ mã script nâng cấp nào**.
- **Triển khai**: Ghi đè tệp `target/jtrac.war` vào `ROOT.war` hiện có trên máy chủ.
- **Tài liệu liên quan**:
  - [Hướng dẫn truy vấn Email và viết Prompt cho JTrac AI](../llm/PROMPT_EXAMPLES_vi.md)
  - [Ghi chú phát hành phiên bản trước (2.3.3-2.0.0)](release-2.3.3-2.0.0_vi.md)
