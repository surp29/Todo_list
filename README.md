# TaskFlow — Quản lý công việc nhóm

Ứng dụng quản lý công việc nhóm full-stack: **Spring Boot 3 + PostgreSQL** cho backend, **React 18 (Vite) + TailwindCSS** cho frontend, tách biệt hoàn toàn qua REST API + WebSocket. Có đăng nhập, phân quyền Leader/Nhân viên, giao việc và thông báo real-time. Dự án được xây dựng cho bài test vị trí Intern Developer tại SRT Group.

## Demo trực tuyến

Dự án đã được triển khai online — **frontend trên Vercel, backend + PostgreSQL trên Render** (chi tiết cách deploy ở mục 15):

- **Ứng dụng**: https://todo-list-gamma-ten-42.vercel.app
- **Backend API**: https://todo-list-n7ha.onrender.com/api/v1
- **Swagger UI**: https://todo-list-n7ha.onrender.com/swagger-ui.html

Tài khoản đăng nhập thử: `leader` / `leader123` (xem mục 7 nếu đã đổi).

> ⚠️ **Backend chạy trên gói free của Render nên tự "ngủ" sau ~15 phút không có traffic** — lần truy cập đầu tiên sau đó có thể mất 30-90s để khởi động lại (đăng nhập sẽ đứng ở "Đang đăng nhập..." trong lúc chờ, không phải app bị lỗi). Thao tác lại lần 2 sẽ nhanh bình thường. Đây là giới hạn của hạ tầng miễn phí, không phải chất lượng code — chi tiết và bằng chứng kiểm thử ở mục 15.5.

## 1. Tính năng

**Xác thực & phân quyền**
- Đăng nhập bằng JWT (Spring Security), 2 vai trò: `LEADER` (trưởng nhóm) và `EMPLOYEE` (nhân viên).
- Không có trang đăng ký công khai — Leader tạo tài khoản nhân viên trong ứng dụng.
- Một tài khoản Leader mặc định được tự động seed khi khởi động lần đầu (xem mục 7).

**Giao việc & theo dõi (Leader)**
- Tạo công việc và giao cho một nhân viên cụ thể (`assigneeId`).
- Xem toàn bộ công việc của cả nhóm, lọc theo nhân viên / trạng thái / độ ưu tiên / từ khóa.
- Sửa, xóa, đổi trạng thái bất kỳ công việc nào — dạng danh sách hoặc **bảng Kanban kéo-thả** (Chưa làm | Đang làm | Tạm hoãn | Hoàn thành).
- Nhận **thông báo real-time qua WebSocket** ngay khi nhân viên đánh dấu hoàn thành một việc được giao — danh sách công việc trên màn hình cũng tự cập nhật theo, không cần F5.
- **Bảng thống kê năng suất** theo từng nhân viên (điểm 0-100 dựa trên mức độ ưu tiên hoàn thành + đúng hạn — công thức ở mục 10).
- **Quản lý nhân viên**: sửa họ tên/chức vụ, đặt lại mật khẩu khi quên, xóa tài khoản không còn làm việc (tự động chuyển sang vô hiệu hóa thay vì xóa hẳn nếu nhân viên đã có lịch sử công việc, để giữ số liệu năng suất).

**Thực hiện việc (Nhân viên)**
- Chỉ thấy các công việc được giao cho chính mình (chặn ở cả backend lẫn ẩn UI, không phải chỉ ẩn giao diện).
- Đổi trạng thái Chưa làm / Đang làm / **Tạm hoãn** / Hoàn thành — trạng thái Tạm hoãn dùng khi phải ưu tiên việc khác gấp hơn, không bị tính vào điểm năng suất.
- Không thể tạo/sửa/xóa việc hay xem việc của người khác.
- Nhận **thông báo real-time qua WebSocket** ngay khi Leader giao một việc mới — việc mới cũng tự hiện trong danh sách của nhân viên ngay lập tức, không cần F5.

