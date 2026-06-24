pipeline {
    agent {
        docker {
            image 'docker:latest'
            args '-v /var/run/docker.sock:/var/run/docker.sock --privileged'
        }
    }

    environment {
        // NAS 上的项目部署目录（Jenkins 容器内挂载的目录）
        DEPLOY_DIR = '/opt/tutor-assist'
        // 项目镜像名前缀
        IMAGE_PREFIX = 'tutor-assist'
        // docker-compose 项目文件路径
        COMPOSE_FILE = "${DEPLOY_DIR}/docker-compose.prod.yml"
    }

    parameters {
        choice(
            name: 'DEPLOY_ENV',
            choices: ['production', 'staging'],
            description: '部署环境'
        )
        booleanParam(
            name: 'SKIP_BUILD',
            defaultValue: false,
            description: '跳过构建，直接使用已有镜像'
        )
        booleanParam(
            name: 'CLEANUP',
            defaultValue: true,
            description: '清理旧镜像和悬空镜像'
        )
    }

    stages {
        stage('拉取代码') {
            steps {
                checkout scm
                // 如果不用 SCM，改为：
                // git branch: 'main',
                //     url: 'https://gitee.com/your-repo/aiteacher.git'
            }
        }

        stage('复制部署文件') {
            steps {
                sh """
                    mkdir -p ${DEPLOY_DIR}
                    cp -r . ${DEPLOY_DIR}/
                    echo "部署文件已复制到 ${DEPLOY_DIR}"
                """
            }
        }

        stage('构建镜像') {
            when {
                expression { return !params.SKIP_BUILD }
            }
            steps {
                sh """
                    cd ${DEPLOY_DIR}
                    echo "=== 构建后端镜像 ==="
                    docker build -t ${IMAGE_PREFIX}-backend:latest ./backend

                    echo "=== 构建前端镜像 ==="
                    docker build -t ${IMAGE_PREFIX}-frontend:latest ./frontend
                """
            }
        }

        stage('停止旧服务') {
            steps {
                sh """
                    cd ${DEPLOY_DIR}
                    docker compose -f docker-compose.prod.yml down --remove-orphans || true
                """
            }
        }

        stage('启动新服务') {
            steps {
                sh """
                    cd ${DEPLOY_DIR}
                    # 使用生产环境配置
                    export \$(cat .env.prod | grep -v '^#' | xargs)

                    docker compose -f docker-compose.prod.yml up -d

                    echo "=== 等待服务启动 ==="
                    sleep 15
                """
            }
        }

        stage('健康检查') {
            steps {
                sh """
                    echo "=== 检查服务状态 ==="
                    cd ${DEPLOY_DIR}

                    # 等待后端就绪（最多 60 秒）
                    for i in \$(seq 1 12); do
                        if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1 || \\
                           curl -sf http://localhost:8080/api/v1/auth/login > /dev/null 2>&1; then
                            echo "后端服务已就绪"
                            break
                        fi
                        echo "等待后端启动... (\$i/12)"
                        sleep 5
                    done

                    # 检查所有容器运行状态
                    echo "=== 容器运行状态 ==="
                    docker compose -f docker-compose.prod.yml ps

                    # 检查关键容器
                    for svc in tutor-postgres tutor-redis tutor-backend tutor-frontend; do
                        if docker ps --format '{{.Names}}' | grep -q "^$svc\$"; then
                            echo "✓ \$svc 运行正常"
                        else
                            echo "✗ \$svc 未运行！"
                            docker logs \$svc --tail 20
                            exit 1
                        fi
                    done

                    # 检查前端可访问
                    if curl -sf http://localhost:80 > /dev/null 2>&1; then
                        echo "✓ 前端页面可访问"
                    else
                        echo "⚠ 前端页面暂时不可访问，可能还在启动中"
                    fi

                    echo "=== 部署完成 ==="
                """
            }
        }

        stage('清理旧镜像') {
            when {
                expression { return params.CLEANUP }
            }
            steps {
                sh """
                    docker image prune -f
                    echo "已清理悬空镜像"
                """
            }
        }
    }

    post {
        success {
            echo '✅ 部署成功！'
            // 可选：发送通知
            // sh "curl -X POST https://your-webhook-url -d 'TutorAssist 部署成功'"
        }
        failure {
            echo '❌ 部署失败！'
            sh """
                cd ${DEPLOY_DIR}
                docker compose -f docker-compose.prod.yml ps
                echo "=== 后端日志 ==="
                docker logs tutor-backend --tail 30 2>&1 || true
            """
        }
        always {
            // 清理构建缓存
            sh 'docker builder prune -f || true'
        }
    }
}
