# AI家教辅助系统

请先阅读 requires_01.md 项目需求文件

## 项目概述

家教助手系统（TutorAssist）是面向家教老师的一站式数字化教学管理平台，涵盖学生管理、排课管理、作业管理、成绩管理、资料库管理等核心功能，并集成 AI 辅助教学能力（作业批改、学情分析、智能问答）。

## 技术栈

| 层次 | 技术选型 |
|------|---------|
| 前端 | Vue 3 + Vite + TypeScript + Element Plus + Pinia + FullCalendar |
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus + Spring Security |
| 数据库 | PostgreSQL 15 |
| 缓存 | Redis 7 |
| 文件存储 | MinIO（本地部署） |
| 部署 | Docker + Docker Compose（飞牛NAS） |
| AI | SpringAI（Claude / GPT / Minimax / Qwen / Ollama） |

## 项目结构

```
ai-teacher-helper/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/tutorassist/
│   │   ├── TutorAssistApplication.java
│   │   ├── config/                   # 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── MyBatisPlusConfig.java
│   │   │   └── DataInitializer.java
│   │   ├── common/                   # 通用类
│   │   │   ├── BaseEntity.java
│   │   │   ├── Result.java
│   │   │   ├── PageResult.java
│   │   │   └── exception/
│   │   ├── auth/                     # 认证模块
│   │   │   ├── controller/AuthController.java
│   │   │   ├── service/AuthService.java
│   │   │   ├── entity/User.java
│   │   │   ├── mapper/UserMapper.java
│   │   │   ├── dto/
│   │   │   └── filter/JwtAuthenticationFilter.java
│   │   ├── student/                  # 学生管理模块
│   │   │   ├── controller/StudentController.java
│   │   │   ├── service/StudentService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── schedule/                 # 排课管理模块
│   │   │   ├── controller/CourseController.java
│   │   │   ├── service/CourseService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── classroom/                # 虚拟班级模块
│   │   │   ├── controller/ClassroomController.java
│   │   │   ├── service/ClassroomService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── homework/                 # 作业管理模块
│   │   │   ├── controller/HomeworkController.java
│   │   │   ├── service/HomeworkService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── grade/                    # 成绩管理模块
│   │   │   ├── controller/ExamController.java
│   │   │   ├── service/ExamService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── material/                 # 资料库模块
│   │   │   ├── controller/MaterialController.java
│   │   │   ├── service/MaterialService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── ai/                       # AI 功能模块
│   │   │   ├── controller/
│   │   │   │   ├── AIController.java
│   │   │   │   └── SettingsController.java
│   │   │   ├── service/
│   │   │   │   ├── AIChatService.java
│   │   │   │   ├── AIHomeworkService.java
│   │   │   │   └── AITeachingPlanService.java
│   │   │   ├── gateway/
│   │   │   │   ├── AIGateway.java
│   │   │   │   ├── AIGatewayFactory.java
│   │   │   │   └── impl/
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── notification/             # 通知模块
│   │   │   ├── controller/NotificationController.java
│   │   │   ├── service/
│   │   │   │   ├── NotificationService.java
│   │   │   │   └── WeChatService.java
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   └── dto/
│   │   ├── statistics/               # 统计模块
│   │   │   ├── controller/StatisticsController.java
│   │   │   ├── service/StatisticsService.java
│   │   │   └── dto/
│   │   └── util/JwtUtil.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── db/migration/             # Flyway 迁移脚本（V1~V12）
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                         # Vue 3 前端
│   ├── src/
│   │   ├── main.ts
│   │   ├── App.vue
│   │   ├── router/index.ts
│   │   ├── stores/user.ts
│   │   ├── api/
│   │   │   ├── request.ts            # Axios 封装
│   │   │   ├── auth.ts
│   │   │   ├── student.ts
│   │   │   ├── course.ts
│   │   │   ├── classroom.ts
│   │   │   ├── homework.ts
│   │   │   ├── exam.ts
│   │   │   ├── material.ts
│   │   │   ├── ai.ts
│   │   │   ├── settings.ts
│   │   │   ├── notification.ts
│   │   │   └── statistics.ts
│   │   └── views/
│   │       ├── login/LoginView.vue
│   │       ├── layout/
│   │       │   ├── MainLayout.vue
│   │       │   ├── SideMenu.vue
│   │       │   └── TopBar.vue
│   │       ├── dashboard/DashboardView.vue
│   │       ├── student/
│   │       │   ├── StudentListView.vue
│   │       │   ├── StudentDetailView.vue
│   │       │   └── components/
│   │       │       └── StudentForm.vue
│   │       ├── schedule/
│   │       │   ├── ScheduleView.vue
│   │       │   └── components/
│   │       │       ├── CourseForm.vue
│   │       │       └── CourseDetail.vue
│   │       ├── homework/
│   │       │   ├── HomeworkListView.vue
│   │       │   ├── HomeworkDetailView.vue
│   │       │   └── components/
│   │       │       └── HomeworkForm.vue
│   │       ├── classroom/
│   │       │   ├── ClassroomListView.vue
│   │       │   └── components/
│   │       │       └── ClassroomForm.vue
│   │       ├── grade/
│   │       │   ├── GradeListView.vue
│   │       │   └── components/
│   │       │       └── GradeForm.vue
│   │       ├── material/
│   │       │   ├── MaterialListView.vue
│   │       │   └── components/
│   │       │       └── MaterialForm.vue
│   │       ├── ai/
│   │       │   ├── AIChatView.vue
│   │       │   └── components/
│   │       │       └── ChatMessage.vue
│   │       ├── settings/
│   │       │   └── SettingsView.vue
│   │       ├── statistics/
│   │       │   └── StatisticsView.vue
│   │       └── placeholder/PlaceholderView.vue
│   ├── package.json
│   ├── vite.config.ts
│   ├── nginx.conf
│   └── Dockerfile
├── docker-compose.yml
├── .env
├── .gitignore
├── CALUDE.md                         # 本文件
└── requires_01.md                    # 需求文档
```