**Thông báo (cả hai vai trò)**
- Chuông thông báo hiển thị cho cả Leader lẫn Nhân viên, đẩy real-time qua WebSocket (STOMP/SockJS) theo từng tài khoản — không polling, không cần F5.
- Nút **"Đã đọc toàn bộ"** để đánh dấu tất cả thông báo chưa đọc thành đã đọc chỉ bằng một lần bấm, thay vì bấm từng cái.
- Phân biệt icon theo loại thông báo: giao việc mới (📋) và hoàn thành việc (✅).

**Chung**
- Tìm kiếm theo tiêu đề/mô tả (debounce 300ms); phân trang có **chọn số lượng hiển thị** (5/10/15/20/30/50/Tất cả); sắp xếp linh hoạt.
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

### Thiết kế hướng đối tượng (OOP) trong backend

Backend áp dụng đầy đủ 4 trụ cột OOP, không chỉ dừng ở việc dùng class:

- **Đóng gói (Encapsulation)**: mọi entity (`model/User.java`, `model/Todo.java`, `model/Notification.java`) khai báo field `private`, chỉ lộ ra qua getter/setter; quy tắc nghiệp vụ (ví dụ quyết định xóa hẳn hay chỉ vô hiệu hóa một nhân viên) nằm trong `service/impl/UserServiceImpl.java#removeEmployee`, controller hoàn toàn không biết chi tiết cách quyết định.
- **Trừu tượng hóa (Abstraction)**: tầng service khai báo interface (`service/TodoService.java`, `UserService.java`, `NotificationService.java`, `AnalyticsService.java`) tách rời API khỏi phần cài đặt (`service/impl/*Impl.java`); tầng dữ liệu dùng `Repository` interface của Spring Data JPA, che giấu toàn bộ SQL/JPQL phía sau các phương thức như `existsByAssignee`, `findByRecipientOrderByCreatedAtDesc`.
- **Kế thừa (Inheritance)**:
  - `model/BaseEntity.java` (giữ `id`) và `model/AuditableEntity.java extends BaseEntity` (thêm `createdAt`) là lớp cha `@MappedSuperclass` dùng chung bởi `User`, `Todo`, `Notification` — cả ba đều `extends AuditableEntity` thay vì khai báo lặp lại `id`/`createdAt`.
  - Cây exception: `exception/AppException.java` (gốc) → `exception/ResourceNotFoundException.java` → `TodoNotFoundException`, `UserNotFoundException`, `NotificationNotFoundException`; còn `DuplicateUsernameException` kế thừa thẳng `AppException`.
- **Đa hình (Polymorphism)**:
  - `exception/GlobalExceptionHandler.java` chỉ có **một** `@ExceptionHandler(ResourceNotFoundException.class)` duy nhất để xử lý cả 3 loại "không tìm thấy" — tại runtime, Spring gọi đúng instance cụ thể (`TodoNotFoundException`/`UserNotFoundException`/`NotificationNotFoundException`) nhưng xử lý thông qua kiểu cha, không cần 3 handler riêng biệt.
  - `model/TodoPriority.java` gắn `weight` (trọng số tính điểm năng suất) ngay trong từng hằng số enum (`LOW(1), MEDIUM(2), HIGH(3)`) qua `getWeight()`, thay cho một switch-case tra bảng thủ công trong `AnalyticsServiceImpl`.

Toàn bộ thay đổi trên đã chạy lại 41 unit test (`mvn test`, xem mục 12) cộng với hơn 40 kịch bản tích hợp thủ công (đăng nhập sai, thiếu quyền, dữ liệu không hợp lệ, xóa/khôi phục nhân viên, hoàn thành công việc kèm thông báo realtime...) để đảm bảo không có hồi quy.

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

> Ví dụ xuyên suốt tài liệu này dùng username `postgres`, password `29042003` — đó là Postgres trên máy tác giả. **Bạn phải thay bằng username/password Postgres thật của máy bạn**.

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
| `mvnw.cmd spring-boot:run` chạy tay trong `backend/` | **Biến môi trường của terminal**, KHÔNG phải file `.env` | Xem mục 5.3 |

