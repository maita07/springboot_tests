pipeline {
    agent any
    tools {
        maven 'Maven 3.9.9'
    }

    stages {
        stage('Instalar Java 17') {
            steps {
                script {
                    // Actualizar los repositorios de paquetes
                    sh 'sudo apt-get update'
                    // Instalar java 17
                    sh 'sudo apt-get install -y openjdk-17-jdk'
                    echo 'todo piola'
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
                    sh 'docker run -d -p 8081:8081 myapp'

                    echo 'Desplegar la aplicación en un servidor o Docker'
                }
            }
        }
    }
    post {
        failure {
                emailext(
                body: """Error en las Pruebas Unitarias, mire el siguiente archivo xml""",
                subject: 'Fallaron las pruebas...',
                to: 'jose.maita@eldars.com.ar',
                mimeType: 'text/html',
                attachmentsPattern: '**/target/surefire-reports/*.xml'
                )
        }
    }
}