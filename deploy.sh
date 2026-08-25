#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/docker-compose.prod.yml"
ACTION="${1:-deploy}"
ENV_FILE="${2:-${SCRIPT_DIR}/.env.production}"
STATE_FILE="${3:-${SCRIPT_DIR}/.tutor-assist-previous-images}"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-tutor-assist}"
BACKUP_VOLUME="${BACKUP_VOLUME:-tutor-assist-deploy-backups}"
BACKUP_RETENTION_COUNT="${BACKUP_RETENTION_COUNT:-10}"
HEALTH_RETRIES="${HEALTH_RETRIES:-36}"
HEALTH_INTERVAL_SECONDS="${HEALTH_INTERVAL_SECONDS:-5}"

if docker compose version >/dev/null 2>&1; then
    COMPOSE_COMMAND=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE_COMMAND=(docker-compose)
else
    echo "错误：未找到 docker compose 或 docker-compose。" >&2
    exit 1
fi

if [[ ! -r "${ENV_FILE}" ]]; then
    echo "错误：生产环境变量文件不可读：${ENV_FILE}" >&2
    exit 1
fi

env_file_value() {
    local key="$1"
    sed -n "s/^${key}=//p" "${ENV_FILE}" | tail -n 1
}

validate_environment() {
    local key
    local value
    local jwt_secret
    local required_keys=(
        DB_PASSWORD
        JWT_SECRET
        ONLYOFFICE_JWT_SECRET
        ONLYOFFICE_PUBLIC_URL
        BACKEND_PUBLIC_URL
    )

    for key in "${required_keys[@]}"; do
        value="$(env_file_value "${key}")"
        if [[ -z "${value}" ]]; then
            echo "错误：生产环境文件缺少 ${key}。" >&2
            return 1
        fi
        if [[ "${value}" == 请替换* ]]; then
            echo "错误：${key} 仍是示例占位值。" >&2
            return 1
        fi
    done

    jwt_secret="$(env_file_value JWT_SECRET)"
    if ((${#jwt_secret} < 32)); then
        echo "错误：JWT_SECRET 至少需要 32 个字符。" >&2
        return 1
    fi

    for key in ONLYOFFICE_PUBLIC_URL BACKEND_PUBLIC_URL; do
        value="$(env_file_value "${key}")"
        if [[ "${value}" == *localhost* || "${value}" == *127.0.0.1* ]]; then
            echo "错误：${key} 必须使用 NAS 局域网地址，不能使用 localhost。" >&2
            return 1
        fi
    done
}

compose() {
    "${COMPOSE_COMMAND[@]}" \
        --project-name "${COMPOSE_PROJECT_NAME}" \
        --env-file "${ENV_FILE}" \
        --file "${COMPOSE_FILE}" \
        "$@"
}

container_image() {
    docker inspect --format '{{.Config.Image}}' "$1" 2>/dev/null || true
}

container_health() {
    docker inspect \
        --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
        "$1" 2>/dev/null || echo missing
}

record_previous_images() {
    local backend_image
    local frontend_image
    backend_image="$(container_image tutor-backend)"
    frontend_image="$(container_image tutor-frontend)"

    {
        printf 'BACKEND_IMAGE=%q\n' "${backend_image}"
        printf 'FRONTEND_IMAGE=%q\n' "${frontend_image}"
    } > "${STATE_FILE}"

    echo "已记录部署前镜像："
    echo "  backend=${backend_image:-<首次部署>}"
    echo "  frontend=${frontend_image:-<首次部署>}"
}

backup_database() {
    local backup_name
    local db_name
    local db_user

    if ! docker inspect tutor-postgres >/dev/null 2>&1; then
        echo "PostgreSQL 容器尚未运行，跳过首次部署前备份。"
        return 0
    fi

    db_name="$(env_file_value DB_NAME)"
    db_user="$(env_file_value DB_USER)"
    db_name="${db_name:-tutor_assist}"
    db_user="${db_user:-tutor_assist}"
    backup_name="tutor-assist-$(date '+%Y%m%d-%H%M%S').dump"
    docker volume create "${BACKUP_VOLUME}" >/dev/null

    echo "正在备份 PostgreSQL：${backup_name}"
    docker exec tutor-postgres pg_dump -U "${db_user}" -d "${db_name}" -Fc |
        docker run --rm -i \
            -v "${BACKUP_VOLUME}:/backups" \
            postgres:15-alpine \
            sh -c "cat > '/backups/${backup_name}'"

    docker run --rm \
        -v "${BACKUP_VOLUME}:/backups" \
        postgres:15-alpine \
        sh -c "ls -1t /backups/tutor-assist-*.dump 2>/dev/null | awk 'NR > ${BACKUP_RETENTION_COUNT}' | while IFS= read -r file; do rm -f \"\$file\"; done"
}

wait_for_containers() {
    local attempt
    local all_ready
    local container_name
    local status
    local healthy_containers=(tutor-postgres tutor-backend tutor-frontend)
    local running_containers=(tutor-onlyoffice)

    for ((attempt = 1; attempt <= HEALTH_RETRIES; attempt++)); do
        all_ready=true
        echo "容器健康检查 ${attempt}/${HEALTH_RETRIES}"

        for container_name in "${healthy_containers[@]}"; do
            status="$(container_health "${container_name}")"
            echo "  ${container_name}: ${status}"
            if [[ "${status}" != healthy ]]; then
                all_ready=false
            fi
        done

        for container_name in "${running_containers[@]}"; do
            status="$(container_health "${container_name}")"
            echo "  ${container_name}: ${status}"
            if [[ "${status}" != running && "${status}" != healthy ]]; then
                all_ready=false
            fi
        done

        if [[ "${all_ready}" == true ]]; then
            return 0
        fi
        sleep "${HEALTH_INTERVAL_SECONDS}"
    done
    return 1
}

rollback_from_state() {
    local previous_backend_image
    local previous_frontend_image

    if [[ ! -r "${STATE_FILE}" ]]; then
        echo "错误：找不到回滚状态文件：${STATE_FILE}" >&2
        return 1
    fi

    # 此文件只由 record_previous_images 生成，内容已经过 shell 转义。
    # shellcheck disable=SC1090
    source "${STATE_FILE}"
    previous_backend_image="${BACKEND_IMAGE:-}"
    previous_frontend_image="${FRONTEND_IMAGE:-}"

    if [[ -z "${previous_backend_image}" || -z "${previous_frontend_image}" ]]; then
        echo "没有完整的上一版本镜像，无法自动回滚（通常发生在首次部署）。" >&2
        return 1
    fi

    export BACKEND_IMAGE="${previous_backend_image}"
    export FRONTEND_IMAGE="${previous_frontend_image}"
    echo "正在回滚到 ${BACKEND_IMAGE} / ${FRONTEND_IMAGE}"
    compose up -d --no-build backend frontend

    if ! wait_for_containers; then
        echo "错误：回滚后服务仍未恢复健康。" >&2
        compose logs --no-color --tail=200 backend frontend || true
        return 1
    fi
    echo "已恢复上一版本。"
}

deploy_release() {
    record_previous_images
    backup_database

    echo "正在更新 TutorAssist 服务。"
    if ! compose up -d --remove-orphans; then
        echo "Compose 更新失败，尝试恢复上一版本。" >&2
        compose logs --no-color --tail=300 backend || true
        rollback_from_state || true
        return 1
    fi

    if ! wait_for_containers; then
        echo "新版本健康检查失败，尝试恢复上一版本。" >&2
        compose logs --no-color --tail=200 || true
        rollback_from_state || true
        return 1
    fi
    echo "TutorAssist 新版本部署成功。"
}

cleanup_repository_images() {
    local repository="$1"
    local current_image="$2"
    local previous_image="$3"
    local image_ref

    if [[ -z "${current_image}" ]]; then
        echo "无法清理 ${repository}：当前运行镜像为空。" >&2
        return 1
    fi

    while IFS= read -r image_ref; do
        [[ -z "${image_ref}" ]] && continue
        if [[ "${image_ref}" == "${current_image}" || "${image_ref}" == "${previous_image}" ]]; then
            continue
        fi
        echo "清理旧镜像：${image_ref}"
        docker image rm "${image_ref}" >/dev/null 2>&1 || true
    done < <(
        docker image ls "${repository}" --format '{{.Repository}}:{{.Tag}}' |
            grep -v ':<none>$' | awk '!seen[$0]++'
    )
}

cleanup_images() {
    local previous_backend=""
    local previous_frontend=""
    if [[ -r "${STATE_FILE}" ]]; then
        # shellcheck disable=SC1090
        source "${STATE_FILE}"
        previous_backend="${BACKEND_IMAGE:-}"
        previous_frontend="${FRONTEND_IMAGE:-}"
    fi

    cleanup_repository_images \
        tutor-assist-backend "$(container_image tutor-backend)" "${previous_backend}"
    cleanup_repository_images \
        tutor-assist-frontend "$(container_image tutor-frontend)" "${previous_frontend}"
    docker image prune -f --filter label=com.tutor-assist.image-scope=backend >/dev/null
    docker image prune -f --filter label=com.tutor-assist.image-scope=frontend >/dev/null
}

validate_environment

case "${ACTION}" in
    validate)
        compose config --quiet
        echo "生产环境变量与 Docker Compose 配置校验通过。"
        ;;
    test)
        docker build \
            --file "${SCRIPT_DIR}/backend/Dockerfile" \
            --target test \
            --tag "tutor-assist-backend-test:${RELEASE_TAG:-local}" \
            "${SCRIPT_DIR}/backend"
        ;;
    build)
        compose build backend frontend
        ;;
    deploy)
        deploy_release
        ;;
    health)
        wait_for_containers
        ;;
    cleanup)
        cleanup_images
        ;;
    rollback)
        rollback_from_state
        ;;
    logs)
        compose logs --no-color --tail=200
        ;;
    *)
        echo "用法：$0 {validate|test|build|deploy|health|cleanup|rollback|logs} [env-file] [state-file]" >&2
        exit 2
        ;;
esac
