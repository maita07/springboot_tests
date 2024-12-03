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
                def logDir = "/home/labqa/logs-pipeline-mobile/test-log-${date}"
                sh "sudo mkdir -p ${logDir}"
                sh "sudo cp target/surefire-reports/*.txt ${logDir}"

                // Limitar a 50 errores
                def limitedErrors = []
                def errorCount = 0
                failedTests.each { error -> 
                    if (errorCount < 50) {
                        limitedErrors.add(error)
                        errorCount++
                    }
                }

                // Subir los reportes al repositorio GitHub/GitLab
                sh "sudo rm -rf /tmp/test-reports"  // Elimina el directorio si ya existe
                sh "git clone https://github.com/maita07/tests_resultados /tmp/test-reports"

                // Copiar los archivos .txt a /tmp/test-reports/
                sh "sudo cp target/surefire-reports/*.txt /tmp/test-reports/"

                // Verificar que los archivos están en /tmp/test-reports/
                sh "sudo ls -l /tmp/test-reports/"

                // Realizar el commit y push de los archivos
                withCredentials([usernamePassword(credentialsId: 'github-pat', usernameVariable: 'GITHUB_USER', passwordVariable: 'GITHUB_PAT')]) {
                    dir('/tmp/test-reports') {
                        // Configurar el usuario de Git
                        sh "git config user.name '${gitAuthorName}'"
                        sh "git config user.email '${gitAuthorEmail}'"
                        // Cambiar la URL remota para usar las credenciales
                        sh "git remote set-url origin https://${GITHUB_USER}:${GITHUB_PAT}@github.com/maita07/tests_resultados.git"
                        sh 'git add .'
                        sh 'git commit -m "Agregando reportes de prueba"'
                        sh 'git push origin main'  // O la rama que corresponda
                    }
                }

                // Si hay errores, agregarlos al cuerpo del correo
                if (limitedErrors) {
                    def repoLink = "https://github.com/maita07/tests_resultados/blob/main/${gitCommit}.txt"
                    body += """
                    <h3>Información de los siguientes Sets de Tests:</h3>
                    <pre>${limitedErrors.join('\n')}</pre>
                    <p>Por favor, revisa los reportes completos de las pruebas en el siguiente enlace: <a href='${repoLink}'>Ver reportes</a></p>
                    """
                } else {
                    body += "<p>¡Todos los tests han pasado exitosamente!</p>"
                }

                // Enviar correo con los detalles y reportes
                emailext(
                    subject: subject,
                    body: body,
                    to: 'nicolas.batistelli@eldars.com.ar',
                    mimeType: 'text/html',
                    attachmentsPattern: 'target/surefire-reports/*.txt'
                )
            }
        }
    }
}
