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
    // Acceder a los resultados de las pruebas fallidas sin usar getRawBuild
                def testResultAction = currentBuild.getAction(hudson.tasks.junit.TestResultAction)
                def failedTests = testResultAction?.getFailedTests()
                def failedTestsList = []
                if (failedTests) {
                    failedTests.each { test ->
                        failedTestsList.add("Test failed: ${test.name}")
                    }
                }

                // Definir el cuerpo del correo
                def subject = "Jenkins Build #${BUILD_NUMBER} - ${currentBuild.currentResult}"
                def body = """
                    <p>El build ha terminado con el siguiente resultado: ${currentBuild.currentResult}</p>
                    <p>Detalles del commit:</p>
                    <ul>
                        <li><strong>Commit:</strong> ${gitCommit}</li>
                        <li><strong>Autor:</strong> ${gitAuthorName}</li>
                        <li><strong>Mensaje del commit:</strong> ${gitCommitMessage}</li>
                    </ul>
                    <p>Ver detalles en Jenkins: ${BUILD_URL}</p>
                """

                // Si alguna prueba falló, agregar los detalles al cuerpo del correo
                if (failedTestsList.size() > 0) {
                    body += """
                        <h3>Las siguientes pruebas fallaron:</h3>
                        <ul>
                            ${failedTestsList.collect { "<li>${it}</li>" }.join()}
                        </ul>
                    """
                }


            // Enviar el correo
            emailext(
                subject: subject,
                body: body,
                to: ${gitAuthorEmail},
                mimeType: 'text/html'
            )
        }
    }
}

