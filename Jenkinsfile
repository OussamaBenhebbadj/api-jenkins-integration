pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Récupération du code source...'
                checkout scm
            }
        }



        stage('Test') {
            steps {
                echo 'Lancement des tests...'
                script {
                    try {
                        bat 'gradlew.bat clean test'
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        echo "Tests échoués: ${e.message}"
                    }
                }
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Code Analysis') {
            steps {
                echo 'Analyse du code avec SonarQube...'
                bat 'gradlew.bat sonarqube'
            }
        }

        stage('Code Quality') {
            steps {
                script {
                    timeout(time: 10, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Échec du Quality Gate: ${qg.status}"
                        }
                    }
                }
            }
        }




        stage('Build') {
            steps {
                echo 'Construction du projet...'
                bat 'gradlew.bat build -x test'

                echo 'Génération de la documentation...'
                bat 'gradlew.bat javadoc'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                    archiveArtifacts artifacts: '**/build/docs/javadoc/**', allowEmptyArchive: true
                }
            }
        }
}