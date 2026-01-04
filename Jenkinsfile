pipeline {
    agent any

    environment {
        MAVEN_USERNAME = credentials('maven-username')
        MAVEN_PASSWORD = credentials('maven-password')
    }

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
                        sh './gradlew clean test'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error("Les tests ont échoué")
                    }
                }
            }
            post {
                always {
                    // Archivage des résultats des tests
                    junit '**/build/test-results/test/*.xml'

                    // Génération des rapports Cucumber
                    cucumber buildStatus: 'UNSTABLE',
                            fileIncludePattern: '**/*.json',
                            jsonReportDirectory: 'build/reports/cucumber'
                }
            }
        }

        stage('Code Analysis') {
            steps {
                echo 'Analyse du code avec SonarQube...'
                script {
                    try {
                        sh './gradlew sonarqube'
                    } catch (Exception e) {
                        echo "Erreur lors de l'analyse SonarQube: ${e.message}"
                    }
                }
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Vérification du Quality Gate...'
                script {
                    timeout(time: 5, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline interrompu : Quality Gate en échec (${qg.status})"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Construction du projet...'

                // Génération du fichier JAR
                sh './gradlew build -x test'

                // Génération de la documentation
                sh './gradlew javadoc'
            }
            post {
                success {
                    // Archivage du JAR et de la documentation
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                    archiveArtifacts artifacts: '**/build/docs/javadoc/**', fingerprint: true
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Déploiement vers Maven Repository...'
                script {
                    try {
                        sh './gradlew publish'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error("Le déploiement a échoué: ${e.message}")
                    }
                }
            }
        }

        stage('Notification') {
            steps {
                echo 'Envoi des notifications...'
                script {
                    if (currentBuild.result == 'SUCCESS' || currentBuild.result == null) {
                        // Notification par email
                        emailext (
                            subject: "✅ Déploiement réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                            body: """
                                Le déploiement a été effectué avec succès !

                                Projet: ${env.JOB_NAME}
                                Build: #${env.BUILD_NUMBER}
                                URL: ${env.BUILD_URL}
                            """,
                            to: 'votre-email@example.com'
                        )

                        // Notification Slack
                        slackSend (
                            color: 'good',
                            message: "✅ Déploiement réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Voir>)"
                        )
                    }
                }
            }
        }
    }

    post {
        failure {
            echo 'Le pipeline a échoué, envoi des notifications...'
            emailext (
                subject: "❌ Échec du pipeline - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le pipeline a échoué à l'étape: ${env.STAGE_NAME}

                    Projet: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    URL: ${env.BUILD_URL}
                """,
                to: 'votre-email@example.com'
            )

            slackSend (
                color: 'danger',
                message: "❌ Échec du pipeline - ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Voir>)"
            )
        }
    }
}