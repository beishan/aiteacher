#!/bin/bash
# ============================================================
# TutorAssist NAS 部署脚本
# 用途：在飞牛 NAS 上通过 Jenkins 构建和部署家教助手系统
# 使用方式：Jenkins Pipeline 调用，或手动执行
# ============================================================

set -e

# ==================== 配置区 ====================
# 部署目录（NAS 上的项目路径）
DEPLOY_DIR="${DEPLOY_DIR:-/opt/tutor-assist}"
# 镜像名前缀
IMAGE_PREFIX="tutor-assist"
# 数据备份目录
BACKUP_DIR="${DEPLOY_DIR}/backups"
# 是否保留数据库备份（默认保留最近 5 个）
MAX_BACKUPS=5
# 日志文件
LOG_FILE="${DEPLOY_DIR}/deploy.log"

# ==================== 工具函数 ====================
log() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] $1"
    echo "$msg"
    echo "$msg" >> "$LOG_FILE"
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        log "❌ Docker 未安装"
        exit 1
    fi
    if ! docker info &> /dev/null; then
        log "❌ Docker 守护进程未运行"
        exit 1
    fi
    log "✓ Docker 环境正常"
}

check_compose() {
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
    else
        log "❌ Docker Compose 未安装"
        exit 1
    fi
    log "✓ Compose 命令: $COMPOSE_CMD"
}

# ==================== 核心功能 ====================

# 备份数据库
backup_database() {
    log "=== 备份数据库 ==="
    mkdir -p "$BACKUP_DIR"

    local backup_file="${BACKUP_DIR}/db_backup_$(date '+%Y%m%d_%H%M%S').sql"

    # 检查 postgres 容器是否运行
    if docker ps --format '{{.Names}}' | grep -q "^tutor-postgres$"; then
        docker exec tutor-postgres pg_dump -U postgres tutor_assist > "$backup_file" 2>/dev/null
        log "✓ 数据库已备份到: $backup_file"

        # 清理旧备份，保留最近 N 个
        local count=$(ls -1 "$BACKUP_DIR"/db_backup_*.sql 2>/dev/null | wc -l)
        if [ "$count" -gt "$MAX_BACKUPS" ]; then
            ls -1t "$BACKUP_DIR"/db_backup_*.sql | tail -n +$((MAX_BACKUPS + 1)) | xargs rm -f
            log "✓ 已清理旧备份，保留最近 $MAX_BACKUPS 个"
        fi
    else
        log "⚠ PostgreSQL 未运行，跳过数据库备份"
    fi
}

# 构建镜像
build_images() {
    log "=== 构建 Docker 镜像 ==="
    cd "$DEPLOY_DIR"

    log "构建后端镜像..."
    docker build -t "${IMAGE_PREFIX}-backend:latest" ./backend
    log "✓ 后端镜像构建完成"

    log "构建前端镜像..."
    docker build -t "${IMAGE_PREFIX}-frontend:latest" ./frontend
    log "✓ 前端镜像构建完成"
}

# 停止服务
stop_services() {
    log "=== 停止旧服务 ==="
    cd "$DEPLOY_DIR"

    $COMPOSE_CMD -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
    log "✓ 旧服务已停止"
}

# 启动服务
start_services() {
    log "=== 启动新服务 ==="
    cd "$DEPLOY_DIR"

    # 加载环境变量
    if [ -f .env.prod ]; then
        export $(grep -v '^#' .env.prod | xargs)
    fi

    $COMPOSE_CMD -f docker-compose.prod.yml up -d
    log "✓ 服务启动命令已执行"
}