**Nếu dùng `start.bat` hoặc Docker** — copy và sửa file `.env`:

```bash
cp .env.example .env
```

Mở file `.env` vừa tạo (ở thư mục gốc dự án), sửa 3 dòng đầu bằng thông tin Postgres thật của bạn:

```
DB_HOST=127.0.0.1
DB_USERNAME=postgres
DB_PASSWORD=<mật khẩu Postgres của BẠN — không phải 29042003 của tác giả>
```

Các dòng còn lại (`JWT_SECRET`, `SEED_LEADER_*`,...) có thể giữ nguyên giá trị mặc định, không bắt buộc sửa.

### 5.3. Nếu chạy `mvnw.cmd spring-boot:run` trực tiếp (không qua `start.bat`/Docker)

File `.env` ở thư mục gốc **không tự động được Spring Boot đọc** — phải khai báo biến môi trường trước khi chạy, ngay trong terminal đó:

```powershell
cd backend
$env:DB_HOST = "127.0.0.1"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "<mật khẩu Postgres của bạn>"
mvnw.cmd spring-boot:run
```

Muốn khỏi gõ lại mỗi lần mở terminal mới, set cố định một lần cho toàn tài khoản Windows (chỉ cần làm 1 lần, sau đó **mở cửa sổ terminal mới** để có hiệu lực):

```powershell
setx DB_HOST "127.0.0.1"
setx DB_USERNAME "postgres"
setx DB_PASSWORD "<mật khẩu Postgres của bạn>"
```

Sau bước này, từ mọi terminal mới, lệnh `cd backend && mvnw.cmd spring-boot:run` chạy được ngay không cần khai báo gì thêm.

### 5.4. Frontend

`frontend/.env` (khác với `.env` ở thư mục gốc) đã có sẵn `VITE_API_BASE_URL=http://localhost:8080` trỏ đúng vào backend chạy trên cùng máy — **không cần sửa gì** nếu chạy local theo hướng dẫn ở đây. Chỉ sửa file này nếu bạn đổi cổng backend hoặc deploy backend ở địa chỉ khác.

### 5.5. Chỉ cần nếu database đã tồn tại **trước** khi có trạng thái `ON_HOLD`

`ddl-auto=update` không tự sửa được ràng buộc CHECK đã có sẵn trên một bảng cũ (xem mục 13). Nếu bạn từng chạy dự án này trước khi tính năng "Tạm hoãn" được thêm vào, chạy 1 lần:

```bash
psql -U <username> -h <host> -d todo_db -f backend/migrations/001_add_on_hold_status.sql
```

Database tạo mới hoàn toàn không cần bước này (Hibernate tự sinh đúng ràng buộc với đủ 4 trạng thái ngay từ đầu).

## 6. Chạy nhanh nhất — một lệnh cho mỗi phần

Nếu chỉ muốn `cd` vào từng thư mục rồi gõ một lệnh duy nhất:

```bash
cd backend
mvnw.cmd spring-boot:run     # Windows — hoặc ./mvnw spring-boot:run trên macOS/Linux
```

```bash
cd frontend
npm start                    # tương đương npm run dev
```

Hai điều kiện để lệnh trên chạy được ngay không cần thêm gì:

- **Backend**: biến `DB_HOST` / `DB_USERNAME` / `DB_PASSWORD` phải có trong môi trường. Trên máy đã từng setup dự án này, các biến này đã được lưu cố định vào tài khoản Windows bằng `setx` nên **mọi cửa sổ terminal mới mở sau đó** đều tự nhận được — không cần export lại mỗi lần. Nếu là máy khác/lần đầu setup, xem mục 5.3.
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

```bash
cd backend
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run     # Windows: mvnw.cmd spring-boot:run — không cần cài Maven
```

Backend chạy tại `http://localhost:8080`. Tài khoản Leader mặc định (`leader`/`leader123`) được tạo tự động ở lần chạy đầu tiên.

