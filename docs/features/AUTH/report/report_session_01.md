# S01 — Foundation + Auth + Docker

**Ngày:** 2026-11-23

Tạo project Spring Boot 3.3.13 / Java 21, pom.xml, application.yml (1 file), Dockerfile, docker-compose.yml (app + mysql:8.0), .gitignore, README.md, PROJECT_INDEX.md.
Core: DataSet/DataSetRegistry (JDBC thuần), PasswordUtil (BCrypt), AuthUtil (session SH_USER), PermissionFilter (quyền chỉ ở Filter, menu từ DB).
Auth ZZA0_0001: login BCrypt, session, logout; seed admin/admin123, teacher/teacher123, student/student123 (BCrypt hash tạo lúc scaffold).
Dashboard MSA0_0100 + Users/Menu MSA0_0200. Flyway V1.

## Kiểm thử (sandbox — xem ROADMAP §10 S01–S06)

- `javac` trực tiếp toàn bộ `src/main/java` với classpath là toàn bộ jar trong `~/.m2/repository` (Maven qua proxy sandbox lỗi TLS, `mvn`/`docker` không có sẵn trong sandbox).
- Unit test `ExamEngineTest` chạy bằng junit-platform-console-standalone.
- Chưa verify được trong sandbox: `mvn test`, `mvn clean package`, `docker compose up --build`, chạy app thật + login + chạy tay 3 luồng. Cần máy dev có Docker/Maven.
