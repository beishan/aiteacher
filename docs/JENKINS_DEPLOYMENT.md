# 飞牛 NAS Jenkins 部署指南

本方案参照 `aibook` 项目的部署方式：Jenkins 在 NAS 本机使用 Docker 构建镜像，Docker Compose 原地更新服务；每个版本使用唯一镜像标签，部署前自动备份 PostgreSQL，失败时自动恢复上一版前后端镜像。

## 1. 前提条件

- 飞牛 NAS 已安装 Docker 和 Docker Compose v2。
- Jenkins 容器已安装 `docker` CLI、`bash`、`curl` 和 Git。
- Jenkins 容器挂载 NAS 的 Docker socket：`/var/run/docker.sock:/var/run/docker.sock`。
- Jenkins 运行用户有权访问 Docker socket。可在 Jenkins 任务中执行 `docker info` 验证。
- Jenkins 已配置代码仓库凭据，并能检出本项目。
- NAS 至少有 4 GB 可用内存；OnlyOffice 启动时资源占用较高，建议预留更多内存。

Jenkins 容器只需要持久化自己的工作目录和 Docker socket。应用数据使用以下 Docker 命名卷，不依赖 Jenkins 工作区：

| 数据 | Docker 卷 |
|---|---|
| PostgreSQL | `tutor-assist-postgres-data` |
| 资料文件 | `tutor-assist-materials` |
| OnlyOffice | `tutor-assist-onlyoffice-data`、`tutor-assist-onlyoffice-logs` |
| 应用内备份 | `tutor-assist-backups` |
| 部署前备份 | `tutor-assist-deploy-backups` |

不要执行 `docker compose down -v`，该命令会删除业务数据卷。

## 2. 创建生产环境凭据

1. 复制仓库根目录的 `.env.production.example`，填入真实密码和 NAS 局域网地址。
2. `JWT_SECRET` 至少 32 个字符；数据库和 OnlyOffice 密钥不要复用。
3. `ONLYOFFICE_PUBLIC_URL` 和 `BACKEND_PUBLIC_URL` 必须是浏览器及 OnlyOffice 可访问的 NAS 局域网地址，不能使用 `localhost`。
4. 在 Jenkins 中打开 **Manage Jenkins → Credentials**，新增 **Secret file**：
   - ID：`tutor-assist-production-env`
   - File：填写完成的生产环境文件
5. 删除本地包含真实密码的临时文件，或将其保存在受控的密码管理位置。

生产环境文件不会从仓库读取。流水线所有校验、构建和部署阶段均通过 Jenkins 临时凭据文件调用 `deploy.sh`。

## 3. 创建 Pipeline

在 Jenkins 新建 **Pipeline** 或 **Multibranch Pipeline**：

- Definition：`Pipeline script from SCM`
- SCM：Git
- Branch：通常为 `main`
- Script Path：`Jenkinsfile`

首次构建前确认参数：

| 参数 | 含义 |
|---|---|
| `NAS_HOST` | 飞牛 NAS 的局域网 IP 或域名，仅用于 Jenkins 从局域网入口验收 |
| `FRONTEND_PORT` | 前端入口端口，必须和生产环境文件一致 |
| `BACKEND_PORT` | 后端入口端口，必须和生产环境文件一致 |
| `SKIP_TESTS` | 默认跳过当前尚未适配安全配置的 WebMvc 测试；测试修复后可取消勾选 |

默认访问地址为：

- 前端：`http://NAS_HOST:8281/`
- 后端健康检查：`http://NAS_HOST:8282/actuator/health`
- OnlyOffice：`http://NAS_HOST:8283/`
数据库默认不暴露宿主机端口，只允许应用 Docker 网络内部访问。

## 4. 流水线行为

流水线依次执行：

1. 检出代码并生成 `版本号-构建号-提交号` 镜像标签。
2. 校验生产环境变量和最终 Compose 配置。
3. `SKIP_TESTS=false` 时，在 Docker 构建阶段运行后端测试。
4. 构建前后端版本镜像。
5. 记录当前镜像并备份 PostgreSQL。
6. 更新全部服务，等待容器健康。
7. 从 Jenkins 容器访问 NAS 的前端和后端入口。
8. 成功后清理更早镜像，同时保留当前版与上一版；失败时自动回滚。

## 5. 运维命令

在仓库工作区准备好 `.env.production` 后可手动运行：

```bash
./deploy.sh validate .env.production
./deploy.sh health .env.production
./deploy.sh logs .env.production
./deploy.sh rollback .env.production .tutor-assist-previous-images
```

查看服务和部署前数据库备份：

```bash
docker compose --env-file .env.production -f docker-compose.prod.yml ps
docker volume inspect tutor-assist-deploy-backups
```

首次部署没有上一版本镜像，因此无法回滚。数据库恢复属于高风险操作，流水线只负责创建备份，不会自动覆盖数据库。

资料文件保存在 `tutor-assist-materials` 卷。若旧版本已经把文件存入 MinIO，升级前需将 `materials` bucket 中的对象按原 key 复制到该卷；流水线不会删除旧 MinIO 卷，也不会自动迁移历史文件。

## 6. 常见问题

- **Jenkins 报 Docker 权限错误**：检查 Docker socket 是否挂载，并确认 Jenkins 容器用户的 GID 与 NAS 上 socket 所属组一致。
- **局域网健康检查失败但容器健康**：检查飞牛防火墙、端口映射、`NAS_HOST` 和 Jenkins 容器到 NAS 局域网 IP 的路由。
- **OnlyOffice 无法回调**：检查两个 `*_PUBLIC_URL` 是否使用客户端和容器都能访问的 NAS 地址，并确认 8282、8283 端口未被占用。
- **首次拉取镜像很慢**：PostgreSQL 和 OnlyOffice 基础镜像需要从 NAS 访问镜像仓库，可提前在 NAS 上执行 `docker compose pull`。
