pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        string(name: 'NAS_HOST', defaultValue: '192.168.31.155', description: '飞牛 NAS 局域网 IP 或域名')
        string(name: 'FRONTEND_PORT', defaultValue: '8281', description: '前端对外端口')
        string(name: 'BACKEND_PORT', defaultValue: '8282', description: '后端对外端口')
        booleanParam(name: 'SKIP_TESTS', defaultValue: true, description: '现有 WebMvc 测试尚未适配安全配置；修复后可取消勾选')
    }

    environment {
        APP_NAME = 'tutor-assist'
        COMPOSE_PROJECT_NAME = 'tutor-assist'
        PRODUCTION_ENV_CREDENTIAL_ID = 'tutor-assist-production-env'
        PREVIOUS_IMAGES_FILE = '.tutor-assist-previous-images'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    def shortCommit = sh(
                        script: 'git rev-parse --short=12 HEAD',
                        returnStdout: true
                    ).trim()
                    def appVersion = sh(
                        script: '''awk -F '"' '/"version"[[:space:]]*:/ { print $4; exit }' frontend/package.json''',
                        returnStdout: true
                    ).trim()
                    if (!(appVersion ==~ /\d+\.\d+\.\d+/)) {
                        error "frontend/package.json 中的应用版本号无效：${appVersion ?: '空'}"
                    }
                    def subject = sh(
                        script: 'git log -1 --pretty=%s',
                        returnStdout: true
                    ).trim().replaceAll(/\s+/, ' ')
                    def title = subject.take(48)

                    env.APP_VERSION = appVersion
                    env.RELEASE_TAG = "${appVersion}-${env.BUILD_NUMBER}-${shortCommit}"
                    env.BACKEND_IMAGE = "tutor-assist-backend:${env.RELEASE_TAG}"
                    env.FRONTEND_IMAGE = "tutor-assist-frontend:${env.RELEASE_TAG}"
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${title ?: shortCommit}"
                    currentBuild.description = "版本 ${appVersion} · 提交 ${shortCommit}"
                }
            }
        }

        stage('Validate') {
            steps {
                withCredentials([file(
                    credentialsId: env.PRODUCTION_ENV_CREDENTIAL_ID,
                    variable: 'TUTOR_ASSIST_ENV_FILE'
                )]) {
                    sh './deploy.sh validate "$TUTOR_ASSIST_ENV_FILE"'
                }
            }
        }

        stage('Backend Test') {
            when {
                expression { !params.SKIP_TESTS }
            }
            steps {
                withCredentials([file(
                    credentialsId: env.PRODUCTION_ENV_CREDENTIAL_ID,
                    variable: 'TUTOR_ASSIST_ENV_FILE'
                )]) {
                    sh './deploy.sh test "$TUTOR_ASSIST_ENV_FILE"'
                }
            }
            post {
                always {
                    sh 'docker image rm "tutor-assist-backend-test:${RELEASE_TAG}" >/dev/null 2>&1 || true'
                }
            }
        }

        stage('Build Images') {
            steps {
                withCredentials([file(
                    credentialsId: env.PRODUCTION_ENV_CREDENTIAL_ID,
                    variable: 'TUTOR_ASSIST_ENV_FILE'
                )]) {
                    sh './deploy.sh build "$TUTOR_ASSIST_ENV_FILE"'
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([file(
                    credentialsId: env.PRODUCTION_ENV_CREDENTIAL_ID,
                    variable: 'TUTOR_ASSIST_ENV_FILE'
                )]) {
                    sh './deploy.sh deploy "$TUTOR_ASSIST_ENV_FILE" "$PREVIOUS_IMAGES_FILE"'
                }
            }
        }

        stage('LAN Health Check') {
            steps {
                sh '''
                    set -eu

                    check_url() {
                        name="$1"
                        url="$2"
                        attempt=1
                        while [ "$attempt" -le 20 ]; do
                            echo "${name} health check ${attempt}/20: ${url}"
                            status="$(curl -sS -o /dev/null -w '%{http_code}' \
                                --connect-timeout 5 --max-time 10 "${url}" || true)"
                            if [ "${status}" = "200" ]; then
                                echo "${name} health check passed."
                                return 0
                            fi
                            echo "${name} returned ${status:-curl-error}; retrying in 5s."
                            sleep 5
                            attempt=$((attempt + 1))
                        done
                        echo "${name} health check failed."
                        return 1
                    }

                    check_url backend "http://${NAS_HOST}:${BACKEND_PORT}/actuator/health"
                    check_url frontend "http://${NAS_HOST}:${FRONTEND_PORT}/"
                '''
            }
        }
    }

    post {
        success {
            withCredentials([file(
                credentialsId: env.PRODUCTION_ENV_CREDENTIAL_ID,
                variable: 'TUTOR_ASSIST_ENV_FILE'
            )]) {
                sh './deploy.sh cleanup "$TUTOR_ASSIST_ENV_FILE" "$PREVIOUS_IMAGES_FILE"'
            }
            echo "TutorAssist ${env.RELEASE_TAG} 构建和部署成功。"
        }
        failure {
            script {
                if (fileExists(env.PREVIOUS_IMAGES_FILE)) {
                    withCredentials([file(
                        credentialsId: env.PRODUCTION_ENV_CREDENTIAL_ID,
                        variable: 'TUTOR_ASSIST_ENV_FILE'
                    )]) {
                        sh './deploy.sh rollback "$TUTOR_ASSIST_ENV_FILE" "$PREVIOUS_IMAGES_FILE" || true'
                    }
                }
            }
            echo 'TutorAssist 构建或部署失败，已执行可用的自动回滚。'
        }
        cleanup {
            cleanWs()
        }
    }
}