> Đổi `postgres`/`postgres` ở trên bằng username/password Postgres thật của bạn — xem mục 5 nếu muốn set cố định một lần cho toàn bộ terminal thay vì `export` mỗi lần.

### 9.3. Frontend

```bash
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
| POST | `/users` | Leader | Tạo tài khoản nhân viên mới (có `position` — chức vụ, tùy chọn) |
| GET | `/users` | Leader | Danh sách nhân viên, gồm cả tài khoản đã vô hiệu hóa (`active: false`) |
| PUT | `/users/{id}` | Leader | Sửa họ tên / chức vụ |
| PATCH | `/users/{id}/password` | Leader | Đặt lại mật khẩu (khi nhân viên quên mật khẩu) |
| DELETE | `/users/{id}` | Leader | Xóa nhân viên — xóa hẳn nếu chưa từng được giao việc nào, ngược lại chỉ vô hiệu hóa (`active=false`): không đăng nhập được, không nhận việc mới, nhưng lịch sử công việc/năng suất vẫn giữ nguyên |

### Todos

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/todos` | Đã đăng nhập | Danh sách công việc (Leader thấy tất cả; Nhân viên chỉ thấy việc của mình dù truyền filter gì) |
| GET | `/todos/{id}` | Đã đăng nhập | Chi tiết một công việc (Nhân viên chỉ xem được việc của mình) |
| POST | `/todos` | Leader | Tạo và giao công việc cho một nhân viên |
| PUT | `/todos/{id}` | Leader | Cập nhật toàn bộ / giao lại cho người khác |
| PATCH | `/todos/{id}/status` | Leader hoặc chính nhân viên được giao | Đổi trạng thái — khi nhân viên chuyển sang `COMPLETED`, Leader tạo việc đó sẽ được thông báo real-time |
| DELETE | `/todos/{id}` | Leader | Xóa công việc |

Query params cho `GET /todos`: `keyword`, `status` (`PENDING`\|`IN_PROGRESS`\|`ON_HOLD`\|`COMPLETED`), `priority` (`LOW`\|`MEDIUM`\|`HIGH`), `assigneeId` (chỉ có tác dụng với Leader), `page` (mặc định 0), `size` (mặc định 10, tối đa 1000 — dùng 1000 cho tùy chọn "Tất cả" và cho bảng Kanban), `sortBy` (mặc định `createdAt`), `sortDir` (`ASC`\|`DESC`, mặc định `DESC`).

### Analytics (năng suất)

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/analytics/productivity` | Leader | Điểm năng suất từng nhân viên + tổng quan nhóm |

**Công thức điểm năng suất** (0-100/nhân viên): mỗi việc **không ở trạng thái `ON_HOLD`** đóng góp điểm khả dĩ theo độ ưu tiên (`LOW=1, MEDIUM=2, HIGH=3`); hoàn thành đúng hạn (hoặc không có hạn) nhận đủ điểm, hoàn thành trễ hạn chỉ nhận 50%, chưa xong thì chưa có điểm — `Điểm = tổng điểm đã nhận / tổng điểm khả dĩ × 100`. Việc **`ON_HOLD`** bị loại hoàn toàn khỏi công thức (không cộng vào điểm khả dĩ, không tính quá hạn) nên tạm hoãn một việc không bao giờ kéo điểm năng suất xuống. Chi tiết tại `AnalyticsServiceImpl`.

### Notifications

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/notifications` | Đã đăng nhập | Danh sách thông báo của tài khoản hiện tại |
| PATCH | `/notifications/{id}/read` | Đã đăng nhập (chỉ chủ sở hữu) | Đánh dấu một thông báo đã đọc |
| PATCH | `/notifications/read-all` | Đã đăng nhập | Đánh dấu **toàn bộ** thông báo chưa đọc của tài khoản hiện tại là đã đọc |

`NotificationResponseDTO` có trường `type` (`TASK_ASSIGNED` | `TASK_COMPLETED`) để phân biệt loại sự kiện — thông báo tạo trước khi trường này tồn tại sẽ mặc định là `TASK_COMPLETED` (loại duy nhất từng có).

