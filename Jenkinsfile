pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                echo 'Récupération du code source...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Construction du projet...'
                script {
                    bat 'gradlew.bat clean build -x test'
                }
            }
            post {
                success {
                    echo 'Build réussi !'
                    archiveArtifacts artifacts: '**/build/libs/*.jar', allowEmptyArchive: true
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Lancement des tests...'
                script {
                    try {
                        bat 'gradlew.bat test'
                    } catch (Exception e) {
                        echo "Tests échoués: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Generate Documentation') {
            steps {
                echo 'Génération de la documentation...'
                script {
                    try {
                        bat 'gradlew.bat javadoc'
                    } catch (Exception e) {
                        echo "Documentation generation failed: ${e.message}"
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/docs/javadoc/**', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline terminé avec succès !'
            emailext (
                subject: "✅ Build réussi - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le build a été effectué avec succès !

                    Projet: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    URL: ${env.BUILD_URL}
                """,
                to: 'votre-email@example.com',
                attachLog: true
            )
        }
        failure {
            echo '❌ Le pipeline a échoué !'
            emailext (
                subject: "❌ Build échoué - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                    Le build a échoué !

                    Projet: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    URL: ${env.BUILD_URL}

                    Consultez les logs pour plus de détails.
                """,
                to: 'votre-email@example.com',
                attachLog: true
            )
        }
        always {
            echo "Build terminé avec le statut: ${currentBuild.result ?: 'SUCCESS'}"
        }
    }
}