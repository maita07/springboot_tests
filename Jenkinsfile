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
                    // Actualizar los repositorios de paquetes
                    sh 'sudo apt-get update'
                    // Instalar java 17
                    sh 'sudo apt-get install -y openjdk-17-jdk'
                    // def gitAuthorName = sh(script: 'git log -1 --format=%an', returnStdout: true).trim()
                    echo "El autor del commit es: ${BUILD_TRIGGER_BY}"
                }
            }
        }
        stage('Clonar código fuente') {
            steps {
                git branch: 'main', url: 'https://github.com/maita07/springboot_tests.git'
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
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Build & Run Docker') {
            steps {
                script {
                    sh 'docker build -t myapp .'
                    // Detener y eliminar el contenedor existente si está en ejecución
                    sh "docker stop myapp || true"
                    sh "docker rm -f myapp || true"
                    sh 'docker run -d -p 8081:8081 --name myapp myapp'

                    echo 'Desplegar la aplicación en un servidor o Docker'
                }
            }
        }
    }
    post {
        failure {
            script{
                // Definir el asunto y cuerpo del correo con base en el resultado del build
                def subject = "Jenkins Build #${BUILD_NUMBER} - ${currentBuild.currentResult}"
                def body = """
                    <p>El build ha terminado con el siguiente resultado: ${currentBuild.currentResult}</p>
                    <p>Detalles del commit:</p>
                    <ul>
                        <li><strong>Commit:</strong> ${GIT_COMMIT}</li>
                        <li><strong>Autor:</strong> ${GIT_AUTHOR_NAME}</li>
                        <li><strong>Mensaje del commit:</strong> ${GIT_COMMIT_MESSAGE}</li>
                    </ul>
                    <p>Ver detalles en Jenkins: ${BUILD_URL}</p>
                """
                
                // Si las pruebas fallaron, incluir los detalles de las pruebas fallidas en el cuerpo del correo
                if (currentBuild.currentResult == 'FAILURE') {
                    // Leer los resultados de las pruebas fallidas
                    def failedTests = readFile('target/surefire-reports/*.txt')
                    def failedTestNames = failedTests.readLines().findAll { it.contains('FAIL') }.join('\n')

                    // Modificar el cuerpo del correo para agregar los detalles de las pruebas fallidas
                    body += """
                        <h3>Las siguientes pruebas fallaron en el paquete ${PACKAGE_NAME} versión ${VERSION}:</h3>
                        <pre>${failedTestNames}</pre>
                        <p>Por favor, revisa los reportes de las pruebas en:</p>
                        <a href="${env.GIT_URL}/-/jobs/${BUILD_NUMBER}/test_report">Ver reporte de pruebas</a>
                    """
                }
            }
            

            // Enviar el correo
            emailext(
                subject: subject,
                body: body,
                to: 'jose.maita@gmail.com',
                mimeType: 'text/html'
            )
        }

    }
}