### WebSocket (thông báo real-time)

- Endpoint: `ws(s)://<host>/ws` (STOMP over SockJS).
- Xác thực: gửi header `Authorization: Bearer <token>` trong STOMP `CONNECT` frame.
- Subscribe: `/user/queue/notifications` — nhận `NotificationResponseDTO` (JSON) mỗi khi có thông báo mới dành cho user đang kết nối.
- Có 2 sự kiện đẩy real-time theo 2 chiều:
  - **Leader → Nhân viên**: khi Leader giao việc mới (`type = TASK_ASSIGNED`) — nhân viên nhận ngay cả khi đang mở dashboard, không cần F5.
  - **Nhân viên → Leader**: khi nhân viên hoàn thành việc (`type = TASK_COMPLETED`) — Leader nhận ngay lập tức.
- Frontend dùng một `NotificationContext` (context/NotificationContext.jsx) giữ **một** kết nối WebSocket dùng chung cho cả chuông thông báo lẫn việc tự làm mới danh sách công việc — tránh mở nhiều kết nối trùng lặp và đảm bảo danh sách công việc cập nhật ngay khi có thông báo liên quan, không cần tải lại trang.

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
│   │   ├── controller/                   # AuthController, UserController, TodoController, NotificationController,
│   │   │                                 #   AnalyticsController
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
│   ├── migrations/                       # SQL thủ công cho thay đổi mà ddl-auto=update không xử lý được (mục 5.5)
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
│   │   ├── components/                   # Layout, TodoForm, TodoItem, TodoList, KanbanBoard, ViewToggle, TodoFilter,
│   │   │                                 #   Pagination, ConfirmDialog, Toast, NotificationBell, EmployeeManager,
│   │   │                                 #   ProductivityChart, ProtectedRoute
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

