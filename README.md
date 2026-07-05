# TaskFlow — Quản lý công việc nhóm

Ứng dụng quản lý công việc nhóm full-stack: **Spring Boot 3 + PostgreSQL** cho backend, **React 18 (Vite) + TailwindCSS** cho frontend, tách biệt hoàn toàn qua REST API + WebSocket. Có đăng nhập, phân quyền Leader/Nhân viên, giao việc và thông báo real-time. Dự án được xây dựng cho bài test vị trí Intern Developer tại SRT Group.

## 1. Tính năng

**Xác thực & phân quyền**
- Đăng nhập bằng JWT (Spring Security), 2 vai trò: `LEADER` (trưởng nhóm) và `EMPLOYEE` (nhân viên).
- Không có trang đăng ký công khai — Leader tạo tài khoản nhân viên trong ứng dụng.
- Một tài khoản Leader mặc định được tự động seed khi khởi động lần đầu (xem mục 7).

**Giao việc & theo dõi (Leader)**
- Tạo công việc và giao cho một nhân viên cụ thể (`assigneeId`), có thể kèm chức vụ khi tạo tài khoản nhân viên.
- Xem toàn bộ công việc của cả nhóm, lọc theo nhân viên / trạng thái / độ ưu tiên / từ khóa.
- Sửa, xóa, đổi trạng thái bất kỳ công việc nào — kéo thả (drag-and-drop) trên **bảng Kanban** hoặc chọn trực tiếp ở danh sách.
- Nhận **thông báo real-time qua WebSocket** ngay khi nhân viên đánh dấu hoàn thành một việc được giao.
- Xem **bảng thống kê năng suất** theo từng nhân viên (điểm năng suất, tỷ lệ hoàn thành, tỷ lệ đúng hạn — xem công thức ở mục 10).

**Thực hiện việc (Nhân viên)**
- Chỉ thấy các công việc được giao cho chính mình (chặn ở cả backend lẫn ẩn UI, không phải chỉ ẩn giao diện).
- Đổi trạng thái Chưa làm / Đang làm / Hoàn thành — kéo thả trên bảng Kanban hoặc chọn ở danh sách.
- Không thể tạo/sửa/xóa việc hay xem việc của người khác.

**Chung**
- Tìm kiếm theo tiêu đề/mô tả (debounce 300ms); phân trang với **tùy chọn số lượng hiển thị** (5/10/15/20/30/50/Tất cả); sắp xếp linh hoạt.
- Xem công việc dạng **danh sách** hoặc **bảng Kanban 3 cột** (Chưa làm | Đang làm | Hoàn thành).
- Validate dữ liệu ở cả client (React Hook Form + Yup) và server (Bean Validation).
- Response API theo một envelope thống nhất (`success`, `message`, `data`, `errors`, `timestamp`).
- Xử lý lỗi tập trung (`GlobalExceptionHandler`): 400/401/403/404/409/500 đều trả cấu trúc nhất quán.
- Tài liệu API tự động với Swagger/OpenAPI.
- Unit test cho Service và Controller (JUnit 5 + Mockito + Spring Security Test).
- Đóng gói bằng Docker, chạy toàn bộ hệ thống với một lệnh.
- Script `start.bat` / `stop.bat` để khởi động/dừng cả hệ thống bằng một lệnh duy nhất khi chạy local (không cần Docker).

## 2. Kiến trúc & tách bạch Frontend / Backend

Hai phần hoàn toàn độc lập, giao tiếp **duy nhất** qua REST API (JSON) và một kênh WebSocket (STOMP) cho thông báo — không chia sẻ code, không truy cập chung database, không phụ thuộc build tool của nhau:

```
┌────────────────────┐        REST /api/v1/*  (JWT Bearer)      ┌───────────────────────┐
│  frontend/          │ ───────────────────────────────────────▶ │  backend/              │
│  React 18 + Vite     │                                          │  Spring Boot 3          │
│  (Node/npm project)  │ ◀─────── WebSocket /ws (STOMP/SockJS) ── │  (Maven project)        │
└────────────────────┘                                          └──────────┬────────────┘
                                                                              │ JPA / Hibernate
                                                                              ▼
                                                                    ┌──────────────────┐
                                                                    │   PostgreSQL 15    │
                                                                    └──────────────────┘
```

