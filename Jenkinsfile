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

       stage('Code Quality') {
           steps {
               echo 'Vérification Quality Gate...'
               timeout(time: 10, unit: 'MINUTES') {
                   script {
                       def qg = waitForQualityGate()
                       echo "Quality Gate status: ${qg.status}"
                       if (qg.status != 'OK') {
                           error "Quality Gate failed..."
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
        stage('Deploy') {
            steps {
                echo 'Déploiement du JAR...'
                bat 'gradlew.bat publish'
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
            script {
                sendSlackNotification("✅ Pipeline réussi: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est déployé !")
            }
        }
        failure {
            emailext(
                subject: "Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Le pipeline a échoué. Vérifiez Jenkins pour plus de détails.",
                to: "lo_benhebbadj@esi.dz"
            )
            script {
                sendSlackNotification("❌ Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
            }
        }
    }
}

def sendSlackNotification(String message) {
    withCredentials([string(credentialsId: 'slack-webhook', variable: 'SLACK_URL')]) {
        powershell """
            \$webhook = '${SLACK_URL}'
            \$json = @{text='${message}'} | ConvertTo-Json
            Invoke-RestMethod -Uri \$webhook -Method Post -Body \$json -ContentType 'application/json; charset=utf-8'
        """
    }
}