```bash
cd backend
./mvnw test     # Windows: mvnw.cmd test
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
| Thêm trạng thái `ON_HOLD` bị Postgres từ chối (`violates check constraint "todos_status_check"`) | Hibernate 6 sinh CHECK constraint liệt kê giá trị enum lúc tạo bảng; `ddl-auto=update` chỉ thêm bảng/cột thiếu, **không** sửa constraint đã tồn tại trên DB cũ | Restart backend sau khi thêm enum, thử set `ON_HOLD` thì lỗi ngay | Chạy `backend/migrations/001_add_on_hold_status.sql` để drop + tạo lại constraint với đủ 4 giá trị (xem mục 5.5) |
| Lỗi bảo mật nhỏ: đăng nhập tài khoản bị vô hiệu hóa trả về `"User is disabled"` trong `errors` | Message mặc định của Spring Security lộ trạng thái tài khoản, giúp kẻ tấn công phân biệt "sai mật khẩu" và "tài khoản bị khóa" để dò username | Đọc kỹ response khi test đăng nhập tài khoản đã xóa/vô hiệu hóa | `GlobalExceptionHandler` không còn echo `ex.getMessage()` cho `AuthenticationException`, luôn trả message chung "Tên đăng nhập hoặc mật khẩu không đúng" |
| Deploy lên Vercel: đăng nhập được, nhưng refresh trang hoặc bị redirect thẳng vào `/login`, `/leader` thì gặp **404: NOT_FOUND của chính Vercel** | App dùng React Router `BrowserRouter` (điều hướng phía client); Vercel không tự biết trả `index.html` cho các route không phải file vật lý | `curl` thẳng vào `/login`, `/leader` trên domain Vercel → nhận 404 dù backend test bằng `curl` API vẫn trả 200/201 bình thường | Thêm `frontend/vercel.json` với rewrite `"/(.*)" → "/index.html"` để mọi route đều load app rồi React Router tự xử lý phía client |
| Xóa một nhân viên đã hoàn thành việc nhưng việc đó sau đó bị Leader xóa đi → API trả lỗi 500 `violates foreign key constraint ... notifications` | Điều kiện quyết định hard-delete chỉ kiểm tra còn công việc đang được giao (`existsByAssignee`), không kiểm tra nhân viên có đang là "người thực hiện" (`actor`) trong lịch sử thông báo hay không | Viết kịch bản test tự động: giao việc → hoàn thành (sinh thông báo) → Leader xóa việc đó → xóa nhân viên | `UserServiceImpl#removeEmployee` kiểm tra thêm `notificationRepository.existsByActor(employee)` trước khi quyết định xóa hẳn hay chỉ vô hiệu hóa |
| Gọi API không kèm token (hoặc token rác) vào một endpoint bị chặn ở URL-level (`/api/v1/users/**`) nhận **401 lẫn lộn với 403** tùy tình huống, thân response rỗng | `AccessDeniedHandlerImpl` gọi `response.sendError(403)` → container forward nội bộ sang `/error`; do `/error` chưa được `permitAll()`, request forward đó bị chấm là "anonymous" và bị chặn lại lần hai, ghi đè luôn response gốc | Viết bộ test kịch bản bao trùm cả "không có token" lẫn "có token nhưng sai quyền", so sánh status code trả về | Thêm `.requestMatchers("/error").permitAll()` trong `SecurityConfig`, đồng thời thêm `RestAuthenticationEntryPoint` để trả JSON 401 nhất quán thay vì body rỗng của Spring Security |
| Gửi JSON sai định dạng (enum không hợp lệ, ví dụ `"priority":"INVALID"`) khiến API trả lỗi **500** thay vì 400 | `HttpMessageNotReadableException` (lỗi parse JSON của Jackson) không có handler riêng, rơi vào handler `Exception` chung trả 500 | Test kịch bản dữ liệu đầu vào sai định dạng thay vì chỉ thiếu trường | Thêm `@ExceptionHandler(HttpMessageNotReadableException.class)` trong `GlobalExceptionHandler` trả 400 với thông báo rõ ràng |
| Nhân viên chỉ thấy việc mới/thông báo hoàn thành sau khi **F5 lại trang**, dù backend đã đẩy WebSocket đúng | Hai nguyên nhân cộng lại: (1) `<NotificationBell />` chỉ được render khi `user.role === LEADER` trong `Layout.jsx` — nhân viên chưa từng có UI thông báo nào; (2) `useTodos` chỉ tự fetch lại sau chính hành động của nó (`addTodo`/`changeStatus`...), không có cơ chế nào lắng nghe sự kiện từ phiên làm việc khác | Đọc lại code frontend khi được yêu cầu làm thông báo real-time hai chiều (Leader giao việc → nhân viên, không chỉ nhân viên hoàn thành → Leader) | Bỏ điều kiện chỉ-Leader ở `Layout.jsx`; chuyển `useNotifications` thành `NotificationContext` dùng chung một kết nối WebSocket; thêm `useNotificationListener` để cả hai dashboard tự `fetchTodos()` ngay khi có thông báo liên quan đến mình |
| Sau khi thêm thông báo "giao việc mới" (nhân viên là *recipient*), xóa một nhân viên đã từng được giao việc (dù việc đó đã bị xóa) → API trả lỗi 500 `violates foreign key constraint ... notifications` | Cột `recipient_id` là `NOT NULL`; luồng xóa hẳn tài khoản (`force=true`) trước đó chỉ gỡ tham chiếu `actor` (`clearActor`), chưa từng phải lo tới `recipient` vì trước đây chỉ Leader mới là recipient (không bị xóa qua luồng này) — thêm loại thông báo mới khiến nhân viên lần đầu tiên cũng có thể là recipient | Chạy lại đúng 42 kịch bản test cũ sau khi thêm tính năng mới — bắt được ngay ở bước xóa hẳn nhân viên | Thêm `notificationRepository.deleteByRecipient(employee)` trước khi xóa tài khoản (force path), và tính thêm `existsByRecipient` khi quyết định vô hiệu hóa thay vì xóa hẳn |

