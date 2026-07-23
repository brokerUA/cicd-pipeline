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

        stage('Lint Dockerfile') {
            steps {
                echo 'Linting Dockerfile with Hadolint...'
                script {
                    def dockerHome = tool name: 'docker-in-jenkins', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'

                    withEnv(["PATH+DOCKER=${dockerHome}"]) {
                        sh "docker run --rm -i hadolint/hadolint < Dockerfile || true"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            agent {
                docker {
                    image 'docker:29'
                    args '-v /var/run/docker.sock:/var/run/docker.sock --user 0:0'
                }
            }
            environment {
                HOME = '.'
            }
            steps {
                echo "Building Docker image for branch ${env.BRANCH_NAME} inside Docker agent..."
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Scan Docker Image') {
            agent {
                docker {
                    image 'aquasec/trivy:latest'
                    args '-v /var/run/docker.sock:/var/run/docker.sock -v $HOME/.cache:/root/.cache'
                }
            }
            steps {
                echo "Scanning Docker image ${IMAGE_NAME} for vulnerabilities using Trivy inside Docker agent..."
                script {
                    sh "trivy image --exit-code 0 --severity HIGH,MEDIUM,LOW --no-progress ${IMAGE_NAME}"
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                echo 'Pushing image to Docker Hub...'
                script {
                    def dockerHome = tool name: 'docker-in-jenkins', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'

                    def dockerUser = "brokerua"
                    def remoteImage = "docker.io/${dockerUser}/${IMAGE_NAME}"

                    withEnv(["PATH+DOCKER=${dockerHome}"]) {
                        withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                            sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                            sh "docker tag ${IMAGE_NAME} ${remoteImage}"
                            sh "docker push ${remoteImage}"
                        }
                    }
                }
            }
        }

        stage('Trigger Deployment') {
            steps {
                script {
                    def targetPipeline = (env.BRANCH_NAME == 'main') ? 'Deploy_to_main' : 'Deploy_to_dev'
                    echo "Triggering downstream pipeline: ${targetPipeline}"

                    build job: targetPipeline, wait: false
                }
            }
        }
    }
}