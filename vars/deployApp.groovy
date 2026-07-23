def call(Map config = [:]) {
    def dockerUser    = config.dockerUser ?: 'brokerua'
    def imageName     = config.imageName
    def containerName = config.containerName
    def portMapping   = config.portMapping

    echo "Starting Shared Library deployment for ${containerName}..."

    def dockerHome = tool name: 'docker-in-jenkins', type: 'org.jenkinsci.plugins.docker.commons.tools.DockerTool'

    withEnv(["PATH+DOCKER=${dockerHome}"]) {
        echo "Pulling image from Docker Hub..."
        sh "docker pull docker.io/${dockerUser}/${imageName}"

        echo "Cleaning up old container..."
        sh "docker rm -f ${containerName} || true"

        echo "Running new container..."
        sh "docker run -d --name ${containerName} -p ${portMapping} docker.io/${dockerUser}/${imageName}"
    }
}