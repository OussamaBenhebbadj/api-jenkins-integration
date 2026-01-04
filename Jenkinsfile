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
                    // Archivage des résultats des tests
                    junit '**/build/test-results/test/*.xml'

                    // Génération des rapports Cucumber (si configuré)
                    // cucumber buildStatus: 'UNSTABLE',
                    //         fileIncludePattern: '**/*.json',
                    //         jsonReportDirectory: 'build/reports/cucumber'
                }
            }
        }

        // Décommentez quand SonarQube sera configuré
        /*
        stage('Code Analysis') {
            steps {
                echo 'Analyse du code avec SonarQube...'
                script {
                    try {
                        withSonarQubeEnv('SonarQube') {
                            bat 'gradlew.bat sonarqube'
                        }
                    } catch (Exception e) {
                        echo "Erreur SonarQube: ${e.message}"
                    }
                }
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Vérification du Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        */

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

        // Décommentez quand Maven repo sera configuré
        /*
        stage('Deploy') {
            steps {
                echo 'Déploiement vers Maven Repository...'
                script {
                    try {
                        bat 'gradlew.bat publish'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error("Le déploiement a échoué: ${e.message}")
                    }
                }
            }
        }
        */

        stage('Notification') {
            steps {
                echo 'Envoi des notifications...'
                script {
                    if (currentBuild.result == 'SUCCESS' || currentBuild.result == null) {
                        emailext (
                            subject: "✅ Build réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                            body: """
                                Le build a été effectué avec succès !

                                Projet: ${env.JOB_NAME}
                                Build: #${env.BUILD_NUMBER}
                                URL: ${env.BUILD_URL}
                            """,
                            to: 'votre-email@example.com'
                        )

                        // Décommentez quand Slack sera configuré
                        /*
                        slackSend (
                            color: 'good',
                            message: "✅ Build réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                        )
                        */
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'Le pipeline a échoué !'
            emailext (
                subject: "❌ Échec - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le pipeline a échoué !

                    Projet: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    URL: ${env.BUILD_URL}
                """,
                to: 'votre-email@example.com',
                attachLog: true
            )

            // Décommentez quand Slack sera configuré
            /*
            slackSend (
                color: 'danger',
                message: "❌ Build échoué - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
            )
            */
        }
        always {
            echo "Pipeline terminé: ${currentBuild.result ?: 'SUCCESS'}"
        }
    }
}