- **Backend** không biết gì về React/Vite; nó chỉ export REST endpoints + WebSocket. Có thể thay frontend bằng bất kỳ client nào (mobile app, Postman, curl...) mà không cần sửa backend.
- **Frontend** không bao giờ gọi database trực tiếp, không import bất kỳ code Java nào; toàn bộ giao tiếp mạng đi qua `frontend/src/api/*.js` — component/hook **không được phép** gọi `axios`/`fetch` trực tiếp (đã kiểm tra: chỉ có một chỗ duy nhất, `api/client.js`, dùng `axios`).
- Mỗi bên có build tool, dependency manager, Dockerfile, biến môi trường riêng (`backend/pom.xml` vs `frontend/package.json`); build một bên không ảnh hưởng bên còn lại.
- CORS được cấu hình tường minh ở backend (`CorsConfig`) thay vì để mặc định, vì hai origin (`:5173` dev, `:80` sau nginx) là khác nhau — đây là bằng chứng rõ nhất cho việc tách domain thật sự (không chạy chung port/server).

## 3. Tech Stack

**Backend**

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-Build-red?logo=apachemaven)
![Swagger](https://img.shields.io/badge/OpenAPI-3.0-85EA2D?logo=swagger)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-black?logo=websocket)
![JUnit5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5)

**Frontend**

![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite)
![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3-38BDF8?logo=tailwindcss)
![Axios](https://img.shields.io/badge/Axios-HTTP%20client-5A29E4?logo=axios)
![React Router](https://img.shields.io/badge/React%20Router-6-CA4245?logo=reactrouter)

**Hạ tầng**

![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

## 4. Yêu cầu hệ thống

| Công cụ | Phiên bản tối thiểu |
|---|---|
| JDK | 17 |
| Maven | Không bắt buộc cài — dự án có sẵn **Maven Wrapper** (`backend/mvnw.cmd`), tự tải đúng bản Maven 3.9.6 nếu máy chưa có |
| Node.js | 18 |
| PostgreSQL | 15 (nếu chạy thủ công, không dùng Docker) |
| Docker & Docker Compose | 24+ (nếu chạy bằng Docker) |

## 5. Cấu hình trước khi chạy (bắt buộc, làm 1 lần)

Dự án kết nối tới **PostgreSQL cài sẵn trên máy bạn**. Mỗi máy có username/password Postgres khác nhau, nên bạn **không thể chạy được ngay** với file cấu hình mẫu — phải tự điền đúng thông tin Postgres của mình trước.

> Ví dụ xuyên suốt tài liệu này dùng username `postgres`, password `29042003` — đó là Postgres trên máy tác giả. **Bạn phải thay bằng username/password Postgres thật của máy bạn** (ví dụ có thể là `postgres`/`postgres`, `postgres`/`admin123`,... tùy lúc bạn cài Postgres).

### 5.1. Tạo database

Mở `psql` hoặc pgAdmin, chạy:

```sql
CREATE DATABASE todo_db;
```

### 5.2. Sửa đúng file cấu hình, tùy theo cách bạn định chạy

| Cách chạy (xem mục tương ứng bên dưới) | File cần sửa | Ghi chú |
|---|---|---|
| `start.bat` | `.env` ở **thư mục gốc** | Copy từ `.env.example` rồi sửa |
| `docker compose up` | `.env` ở **thư mục gốc** | Container Postgres cũng dùng chính 2 biến này để tự tạo user/password |
| `.\mvnw.cmd spring-boot:run` chạy tay trong `backend/` | **Biến môi trường của terminal**, KHÔNG phải file `.env` | Xem mục 5.3 |

**Nếu dùng `start.bat` hoặc Docker** — copy và sửa file `.env`:

```powershell
copy .env.example .env
```

Mở file `.env` vừa tạo (ở thư mục gốc dự án), sửa 3 dòng đầu bằng thông tin Postgres thật của bạn:

```
DB_HOST=127.0.0.1
DB_USERNAME=postgres
DB_PASSWORD=<mật khẩu Postgres của BẠN — không phải 29042003 của tác giả>
```

Các dòng còn lại (`JWT_SECRET`, `SEED_LEADER_*`,...) có thể giữ nguyên giá trị mặc định, không bắt buộc sửa.

### 5.3. Nếu chạy `.\mvnw.cmd spring-boot:run` trực tiếp (không qua `start.bat`/Docker)

File `.env` ở thư mục gốc **không tự động được Spring Boot đọc** — phải khai báo biến môi trường trước khi chạy, ngay trong terminal đó:

```powershell
cd backend
$env:DB_HOST = "127.0.0.1"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "<mật khẩu Postgres của bạn>"
.\mvnw.cmd spring-boot:run
```

> Lưu ý PowerShell: phải có tiền tố `.\` trước `mvnw.cmd` — PowerShell không tự chạy file thực thi ở thư mục hiện tại nếu chỉ gõ tên file trần (khác với cmd.exe/bash). Gõ thiếu `.\` sẽ báo lỗi `is not recognized as the name of a cmdlet...`.

Muốn khỏi gõ lại mỗi lần mở terminal mới, set cố định một lần cho toàn tài khoản Windows (chỉ cần làm 1 lần, sau đó **mở cửa sổ terminal mới** để có hiệu lực):

```powershell
setx DB_HOST "127.0.0.1"
setx DB_USERNAME "postgres"
setx DB_PASSWORD "<mật khẩu Postgres của bạn>"
```

Sau bước này, từ mọi terminal mới, chạy `cd backend` rồi `.\mvnw.cmd spring-boot:run` là được ngay không cần khai báo gì thêm.

### 5.4. Frontend

`frontend/.env` (khác với `.env` ở thư mục gốc) đã có sẵn `VITE_API_BASE_URL=http://localhost:8080` trỏ đúng vào backend chạy trên cùng máy — **không cần sửa gì** nếu chạy local theo hướng dẫn ở đây. Chỉ sửa file này nếu bạn đổi cổng backend hoặc deploy backend ở địa chỉ khác.

## 6. Chạy nhanh nhất — một lệnh cho mỗi phần

Nếu chỉ muốn `cd` vào từng thư mục rồi gõ một lệnh duy nhất:

```powershell
cd backend
.\mvnw.cmd spring-boot:run     # macOS/Linux/Git Bash: ./mvnw spring-boot:run
```

```powershell
cd frontend
npm start                      # tương đương npm run dev
```

Hai điều kiện để lệnh trên chạy được ngay không cần thêm gì:

- **Backend**: biến `DB_HOST` / `DB_USERNAME` / `DB_PASSWORD` phải có trong môi trường. Trên máy đã từng setup dự án này, các biến này đã được lưu cố định vào tài khoản Windows bằng `setx` nên **mọi cửa sổ terminal mới mở sau đó** đều tự nhận được — không cần export lại mỗi lần. Nếu là máy khác/lần đầu setup, xem mục 5.3. Lưu ý PowerShell luôn cần tiền tố `.\` trước `mvnw.cmd`.
- **Frontend**: đã có sẵn file `frontend/.env` chứa `VITE_API_BASE_URL`, nên `npm start` luôn chạy được ngay không cần cấu hình gì thêm.

`mvnw.cmd`/`mvnw` tự tải Maven nếu máy chưa cài — không cần cài Maven thủ công.

## 7. Chạy cả hệ thống bằng một script (`start.bat`)

Thay vì mở 2 terminal riêng như mục 6, dùng script này để khởi động cả backend + frontend cùng lúc từ thư mục gốc:

```powershell
copy .env.example .env
# sửa .env: điền đúng DB_USERNAME / DB_PASSWORD của Postgres trên máy bạn
.\start.bat
```

Script tự động: đọc `.env`, tìm Maven, mở cửa sổ chạy backend, đợi backend sẵn sàng, mở cửa sổ chạy frontend, mở trình duyệt tới `http://localhost:5173`. Dừng bằng `.\stop.bat`.

**Tài khoản đăng nhập mặc định** (tự tạo khi backend khởi động lần đầu, nếu chưa có user nào trong DB):

| Username | Password | Vai trò |
|---|---|---|
| `leader` | `leader123` | Trưởng nhóm (Leader) |

Đăng nhập bằng tài khoản này, vào mục **"Nhân viên"** để tạo tài khoản cho nhân viên — không có trang đăng ký công khai.

Đổi tài khoản mặc định qua biến môi trường `SEED_LEADER_USERNAME` / `SEED_LEADER_PASSWORD` / `SEED_LEADER_FULLNAME` (xem `.env.example`).

## 8. Chạy bằng Docker

```bash
cp .env.example .env
docker compose up --build
```

Sau khi khởi động thành công:

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html

Dừng hệ thống: `docker compose down` (thêm `-v` nếu muốn xóa luôn volume dữ liệu Postgres).

## 9. Chạy thủ công (không dùng Docker, không dùng script)

### 9.1. Database

```sql
CREATE DATABASE todo_db;
```

### 9.2. Backend

Windows (PowerShell):

```powershell
cd backend
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"
.\mvnw.cmd spring-boot:run
```

macOS/Linux/Git Bash:

```bash
cd backend
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run
```

Backend chạy tại `http://localhost:8080`. Tài khoản Leader mặc định (`leader`/`leader123`) được tạo tự động ở lần chạy đầu tiên.

> Đổi `postgres`/`postgres` ở trên bằng username/password Postgres thật của bạn — xem mục 5 nếu muốn set cố định một lần cho toàn bộ terminal thay vì gõ lại mỗi lần.

### 9.3. Frontend

```powershell
cd frontend
npm install
echo "VITE_API_BASE_URL=http://localhost:8080" > .env
npm start          # tương đương npm run dev
```

Frontend chạy tại `http://localhost:5173`.

## 10. API Endpoints

Base URL: `/api/v1`. Mọi endpoint (trừ `POST /auth/login`) yêu cầu header `Authorization: Bearer <token>`.

### Auth

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| POST | `/auth/login` | Công khai | Đăng nhập, trả về JWT + thông tin user |
| GET | `/auth/me` | Đã đăng nhập | Lấy thông tin tài khoản hiện tại |

### Users (quản lý nhân viên)

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| POST | `/users` | Leader | Tạo tài khoản nhân viên mới (có thể kèm `position` — chức vụ) |
| GET | `/users` | Leader | Danh sách nhân viên (dùng cho dropdown giao việc) |

### Todos

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/todos` | Đã đăng nhập | Danh sách công việc (Leader thấy tất cả; Nhân viên chỉ thấy việc của mình dù truyền filter gì) |
| GET | `/todos/{id}` | Đã đăng nhập | Chi tiết một công việc (Nhân viên chỉ xem được việc của mình) |
| POST | `/todos` | Leader | Tạo và giao công việc cho một nhân viên |
| PUT | `/todos/{id}` | Leader | Cập nhật toàn bộ / giao lại cho người khác |
| PATCH | `/todos/{id}/status` | Leader hoặc chính nhân viên được giao | Đổi trạng thái — khi nhân viên chuyển sang `COMPLETED`, Leader tạo việc đó sẽ được thông báo real-time |
| DELETE | `/todos/{id}` | Leader | Xóa công việc |

Query params cho `GET /todos`: `keyword`, `status` (`PENDING`\|`IN_PROGRESS`\|`COMPLETED`), `priority` (`LOW`\|`MEDIUM`\|`HIGH`), `assigneeId` (chỉ có tác dụng với Leader), `page` (mặc định 0), `size` (mặc định 10, tối đa 1000 — frontend dùng 1000 cho tùy chọn "Tất cả" và cho bảng Kanban), `sortBy` (mặc định `createdAt`), `sortDir` (`ASC`\|`DESC`, mặc định `DESC`).

### Analytics (năng suất)

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/analytics/productivity` | Leader | Điểm năng suất từng nhân viên + tổng quan nhóm |

**Công thức điểm năng suất** (0-100, tính riêng cho từng nhân viên dựa trên toàn bộ công việc được giao):

1. Mỗi công việc đóng góp vào tổng điểm khả dĩ theo trọng số độ ưu tiên: `LOW=1, MEDIUM=2, HIGH=3`.
2. Công việc **hoàn thành đúng hạn** (hoặc không có hạn) → nhận đủ trọng số.
3. Công việc **hoàn thành trễ hạn** → chỉ nhận 50% trọng số (phạt trễ hẹn).
4. Công việc **chưa hoàn thành** → chưa nhận điểm.
5. `Điểm năng suất = (tổng điểm đã nhận / tổng điểm khả dĩ) × 100`.

Công thức này thưởng cho việc hoàn thành nhiều việc hơn, hoàn thành việc quan trọng hơn (ưu tiên cao), và hoàn thành đúng hạn — gộp thành một con số duy nhất dễ so sánh giữa các nhân viên. Xem chi tiết cài đặt tại `AnalyticsServiceImpl`.

### Notifications

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/notifications` | Đã đăng nhập | Danh sách thông báo của tài khoản hiện tại |
| PATCH | `/notifications/{id}/read` | Đã đăng nhập (chỉ chủ sở hữu) | Đánh dấu một thông báo đã đọc |

### WebSocket (thông báo real-time)

- Endpoint: `ws(s)://<host>/ws` (STOMP over SockJS).
- Xác thực: gửi header `Authorization: Bearer <token>` trong STOMP `CONNECT` frame.
- Subscribe: `/user/queue/notifications` — nhận `NotificationResponseDTO` (JSON) mỗi khi có thông báo mới dành cho user đang kết nối.

### Ví dụ request/response

**POST /api/v1/auth/login**

```json
{ "username": "leader", "password": "leader123" }
```

```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "user": { "id": 1, "username": "leader", "fullName": "Truong nhom", "role": "LEADER", "createdAt": "2026-07-05T13:49:45" }
  },
  "timestamp": "2026-07-05T13:51:16"
}
```

**POST /api/v1/todos** (Leader, header `Authorization: Bearer <token>`)

```json
{
  "title": "Hoàn thành báo cáo tháng",
  "description": "Tổng hợp số liệu Q3",
  "status": "PENDING",
  "priority": "HIGH",
  "dueDate": "2026-07-10",
  "assigneeId": 2
}
```

```json
{
  "success": true,
  "message": "Tạo công việc thành công",
  "data": {
    "id": 5, "title": "Hoàn thành báo cáo tháng", "status": "PENDING", "priority": "HIGH",
    "dueDate": "2026-07-10", "assigneeId": 2, "assigneeName": "Nguyen Van A",
    "createdById": 1, "createdByName": "Truong nhom",
    "createdAt": "2026-07-05T10:30:00", "updatedAt": "2026-07-05T10:30:00"
  },
  "timestamp": "2026-07-05T10:30:00"
}
```

**Lỗi phân quyền — 403 Forbidden** (nhân viên cố tạo/sửa/xóa việc, hoặc xem việc không phải của mình)

```json
{
  "success": false,
  "message": "Bạn không có quyền thực hiện thao tác này",
  "errors": ["Bạn không có quyền truy cập công việc này"],
  "timestamp": "2026-07-05T10:30:00"
}
```

**Lỗi validation — 400 Bad Request**

```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "errors": ["Tiêu đề không được để trống", "Phải chọn người được giao việc"],
  "timestamp": "2026-07-05T10:30:00"
}
```

**Trùng tên đăng nhập — 409 Conflict**

```json
{
  "success": false,
  "message": "Tên đăng nhập đã tồn tại: nhanvien_test",
  "errors": ["Tên đăng nhập đã tồn tại: nhanvien_test"],
  "timestamp": "2026-07-05T10:30:00"
}
```

## 11. Cấu trúc thư mục

```
Todo_list/
├── backend/                              # Spring Boot REST API + WebSocket (Maven project, độc lập)
│   ├── src/main/java/com/example/todolist/
│   │   ├── controller/                   # AuthController, UserController, TodoController, NotificationController, AnalyticsController
│   │   ├── service/                      # Business logic (interface + impl)
│   │   ├── repository/                   # Spring Data JPA repositories
│   │   ├── model/                        # Entity: User, Todo, Notification + enum Role/TodoStatus/TodoPriority
│   │   ├── dto/                          # Request/response DTO (không bao giờ trả entity ra ngoài)
│   │   ├── mapper/                       # MapStruct mapper (Entity <-> DTO)
│   │   ├── security/                     # JwtService, JwtAuthFilter, AppUserDetailsService
│   │   ├── config/                       # SecurityConfig, WebSocketConfig, CorsConfig, DataSeeder
│   │   ├── exception/                    # Custom exception + GlobalExceptionHandler
│   │   └── TodoListApplication.java
│   ├── src/test/java/...                 # Unit test (JUnit 5 + Mockito + Spring Security Test)
│   ├── mvnw / mvnw.cmd / .mvn/            # Maven Wrapper — chạy được mà không cần cài Maven
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/                              # React + Vite SPA (Node project, độc lập, chỉ nói chuyện qua HTTP/WS)
│   ├── src/
│   │   ├── api/                          # client.js (axios instance + JWT interceptor), authApi/userApi/todoApi/
│   │   │                                 #   notificationApi/productivityApi, ws.js
│   │   ├── context/                      # AuthContext (JWT, user, login/logout)
│   │   ├── hooks/                        # useTodos, useEmployees, useNotifications, useProductivity
│   │   ├── components/                   # Layout, TodoForm, TodoItem, TodoList, TodoFilter, Pagination,
│   │   │                                 #   ConfirmDialog, Toast, NotificationBell, EmployeeManager, ProtectedRoute,
│   │   │                                 #   KanbanBoard, ViewToggle, ProductivityChart
│   │   ├── pages/                        # LoginPage, LeaderDashboard, EmployeeDashboard
│   │   └── utils/                        # constants.js
│   ├── package.json
│   ├── nginx.conf
│   └── Dockerfile
│
├── docker-compose.yml                     # 3 service: db, backend, frontend
├── .env.example
├── start.bat / start.ps1                  # Khởi động cả 2 service bằng 1 lệnh (chạy local, không Docker)
├── stop.bat / stop.ps1                    # Dừng cả 2 service
└── README.md
```

## 12. Chạy Unit Test

```powershell
cd backend
.\mvnw.cmd test     # macOS/Linux/Git Bash: ./mvnw test
```

Bộ test bao gồm:

- `TodoServiceTest` — business logic + phân quyền Leader/Employee, dùng Mockito (mock `TodoRepository`, `UserRepository`, `TodoMapper`, `NotificationService`). Có test riêng cho trường hợp nhân viên cố truy cập việc không phải của mình (`AccessDeniedException`) và test việc hoàn thành có bắn thông báo cho Leader.
- `TodoControllerTest` — tầng REST với `@WebMvcTest` + `MockMvc`, giả lập `Authentication` bằng `spring-security-test` để kiểm tra `@AuthenticationPrincipal` hoạt động đúng.

## 13. Tư duy xử lý sự cố phát sinh trong quá trình build

Một số vấn đề thực tế phát sinh khi build và test end-to-end (không phải lý thuyết), cách phát hiện và hướng xử lý:

| Vấn đề | Nguyên nhân | Cách phát hiện | Cách xử lý |
|---|---|---|---|
| `GET /todos` trả lỗi 500 `function lower(bytea) does not exist` | JPQL dùng `LOWER(CONCAT('%', :keyword, '%'))`; khi `keyword` null, Hibernate bind tham số không rõ kiểu, Postgres suy nhầm sang `bytea` | Test API thật bằng curl, đọc log Hibernate/SQL | Build sẵn pattern `%keyword%` ở tầng Service, đơn giản hóa JPQL để tránh suy luận kiểu sai |
| Frontend gọi API bị chặn (CORS) | Backend chưa cấu hình CORS, browser chặn request cross-origin từ `:5173` | Test bằng trình duyệt thật (không chỉ curl) phát hiện request treo | Thêm `CorsConfig` với `CorsConfigurationSource` dùng chung cho cả Spring Security lẫn WebSocket |
| App trắng trang / crash toàn bộ khi thêm tính năng thông báo | `sockjs-client` dùng biến `global` của Node.js, không tồn tại trong trình duyệt | Đọc console log của trình duyệt qua Selenium, thấy `ReferenceError: global is not defined` | Thêm `define: { global: 'globalThis' }` vào `vite.config.js` (giải pháp chuẩn khi dùng sockjs-client với Vite) |
| Nhân viên có thể (về lý thuyết) gọi thẳng API để sửa/xóa việc của người khác | UI ẩn nút không đủ để bảo mật | Rà soát lại thiết kế phân quyền, không tin tưởng riêng phía client | Chặn ở tầng Service (`assertCanView`, so `assignee.id` với `currentUser.id`) + `@PreAuthorize("hasRole('LEADER')")` ở Controller — hai lớp bảo vệ độc lập |
| JWT hết hạn giữa phiên làm việc | Token có thời hạn (24h mặc định) | Thiết kế trước, không phải bug phát hiện sau | Axios response interceptor bắt lỗi `401`, tự xóa token và chuyển hướng về `/login` |

## 14. Giới hạn đã biết / hướng cải tiến tiếp theo

- `ddl-auto=update` phù hợp cho demo/test nhưng không an toàn cho production lâu dài (không có migration có version như Flyway/Liquibase) — nếu dữ liệu cũ tồn tại trước khi thêm cột `NOT NULL` (`assignee_id`, `created_by_id`), migration tự động có thể thất bại. Dự án hiện dùng DB mới nên không gặp vấn đề, nhưng đây là điểm cần lưu ý khi mở rộng.
- Thông báo mới chỉ có 1 loại sự kiện (hoàn thành việc). Có thể mở rộng thêm: nhắc hạn chót sắp tới, thông báo khi được giao việc mới.
- Một task hiện chỉ giao được cho đúng 1 nhân viên (theo phạm vi đã thống nhất khi triển khai); mô hình nhiều người phụ trách 1 việc sẽ cần bảng trung gian `todo_assignees`.

## 15. Screenshot

[Screenshot]

---

Tác giả: Intern Developer Candidate — SRT Group.