## 常用命令

### Docker 部署
```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 重新构建并启动
docker-compose up -d --build

# 停止所有服务
docker-compose down
```

### 后端开发
```bash
cd backend

# 编译
mvn clean package

# 运行（开发环境）
mvn spring-boot:run

# 运行测试
mvn test
```

### 前端开发
```bash
cd frontend

# 安装依赖
npm install

# 开发服务器
npm run dev

# 构建生产版本
npm run build
```

## 数据库

使用 PostgreSQL，通过 Docker Compose 启动。

关键数据表：

| 表名 | 说明 | 状态 |
|------|------|------|
| `users` | 用户表（管理员账号） | ✅ 已创建 |
| `students` | 学生信息表 | ✅ 已创建 |
| `student_fees` | 学生课时费 | ✅ 已创建 |
| `fee_records` | 费用记录 | ✅ 已创建 |
| `courses` | 课程排课表 | ✅ 已创建 |
| `course_records` | 上课记录 | ✅ 已创建 |
| `virtual_classes` | 虚拟班级 | ✅ 已创建 |
| `class_members` | 班级成员 | ✅ 已创建 |
| `homeworks` | 作业表 | ✅ 已创建 |
| `homework_submissions` | 作业提交 | ✅ 已创建 |
| `exam_records` | 考试成绩 | ✅ 已创建 |
| `materials` | 资料表 | ✅ 已创建 |
| `material_versions` | 资料版本 | ✅ 已创建 |
| `student_materials` | 学生专属资料 | ✅ 已创建 |
| `ai_records` | AI 调用记录 | ✅ 已创建 |
| `system_settings` | 系统设置 | ✅ 已创建 |
| `notifications` | 通知记录 | ✅ 已创建 |

默认管理员账号：`admin / admin123`（首次启动自动创建）

## API 接口

### 认证管理
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/login` | 用户登录 |
| GET | `/api/v1/auth/me` | 获取当前用户信息 |
| PUT | `/api/v1/auth/password` | 修改密码 |

### 学生管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/students` | 学生列表（分页、筛选） |
| GET | `/api/v1/students/{id}` | 学生详情 |
| POST | `/api/v1/students` | 新增学生 |
| PUT | `/api/v1/students/{id}` | 更新学生 |
| DELETE | `/api/v1/students/{id}` | 删除学生 |
| PUT | `/api/v1/students/{id}/status` | 修改学生状态 |
| GET | `/api/v1/students/{id}/fees` | 学生课时费列表 |
| POST | `/api/v1/students/{id}/fees` | 新增课时费 |
| PUT | `/api/v1/students/{id}/fees/{feeId}` | 更新课时费 |
| GET | `/api/v1/students/{id}/fee-records` | 费用记录列表 |
| POST | `/api/v1/students/{id}/fee-records` | 新增费用记录 |

### 排课管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/courses` | 课程列表 |
| GET | `/api/v1/courses/{id}` | 课程详情 |
| POST | `/api/v1/courses` | 新增课程（支持周期排课） |
| PUT | `/api/v1/courses/{id}` | 更新课程 |
| DELETE | `/api/v1/courses/{id}` | 删除课程 |
| PUT | `/api/v1/courses/{id}/status` | 修改课程状态 |
| POST | `/api/v1/courses/{id}/complete` | 完成课程（生成上课记录） |
| GET | `/api/v1/courses/{id}/records` | 课程上课记录 |
| GET | `/api/v1/courses/calendar` | 日历视图数据 |
| GET | `/api/v1/courses/conflicts` | 冲突检测 |

### 虚拟班级管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/classrooms` | 班级列表 |
| POST | `/api/v1/classrooms` | 创建班级 |
| PUT | `/api/v1/classrooms/{id}` | 更新班级 |
| DELETE | `/api/v1/classrooms/{id}` | 删除班级 |
| PUT | `/api/v1/classrooms/{id}/status` | 修改班级状态 |
| GET | `/api/v1/classrooms/{id}/members` | 成员列表 |
| POST | `/api/v1/classrooms/{id}/members` | 添加成员 |
| DELETE | `/api/v1/classrooms/{id}/members/{studentId}` | 移除成员 |

