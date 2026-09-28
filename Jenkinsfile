pipeline {

    agent any

    environment {
        COMPOSE_PROJECT_NAME = 'gestion-projets'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {

        stage('1. Checkout') {
            steps {
                echo '--- Recuperation du code source ---'
                checkout scm
                sh 'ls -la'
            }
        }

        stage('2. Verifier les outils') {
            steps {
                sh '''
                    docker --version
                    docker compose version
                '''
            }
        }

        stage('3. Build Backend') {
            steps {
                sh 'docker build -t gestion-backend:${BUILD_NUMBER} -t gestion-backend:latest ./backend'
            }
        }

        stage('4. Build Frontend') {
            steps {
                sh 'docker build -t gestion-frontend:${BUILD_NUMBER} -t gestion-frontend:latest ./frontend'
            }
        }

        stage('5. Deploiement de la stack') {
            steps {
                sh '''
                    docker compose down --remove-orphans || true
                    docker compose up -d
                '''
            }
        }

        stage('6. Tests de sante') {
            steps {
                sh '''
                    for i in $(seq 1 30); do
                        if curl -fs http://localhost:8089/entreprise/all > /dev/null; then
                            echo "Backend operationnel"
                            break
                        fi
                        echo "tentative $i/30..."
                        sleep 5
                    done

                    curl -fs http://localhost:8089/entreprise/all
                    echo ""
                    curl -fsI http://localhost:4200 | head -1
                '''
            }
        }
    }

    post {
        success {
            sh 'docker compose ps'
            echo "Frontend : http://192.168.33.10:4200"
            echo "Backend  : http://192.168.33.10:8089"
        }

        failure {
            sh 'docker compose logs --tail=50 || true'
        }

        always {
            sh 'docker image prune -f || true'
        }
    }
}
