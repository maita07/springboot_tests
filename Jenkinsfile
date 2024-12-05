pipeline {
    agent any

    tools {
        maven 'Maven 3.9.9'
    }

    environment {
        PROJECT_NAME = 'mi-aplicacion' // Nombre del proyecto
        DOCKER_PORT = '8081'
    }

    stages {
        stage("Build Info") {
            steps {
                script {
                    BUILD_TRIGGER_BY = currentBuild.getBuildCauses()[0].userId
                    currentBuild.displayName = "#${env.BUILD_NUMBER}"
                }
            }
        }

        stage('Instalar Java 17') {
            steps {
                sh '''
                    sudo apt-get update
                    sudo apt-get install -y openjdk-17-jdk
                '''
            }
        }

        stage('Clonar código fuente') {
            steps {
                checkout scm
            }
        }

        stage('Obtener versión del proyecto') {
            steps {
                script {
                    // Obtener la versión del proyecto de Maven
                    def projectVersion = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
                    echo "Versión del proyecto: ${projectVersion}"

                    // Usar la versión del proyecto para etiquetar la imagen Docker u otros usos
                    env.PROJECT_VERSION = projectVersion
                }
            }
        }

        stage('Instalar dependencias') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('Ejecutar pruebas unitarias') {
            steps {
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Build & Run Docker') {
            steps {
                script {
                    def dockerTag = "${env.PROJECT_NAME}:${env.PROJECT_VERSION}"
                    echo "Construyendo la imagen Docker con el tag: ${dockerTag}"
                    sh "docker build --build-arg PROJECT_VERSION=${env.PROJECT_VERSION} -t ${dockerTag} ."

                    // Detener y eliminar contenedor si existe
                    sh """
                        docker ps -q -f name=${env.PROJECT_NAME} | xargs -r docker stop || true
                        docker rm -f ${env.PROJECT_NAME} || true
                    """

                    // Correr el contenedor en segundo plano
                    sh "docker run -d -p ${DOCKER_PORT}:${DOCKER_PORT} --name ${env.PROJECT_NAME} ${dockerTag}"
                }
            }
        }
    }

    post {
        always {
            script {
                // Obtener información del commit
                def commitInfo = getCommitInfo()
                
                // Crear el mensaje de correo
                def body = createEmailBody(commitInfo)
                
                // Subir reportes de pruebas a GitHub
                def repoLink = uploadTestReports(commitInfo)
                
                // Si hay errores de pruebas, agregar link a los reportes en GitHub
                if (repoLink) {
                    body += """
                        <h3>Información de los siguientes Sets de Tests:</h3>
                        <p>Por favor, revisa los reportes completos de las pruebas en el siguiente enlace: <a href='${repoLink}'>Ver reportes</a></p>
                    """
                }

                // Enviar correo
                sendEmail(body)
            }
        }
    }
}

def getCommitInfo() {
    return [
        commit: sh(script: 'git rev-parse HEAD', returnStdout: true).trim(),
        authorName: sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim(),
        commitMessage: sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim(),
        authorEmail: sh(script: 'git log -1 --pretty=%ae', returnStdout: true).trim()
    ]
}

def createEmailBody(commitInfo) {
    return """
        <p>El build ha terminado con el siguiente resultado: ${currentBuild.currentResult}</p>
        <p>Detalles del commit:</p>
        <ul>
            <li><strong>Commit:</strong> ${commitInfo.commit}</li>
            <li><strong>Autor:</strong> ${commitInfo.authorName}</li>
            <li><strong>Mensaje del commit:</strong> ${commitInfo.commitMessage}</li>
        </ul>
    """
}

def uploadTestReports(commitInfo) {
    def date = new Date().format('yyyy-MM-dd-HH-mm-ss')
    def logDir = "/home/labqa/logs-pipeline-mobile/test-log-${date}"
    
    // Crear directorio para los logs de pruebas
    sh "sudo mkdir -p ${logDir}"
    sh "sudo cp target/surefire-reports/*.txt ${logDir}"

    // Subir los reportes a GitHub
    sh "sudo rm -rf /tmp/test-reports" // Eliminar directorio temporal si existe
    sh "git clone https://github.com/maita07/tests_resultados /tmp/test-reports"
    sh "sudo cp -r ${logDir} /tmp/test-reports/"

    // Verificar los archivos que se subieron
    sh "sudo ls -l /tmp/test-reports/"

    // Realizar commit y push de los archivos
    withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_PAT')]) {
        dir('/tmp/test-reports') {
            sh "git config user.name '${commitInfo.authorName}'"
            sh "git config user.email '${commitInfo.authorEmail}'"
            sh "git remote set-url origin https://${GITHUB_USER}:${GITHUB_PAT}@github.com/maita07/tests_resultados.git"
            sh 'git add .'
            sh 'git commit -m "Agregando reportes de prueba"'
            sh 'git push origin main'
        }
    }

    // Si hay reportes, devolvemos el enlace a los reportes en GitHub
    return "https://github.com/maita07/tests_resultados/tree/main/test-log-${date}"
}

def sendEmail(body) {
    emailext(
        subject: "Jenkins Build #${BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: body,
        to: currentBuild.getBuildCauses()[0].userId, // Autor del commit
        mimeType: 'text/html',
        attachmentsPattern: 'target/surefire-reports/*.txt'
    )
}
