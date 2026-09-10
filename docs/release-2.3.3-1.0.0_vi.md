# Ghi chú Phát hành JTrac (Release Notes) - 2.3.3-1.0.0

[English](release-2.3.3-1.0.0_en.md) | [繁體中文](release-2.3.3-1.0.0_zh-TW.md) | [简体中文](release-2.3.3-1.0.0_zh-CN.md) | [日本語](release-2.3.3-1.0.0_ja.md) | [Tiếng Việt](release-2.3.3-1.0.0_vi.md) | [Deutsch](release-2.3.3-1.0.0_de.md) | [Español](release-2.3.3-1.0.0_es.md) | [Français](release-2.3.3-1.0.0_fr.md)

---

[![Java](https://img.shields.io/badge/Java-8%20%7C%2011-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](../license.txt)
[![Fork From](https://img.shields.io/badge/Fork%20From-JTrac%202.3.3-blue)](https://jtrac.info)
[![Version](https://img.shields.io/badge/Version-2.3.3--1.0.0-blue.svg)](../pom.xml)

Bản phát hành này đánh dấu phiên bản fork hiện đại hóa và nâng cao tính năng đầu tiên từ [JTrac 2.3.3 (https://jtrac.info)](https://jtrac.info). Phiên bản tập trung vào việc dọn dẹp mã nguồn, thắt chặt phân quyền, hỗ trợ đa ngôn ngữ UTF-8 toàn diện và khả năng lưu trữ tĩnh HTML ngoại tuyến.

---

## 📦 Các Tính năng Trọng tâm Phiên bản 2.3.3-1.0.0 (Major Features)

1. **Dọn dẹp Mô-đun (Module Cleanup)**:
   - Loại bỏ hoàn toàn mô-đun Wiki đã lỗi thời và không còn được duy trì, tinh gọn cơ sở mã nguồn và các thư viện phụ thuộc.
2. **Xuất HTML Hàng loạt & Tải ZIP từ Web (Batch HTML Export & ZIP Download)**:
   - Triển khai chức năng lưu trữ ngoại tuyến hàng loạt cho nhiều không gian dự án trên giao diện quản trị Web, xuất toàn bộ lịch sử Issue thành các tệp HTML tĩnh responsive độc lập và đóng gói ZIP tải về tiện lợi.
3. **Phân quyền Không gian Chặt chẽ (Strict Space Permission Guardrails)**:
   - Tăng cường cơ chế kiểm tra quyền truy cập không gian dự án, đảm bảo người dùng thông thường chỉ có thể xem, tìm kiếm và xuất dữ liệu các dự án mà họ được cấp quyền.
4. **Công cụ Xuất Dòng lệnh Độc lập (`tools/jtrac-exporter.jar`)**:
   - Cung cấp công cụ CLI độc lập hoạt động không cần máy chủ Web, kết nối trực tiếp qua JDBC để xuất báo cáo HTML tĩnh hỗ trợ giao diện tối.
5. **Tiêu chuẩn Đa ngôn ngữ Hoàn chỉnh 8 Ngôn ngữ (Full Multilingual Standard)**:
   - Chuẩn hóa toàn bộ dự án sang mã hóa UTF-8, hoàn thiện và hiệu đính tài nguyên giao diện cho 8 ngôn ngữ (Tiếng Anh, Tiếng Trung Phồn thể, Tiếng Trung Giản thể, Tiếng Nhật, Tiếng Việt, Tiếng Đức, Tiếng Tây Ban Nha, Tiếng Pháp).

---

## 📜 Lịch sử Phiên bản (Release History)

- **Phiên bản tiếp theo**: [Ghi chú Phát hành JTrac - 2.3.3-2.0.0](release-2.3.3-2.0.0_vi.md)

---

## Giấy phép (License)

JTrac là phần mềm mã nguồn mở theo giấy phép [Apache Software License, Version 2.0](../license.txt).
