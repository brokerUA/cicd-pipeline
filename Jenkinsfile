pipeline {
    agent any

    tools {
        nodejs 'node-js-26'
        'org.jenkinsci.plugins.docker.commons.tools.DockerTool' 'docker-in-jenkins'
    }

    environment {
        IMAGE_NAME = "${env.BRANCH_NAME == 'main' ? 'nodemain:v1.0' : 'nodedev:v1.0'}"
        CONTAINER_NAME = "${env.BRANCH_NAME == 'main' ? 'node-app-main' : 'node-app-dev'}"
        PORT_MAPPING = "${env.BRANCH_NAME == 'main' ? '3000:3000' : '3001:3000'}"
    }

    stages {
        stage('Install Dependencies') {
            steps {
                echo 'Installing dependencies...'
                sh 'npm install'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'npm test'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image for branch ${env.BRANCH_NAME}..."
                script {
                    def dockerHome = tool name: 'docker-in-jenkins', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'

                    withEnv(["PATH+DOCKER=${dockerHome}"]) {
                        sh "docker build -t ${IMAGE_NAME} ."
                    }
                }
            }
        }

        stage('Deploy (Lowest Downtime)') {
            steps {
                echo 'Deploying application...'
                script {
                    def dockerHome = tool name: 'docker-in-jenkins', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'

                    withEnv(["PATH+DOCKER=${dockerHome}"]) {
                        sh "docker stop ${CONTAINER_NAME} || true"
                        sh "docker rm ${CONTAINER_NAME} || true"
                        sh "docker run -d --name ${CONTAINER_NAME} --expose 3000 -p ${PORT_MAPPING} ${IMAGE_NAME}"
                    }
                }
            }
        }
    }
}