## 14. Giới hạn đã biết / hướng cải tiến tiếp theo

- `ddl-auto=update` phù hợp cho demo/test nhưng không an toàn cho production lâu dài (không có migration có version như Flyway/Liquibase) — nếu dữ liệu cũ tồn tại trước khi thêm cột `NOT NULL` (`assignee_id`, `created_by_id`), migration tự động có thể thất bại. Dự án hiện dùng DB mới nên không gặp vấn đề, nhưng đây là điểm cần lưu ý khi mở rộng.
- Thông báo mới chỉ có 1 loại sự kiện (hoàn thành việc). Có thể mở rộng thêm: nhắc hạn chót sắp tới, thông báo khi được giao việc mới.
- Một task hiện chỉ giao được cho đúng 1 nhân viên (theo phạm vi đã thống nhất khi triển khai); mô hình nhiều người phụ trách 1 việc sẽ cần bảng trung gian `todo_assignees`.

## 15. Triển khai lên môi trường online (Vercel + Render)

**Đã triển khai** — xem link demo ở đầu README. Kiến trúc tách frontend/backend (mục 2) cho phép deploy 2 nơi khác nhau mà không cần sửa code — chỉ cấu hình biến môi trường: **frontend trên Vercel**, **backend + PostgreSQL trên Render**. Các bước dưới đây là hướng dẫn để bạn tự deploy lại (fork riêng, hoặc dựng lại từ đầu nếu cần).

### 15.1. Database (Render PostgreSQL)

New + → PostgreSQL → đặt tên, chọn region gần nhất → Create. Sau khi tạo xong, mở trang DB, lấy các thông tin trong mục **Connections**: Hostname, Port, Database, Username, Password.

### 15.2. Backend (Render Web Service)

New + → Web Service → Connect repository → chọn repo này.

| Cấu hình | Giá trị |
|---|---|
| Root Directory | `backend` |
| Runtime | Docker (Render tự nhận `backend/Dockerfile`) |

Environment Variables cần khai báo (điền từ thông tin DB ở mục 15.1):

| Biến | Giá trị |
|---|---|
| `DB_HOST` | Hostname của Render Postgres |
| `DB_PORT` | Port của Render Postgres (thường `5432`) |
| `DB_NAME` | Tên database |
| `DB_USERNAME` | Username |
| `DB_PASSWORD` | Password |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET` | Một chuỗi ngẫu nhiên dài (≥32 ký tự) — **không dùng giá trị mặc định trong `.env.example` cho production** |
| `CORS_ALLOWED_ORIGINS` | Tạm thời để `http://localhost:5173`, quay lại sửa sau khi có URL Vercel ở mục 15.4 |

Không cần khai báo `PORT` — Render tự gán và `application.properties` đã đọc đúng biến này (mục 5 chỉ áp dụng cho chạy local).

Deploy xong, copy URL dạng `https://<tên-service>.onrender.com`.

### 15.3. Frontend (Vercel)

Add New → Project → Import repo này.

| Cấu hình | Giá trị |
|---|---|
| Root Directory | `frontend` |
| Framework Preset | Vite (tự nhận) |
| Environment Variable | `VITE_API_BASE_URL` = URL backend Render ở mục 15.2 |

Deploy xong, copy URL dạng `https://<tên-project>.vercel.app`.

> **Bắt buộc có `frontend/vercel.json`** với nội dung `{"rewrites": [{"source": "/(.*)", "destination": "/index.html"}]}`. App dùng React Router `BrowserRouter` (điều hướng phía client) — nếu thiếu file này, Vercel không biết trả `index.html` cho các route như `/login`, `/leader`, `/employee` khi refresh trang hoặc bị redirect thẳng (`window.location.href`), gây lỗi **404: NOT_FOUND** ngay trên Vercel dù backend hoàn toàn bình thường. File này đã có sẵn trong repo.

### 15.4. Nối lại CORS

Quay lại Render → Web Service backend → Environment → sửa `CORS_ALLOWED_ORIGINS` thành URL Vercel thật (mục 15.3) → Save (Render tự redeploy).

