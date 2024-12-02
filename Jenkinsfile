pipeline {
    agent any
    tools {
        maven 'Maven 3.9.9'
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
                script {
                    sh 'sudo apt-get update'
                    sh 'sudo apt-get install -y openjdk-17-jdk'
                }
            }
        }
        stage('Clonar código fuente') {
            steps {
                checkout scm
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
                    sh 'docker build -t myapp .'
                    sh "docker stop myapp || true"
                    sh "docker rm -f myapp || true"
                    sh 'docker run -d -p 8081:8081 --name myapp myapp'
                }
            }
        }
    }

    post {
        always {
            script {
                def gitCommit = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                def gitAuthorName = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
                def gitCommitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                def gitAuthorEmail = sh(script: 'git log -1 --pretty=%ae', returnStdout: true).trim()
                def GITHUB_REPO = 'https://github.com/maita07/springboot_tests'
                def GITHUB_OWNER = 'maita07'

                def subject = "Jenkins Build #${BUILD_NUMBER} - ${currentBuild.currentResult}"
                def body = """
                <p>El build ha terminado con el siguiente resultado: ${currentBuild.currentResult}</p>
                <p>Detalles del commit:</p>
                <ul>
                    <li><strong>Commit:</strong> ${gitCommit}</li>
                    <li><strong>Autor:</strong> ${gitAuthorName}</li>
                    <li><strong>Mensaje del commit:</strong> ${gitCommitMessage}</li>
                </ul>
                """

                // Extraer detalles de pruebas fallidas
                def failedTests = sh(
                    script: 'head -n 4 target/surefire-reports/*.txt',
                    returnStdout: true
                ).trim().split('\n')

                def date = new Date().format('yyyy-MM-dd-HH-mm-ss')
                def logDirectory = "/home/labqa/logs-pipeline-mobile/test-log-${date}"
                sh "sudo mkdir -p ${logDirectory}"
                sh "sudo cp target/surefire-reports/*.txt ${logDirectory}"

                // Limitar a 50 errores
                def limitedErrors = []
                def errorCount = 0

                failedTests.each { error ->
                    if (errorCount < 50) {
                        limitedErrors.add(error)
                        errorCount++
                    }
                }

                // Si hay errores, agregarlos al cuerpo del correo
                if (limitedErrors) {
                    body += """
                    <h3>Información de los siguientes Sets de Tests:</h3>
                    <pre>${limitedErrors.join('\n')}</pre>
                    <p>Por favor, revisa los reportes completos de las pruebas en: ${logDirectory}</p>
                    """
                } else {
                    body += "<p>¡Todos los tests han pasado exitosamente!</p>"
                }

                emailext(
                    subject: subject,
                    body: body,
                    to: 'nicolas.batistelli@eldars.com.ar',
                    mimeType: 'text/html',
                    attachmentsPattern: 'target/surefire-reports/*.txt'
                )

                // Guardar los resultados en una carpeta específica en GitHub
                withCredentials([usernamePassword(credentialsId: 'github-token', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_TOKEN')]) {
                    sh """
                    # Configuración de usuario de Git
                    git config user.name "\${gitAuthorName}"
                    git config user.email "\${gitAuthorEmail}"

                    # Verificar si estamos en un repositorio limpio antes de hacer el checkout
                    git status

                    # Intentar hacer checkout a la rama test-reports-\${BUILD_NUMBER}, si falla, crearla
                    git fetch origin || exit 1
                    git checkout test-reports-\${BUILD_NUMBER} || git checkout -b test-reports-\${BUILD_NUMBER}

                    # Asegurarse de que no haya errores en la creación de la nueva rama
                    if [ $? -ne 0 ]; then
                        echo "Error al hacer checkout de la rama test-reports-${BUILD_NUMBER}" && exit 1
                    fi

                    mkdir -p test-reports/${date}
                    cp target/surefire-reports/*.txt test-reports/${date}/
                    git add test-reports/${date}/*
                    git commit -m "Agregado reporte de pruebas del build ${BUILD_NUMBER}"

                    # Establecer la URL remota y hacer push a la nueva rama
                    git remote set-url origin https://github.com/${GITHUB_USER}/${GITHUB_REPO}.git
                    git push origin test-reports-\${BUILD_NUMBER}
                    """
                }

                // Comentario sobre los errores en el commit
                if (limitedErrors) {
                    def errorMessage = "Se han encontrado errores en los tests. Ver detalles en el commit."
                    sh """
                    curl -u ${GITHUB_USER}:${GITHUB_TOKEN} \
                    -X POST \
                    -d '{
                        "body": "${errorMessage}",
                        "commit_id": "${gitCommit}"
                    }' \
                    https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/commits/${gitCommit}/comments
                    """
                }
            }
        }
    }
}
