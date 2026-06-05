# 家教助手系统 (TutorAssist)

一套完整的家教管理数字化平台，覆盖学生、排课、作业、成绩、资料全流程管理，并集成 AI 辅助教学功能。

## 功能特性

### 核心功能
- 👨‍🎓 **学生管理** - 学生信息、课时费、状态追踪
- 📅 **排课管理** - 日历视图、周期排课、冲突检测、拖拽调课
- 📝 **作业管理** - 创建、提交、批改、评分评语
- 🏫 **虚拟班级** - 班级创建、成员管理、统一排课
- 📊 **成绩管理** - 成绩记录、趋势分析
- 📁 **资料库** - 文件管理、版本控制、收藏功能

### AI 功能
- 🤖 **AI 智能问答** - 自然语言查询系统信息
- ✍️ **AI 批改作业** - OCR 识别、逐题批改、详解生成
- 📋 **AI 教学计划** - 基于学情自动生成个性化计划

### 系统功能
- 🔔 **通知系统** - 站内通知、企业微信推送
- 📈 **数据统计** - 收入、课时、学生分布统计
- ⚙️ **系统设置** - AI 模型配置、通知配置

## 技术栈

| 层次 | 技术选型 |
|------|---------|
| 前端 | Vue 3 + Vite + TypeScript + Element Plus + Pinia + FullCalendar |
| 后端 | Java 17 + Spring Boot 3.2 + MyBatis-Plus + Spring Security |
| 数据库 | PostgreSQL 15 |
| 缓存 | Redis 7 |
| 文件存储 | MinIO |
| 部署 | Docker + Docker Compose |
| AI | SpringAI（Claude / GPT / Ollama） |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- Docker & Docker Compose
- 8GB+ RAM

### 本地开发

1. **克隆项目**
```bash
git clone <repository-url>
cd ai-teacher-helper
```

2. **启动数据库**
```bash
docker-compose up -d postgres redis
```

3. **启动后端**
```bash
cd backend
mvn spring-boot:run
```

4. **启动前端**
```bash
cd frontend
npm install
npm run dev
```

5. **访问系统**
- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html

**默认账号**：admin / admin123

### Docker 部署

```bash
# 启动所有服务
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 项目结构

```
ai-teacher-helper/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/          # Java 源码
│   ├── src/main/resources/     # 配置文件、迁移脚本
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   └── package.json
├── docker-compose.yml          # Docker 编排
├── .env                        # 环境变量
└── README.md                   # 本文件
```

## 数据库

使用 PostgreSQL，通过 Flyway 管理数据库迁移。

### 数据表

| 表名 | 说明 |
|------|------|
| users | 用户表 |
| students | 学生信息表 |
| student_fees | 学生课时费 |
| fee_records | 费用记录 |
| courses | 课程排课表 |
| course_records | 上课记录 |
| virtual_classes | 虚拟班级 |
| class_members | 班级成员 |
| homeworks | 作业表 |
| homework_submissions | 作业提交 |
| exam_records | 考试成绩 |
| materials | 资料表 |
| material_versions | 资料版本 |
| student_materials | 学生专属资料 |
| ai_records | AI 调用记录 |
| system_settings | 系统设置 |
| notifications | 通知记录 |

## API 文档

启动后端后访问：http://localhost:8080/swagger-ui.html

### 主要 API 模块

- `/api/v1/auth` - 认证管理
- `/api/v1/students` - 学生管理
- `/api/v1/courses` - 排课管理
- `/api/v1/homeworks` - 作业管理
- `/api/v1/classrooms` - 虚拟班级
- `/api/v1/exams` - 成绩管理
- `/api/v1/materials` - 资料库
- `/api/v1/ai` - AI 功能
- `/api/v1/notifications` - 通知管理
- `/api/v1/statistics` - 数据统计
- `/api/v1/settings` - 系统设置

## 环境变量

在 `.env` 文件中配置：

```bash
# 数据库
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tutor_assist
DB_USER=postgres
DB_PASSWORD=memoryvault

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=memoryvault

# JWT
JWT_SECRET=your-secret-key-here

# AI 模型
AI_DEFAULT_MODEL=claude
CLAUDE_API_KEY=your-claude-api-key
OPENAI_API_KEY=your-openai-api-key
```

## 开发规范

### 代码风格
- **Java**: Google Java Style Guide
- **Vue/TypeScript**: Composition API + `<script setup>`

### Git 提交
```
<类型>: <描述>

类型：feat, fix, refactor, docs, style, test, chore
```

### 分支策略
- `main` - 生产分支
- `develop` - 开发分支
- `feature/*` - 功能分支
- `fix/*` - 修复分支

## 部署到飞牛 NAS

1. 构建 Docker 镜像
```bash
docker-compose build
```

2. 导出镜像
```bash
docker save tutor-assist-backend tutor-assist-frontend > tutor-assist.tar
```

3. 传输到 NAS 并加载
```bash
scp tutor-assist.tar user@nas:/path/to/
ssh user@nas "docker load < /path/to/tutor-assist.tar"
```

4. 启动服务
```bash
ssh user@nas "cd /path/to/project && docker-compose up -d"
```

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue 或联系开发者。
