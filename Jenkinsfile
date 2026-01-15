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

                withSonarQubeEnv('SonarQube') {
                    bat 'gradlew.bat sonarqube'
                }
            }
        }

        /*stage('Code Quality') {
            steps {
               echo 'Vérification Quality Gate...'
                   timeout(time: 1, unit: 'HOURS') {
                       waitForQualityGate abortPipeline: true
                   }
               }
            }*/


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

        stage('Deploy') {
            steps {
                echo 'Déploiement du JAR...'
                bat 'gradlew.bat publish'
            }
        }

       stage('Slack Notification') {
           steps {
               withCredentials([string(credentialsId: 'slack-webhook', variable: 'SLACK_URL')]) {
                   script {  // <--- IMPORTANT
                       echo "Envoi de la notification Slack"

                       def message = "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est deployé !"

                       powershell """
                           \$webhook = '${SLACK_URL}'
                           \$json = '{\"text\": \"${message}\"}'
                           Invoke-RestMethod -Uri \$webhook -Method Post -Body \$json -ContentType 'application/json; charset=utf-8'
                       """
                   }
               }
           }
       }

    }

    post {
        success {
            emailext(
                subject: "Pipeline réussi: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Le déploiement a été effectué avec succès.",
                to: "lo_benhebbadj@esi.dz"
            )
            slackSend(channel: '#test-canal', message: "✅ Pipeline réussi: ${env.JOB_NAME} #${env.BUILD_NUMBER},tokenCredentialId: 'slack-webhook'")
        }
        failure {
            emailext(
                subject: "Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Le pipeline a échoué. Vérifiez Jenkins pour plus de détails.",
                to: "lo_benhebbadj@esi.dz"
            )
            slackSend(channel: '#test-canal', message: "❌ Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}, tokenCredentialId: 'slack-webhook' ")
        }
    }
}