### 作业管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/homeworks` | 作业列表 |
| GET | `/api/v1/homeworks/{id}` | 作业详情 |
| POST | `/api/v1/homeworks` | 创建作业 |
| PUT | `/api/v1/homeworks/{id}` | 更新作业 |
| DELETE | `/api/v1/homeworks/{id}` | 删除作业 |
| POST | `/api/v1/homeworks/{id}/submit` | 提交作业 |
| GET | `/api/v1/homeworks/{id}/submissions` | 查看提交记录 |
| PUT | `/api/v1/homeworks/{id}/grade` | 批改作业 |

### 成绩管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/exams` | 成绩列表 |
| GET | `/api/v1/exams/{id}` | 成绩详情 |
| POST | `/api/v1/exams` | 新增成绩 |
| PUT | `/api/v1/exams/{id}` | 更新成绩 |
| DELETE | `/api/v1/exams/{id}` | 删除成绩 |
| GET | `/api/v1/exams/student/{studentId}` | 学生历次成绩 |
| GET | `/api/v1/exams/student/{studentId}/trend` | 成绩趋势数据 |

### 资料库管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/materials` | 资料列表 |
| GET | `/api/v1/materials/{id}` | 资料详情 |
| POST | `/api/v1/materials` | 创建资料/文件夹 |
| PUT | `/api/v1/materials/{id}` | 更新资料 |
| DELETE | `/api/v1/materials/{id}` | 删除资料 |
| POST | `/api/v1/materials/{id}/favorite` | 收藏/取消收藏 |
| GET | `/api/v1/materials/{id}/versions` | 版本历史 |
| POST | `/api/v1/materials/{id}/assign` | 分配给学生 |
| GET | `/api/v1/materials/student/{studentId}` | 学生专属资料 |

### AI 辅助功能
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/ai/chat` | AI 智能问答 |
| POST | `/api/v1/ai/grade-homework` | AI 批改作业 |
| POST | `/api/v1/ai/teaching-plan` | AI 生成教学计划 |

### 系统设置
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/settings` | 获取所有设置 |
| GET | `/api/v1/settings/{key}` | 获取单个设置 |
| PUT | `/api/v1/settings` | 更新设置 |

### 通知管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/notifications` | 通知列表 |
| GET | `/api/v1/notifications/unread-count` | 未读数量 |
| PUT | `/api/v1/notifications/{id}/read` | 标记已读 |
| POST | `/api/v1/notifications/read-all` | 全部已读 |

### 数据统计
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/statistics/dashboard` | 首页仪表盘数据 |
| GET | `/api/v1/statistics/revenue` | 收入统计 |
| GET | `/api/v1/statistics/students` | 学生分布统计 |
| GET | `/api/v1/statistics/courses` | 课时统计 |
| GET | `/api/v1/statistics/homework` | 作业统计 |

Swagger 文档：`http://localhost:8080/swagger-ui.html`

## 开发规范

### 代码风格
- **Java**: 遵循 Google Java Style Guide
- **Vue/TypeScript**: 使用 Composition API + `<script setup>`

### Git 提交
- 使用中文提交信息
- 格式: `<类型>: <描述>`
- 类型: feat, fix, refactor, docs, style, test, chore

### 分支策略
- `main` - 生产分支
- `develop` - 开发分支
- `feature/*` - 功能分支
- `fix/*` - 修复分支

## 环境变量

关键环境变量在 `.env` 文件中配置：

```bash
# 数据库密码
DB_PASSWORD=memoryvault
REDIS_PASSWORD=memoryvault
MINIO_PASSWORD=memoryvault

# JWT 密钥（生产环境必须修改）
JWT_SECRET=your-secret-key
```

## 硬件要求

- **开发环境**: macOS/Linux，8GB+ RAM
- **生产环境**: 飞牛 NAS，Docker 部署

## 开发路线图

| 阶段 | 周期 | 状态 | 主要交付物 |
|------|------|------|-----------|
| Phase 1：基础框架 | 第 1-2 周 | ✅ 已完成 | 项目脚手架、数据库设计、用户认证、基础 UI 框架 |
| Phase 2：学生管理 | 第 3-4 周 | ✅ 已完成 | 学生 CRUD、课时费模块、学生状态管理 |
| Phase 3：排课系统 | 第 5-7 周 | ✅ 已完成 | 排课功能、日历视图、冲突检测、拖拽调课 |
| Phase 4：作业管理 | 第 8-9 周 | ✅ 已完成 | 作业创建/提交/批改/评分、虚拟班级管理 |
| Phase 5：成绩&资料 | 第 10-11 周 | ✅ 已完成 | 成绩管理、资料库、版本管理、收藏功能 |
| Phase 6：AI 功能 | 第 12-14 周 | ✅ 已完成 | AI 网关、智能问答、作业批改、教学计划、系统设置 |
| Phase 7：通知&统计 | 第 15 周 | ✅ 已完成 | 通知系统、企业微信推送、数据统计报表 |
| Phase 8：测试&优化 | 第 16 周 | ✅ 已完成 | 单元测试、安全加固、API 限流、操作日志、部署文档 |

## 开发规范

- 回答问题称呼我为"老板"
