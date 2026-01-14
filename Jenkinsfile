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

                withSonarQubeEnv('SonarQubeServer') {
                    bat 'gradlew.bat sonarqube'
                }
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Phase 2.3: Vérification Quality Gate...'
                timeout(time: 1, unit: 'HOURS') {
                    script {
                        def qg = waitForQualityGate(abortPipeline: true)
                        echo "Quality Gate status: ${qg.status}"
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

        stage('Slack Notification') {
                    steps {
                        script {
                            echo "Envoi de la notification Slack"

                            def slackUrl = env.'slack-token'

                            if (slackUrl) {
                                slackUrl = slackUrl.trim()

                                def message = "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est deploye !"

                                // Création du fichier JSON
                                def jsonContent = """{"text": "${message}"}"""
                                writeFile file: 'slack-payload.json', text: jsonContent, encoding: 'UTF-8'

                                // Utilisation de PowerShell pour l'envoi (plus fiable que cmd)
                                powershell """
                                    \$webhook = '${slackUrl}'
                                    \$json = Get-Content -Path 'slack-payload.json' -Raw
                                    Invoke-RestMethod -Uri \$webhook -Method Post -Body \$json -ContentType 'application/json; charset=utf-8'
                                """

                                // Nettoyage
                                bat 'if exist slack-payload.json del slack-payload.json'
                            } else {
                                echo "ATTENTION : La variable 'slack-token' n'est pas definie."
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
            slackSend(channel: '#dev', message: "✅ Pipeline réussi: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
        failure {
            emailext(
                subject: "Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Le pipeline a échoué. Vérifiez Jenkins pour plus de détails.",
                to: "lo_benhebbadj@esi.dz"
            )
            slackSend(channel: '#dev', message: "❌ Pipeline échoué: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
        }
    }
}