> Đổi biến môi trường: Render tự áp dụng ngay (runtime), nhưng Vercel bake `VITE_API_BASE_URL` lúc build nên phải **Redeploy** thủ công nếu đổi giá trị này sau.

### 15.5. Lưu ý khi chạy free tier (đọc trước khi đánh giá qua bản demo online)

Bản demo online dùng **100% gói miễn phí** (Render free Web Service + free PostgreSQL, Vercel Hobby) — mục đích là để không tốn chi phí, không phải giới hạn kỹ thuật của code. Một số hiện tượng chậm/giật khi trải nghiệm bản demo là **đặc thù của hạ tầng miễn phí**, không phản ánh chất lượng hay hiệu năng thực của ứng dụng. Cụ thể:

- **Backend tự "ngủ" sau ~15 phút không có traffic** (giới hạn cứng của Render free Web Service, không có cách nào tắt ở gói free) — lần gọi API đầu tiên sau đó phải chờ Render khởi động lại container từ đầu. Trong lúc kiểm thử thực tế trên bản demo (không phải local), quan sát được:
  - Gọi thẳng API qua `curl` khi backend đang "ngủ": có lúc phải chờ tới ~90 giây mới nhận được phản hồi đầu tiên, những lần gọi tiếp theo trở lại bình thường (dưới 1 giây).
  - Đăng nhập qua giao diện ở trạng thái "ngủ": nút hiện "Đang đăng nhập..." và đứng yên 15-45 giây trước khi chuyển trang — **đây không phải app bị treo**, mà là đang chờ Render dựng lại container; đợi đủ lâu thao tác vẫn hoàn tất bình thường.
  - Kéo-thả Kanban đôi lúc bị lệch/không nhận do độ trễ round-trip Vercel ⇄ Render (khác region, khác nhà cung cấp) cộng dồn vào animation của thư viện drag-and-drop — thử lại thao tác kéo-thả lần 2 luôn thành công. API cập nhật trạng thái đứng sau thao tác này (`PATCH /todos/{id}/status`) đã được test riêng và luôn trả về đúng, không liên quan đến UI.
  - Mọi kết nối WebSocket (thông báo real-time) đang mở sẽ bị ngắt khi backend ngủ, và tự kết nối lại khi có thao tác mới sau khi backend thức dậy.
  - **Cách xác nhận nhanh đây là giới hạn hạ tầng chứ không phải bug**: gọi lại thao tác tương tự lần thứ 2 ngay sau lần đầu — luôn nhanh và mượt như local, vì container đã "thức". Hoặc chạy hẳn ở local theo mục 6/7 để thấy tốc độ thực khi không bị giới hạn bởi gói free.
- **Free PostgreSQL của Render có thể bị giới hạn thời gian tồn tại** theo chính sách hiện hành của Render (ví dụ tự xoá sau một số ngày nếu không nâng cấp) — nếu bản demo báo lỗi kết nối database, khả năng cao là DB free đã hết hạn theo chính sách của Render chứ không phải lỗi ứng dụng; kiểm tra trạng thái DB trên dashboard Render.
- Tài khoản Leader mặc định (`leader`/`leader123` hoặc theo `SEED_LEADER_*` đã đổi) tự tạo ngay lần khởi động đầu tiên trên Render, giống hệt khi chạy local.

Toàn bộ logic nghiệp vụ (backend) đã được xác nhận đúng trên chính bản deploy online này bằng 42 kịch bản kiểm thử tự động qua API (đăng nhập, phân quyền, validate dữ liệu, vòng đời nhân viên, thông báo real-time...) và một lượt kiểm thử trình duyệt đầy đủ qua Selenium — tất cả đều pass, chỉ riêng độ trễ mạng là khác biệt so với chạy local (xem mục 13 để biết các lỗi kỹ thuật thật đã phát hiện và sửa, phân biệt rõ với giới hạn hạ tầng free tier ở mục này).

---

Tác giả: Surp29.