# 健康检查
health_check() {
    log "=== 健康检查 ==="
    local max_wait=120
    local waited=0

    # 等待后端就绪
    log "等待后端服务就绪..."
    while [ $waited -lt $max_wait ]; do
        if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1 || \
           curl -sf http://localhost:8080/api/v1/auth/login > /dev/null 2>&1; then
            log "✓ 后端服务已就绪 (${waited}s)"
            break
        fi
        sleep 5
        waited=$((waited + 5))
        echo -n "."
    done

    if [ $waited -ge $max_wait ]; then
        log "❌ 后端服务启动超时 (${max_wait}s)"
        docker logs tutor-backend --tail 30
        return 1
    fi

    # 检查所有容器
    local all_ok=true
    for svc in tutor-postgres tutor-redis tutor-backend tutor-frontend; do
        if docker ps --format '{{.Names}}' | grep -q "^${svc}$"; then
            log "✓ $svc 运行正常"
        else
            log "✗ $svc 未运行"
            docker logs $svc --tail 20 2>&1 || true
            all_ok=false
        fi
    done

    # 检查前端
    if curl -sf http://localhost:80 > /dev/null 2>&1; then
        log "✓ 前端页面可访问"
    else
        log "⚠ 前端页面暂时不可访问"
    fi

    if [ "$all_ok" = true ]; then
        log "✅ 所有服务运行正常"
        return 0
    else
        log "❌ 部分服务异常"
        return 1
    fi
}

# 清理旧镜像
cleanup() {
    log "=== 清理旧镜像 ==="
    docker image prune -f
    docker builder prune -f 2>/dev/null || true
    log "✓ 清理完成"
}

# 显示服务状态
show_status() {
    log "=== 服务状态 ==="
    cd "$DEPLOY_DIR"
    $COMPOSE_CMD -f docker-compose.prod.yml ps
    echo ""
    log "访问地址:"
    log "  前端: http://localhost:80"
    log "  后端: http://localhost:8080"
    log "  API文档: http://localhost:8080/swagger-ui.html"
}

# 回滚到上一版本
rollback() {
    log "=== 回滚到上一版本 ==="
    cd "$DEPLOY_DIR"

    # 检查是否有备份镜像
    if docker images "${IMAGE_PREFIX}-backend:previous" --format '{{.Repository}}:{{.Tag}}' | grep -q .; then
        docker tag "${IMAGE_PREFIX}-backend:previous" "${IMAGE_PREFIX}-backend:latest"
        docker tag "${IMAGE_PREFIX}-frontend:previous" "${IMAGE_PREFIX}-frontend:latest"
        log "✓ 已切换到上一版本镜像"

        stop_services
        start_services
        health_check
    else
        log "❌ 没有可回滚的版本"
        exit 1
    fi
}

# ==================== 主流程 ====================
main() {
    local action="${1:-deploy}"

    mkdir -p "$DEPLOY_DIR" "$BACKUP_DIR"
    touch "$LOG_FILE"

    log "=========================================="
    log "TutorAssist 部署 - 操作: $action"
    log "=========================================="

    check_docker
    check_compose

    case "$action" in
        deploy)
            # 完整部署流程
            backup_database
            # 保存当前镜像作为回滚版本
            docker tag "${IMAGE_PREFIX}-backend:latest" "${IMAGE_PREFIX}-backend:previous" 2>/dev/null || true
            docker tag "${IMAGE_PREFIX}-frontend:latest" "${IMAGE_PREFIX}-frontend:previous" 2>/dev/null || true
            build_images
            stop_services
            start_services
            health_check
            cleanup
            show_status
            ;;
        build)
            build_images
            ;;
        start)
            start_services
            show_status
            ;;
        stop)
            stop_services
            ;;
        restart)
            stop_services
            start_services
            health_check
            ;;
        status)
            show_status
            ;;
        backup)
            backup_database
            ;;
        rollback)
            rollback
            ;;
        *)
            echo "用法: $0 {deploy|build|start|stop|restart|status|backup|rollback}"
            echo ""
            echo "  deploy   - 完整部署（备份→构建→停止→启动→检查→清理）"
            echo "  build    - 仅构建镜像"
            echo "  start    - 启动服务"
            echo "  stop     - 停止服务"
            echo "  restart  - 重启服务"
            echo "  status   - 查看服务状态"
            echo "  backup   - 备份数据库"
            echo "  rollback - 回滚到上一版本"
            exit 1
            ;;
    esac

    log "=========================================="
    log "操作完成: $action"
    log "=========================================="
}

main "$@"
