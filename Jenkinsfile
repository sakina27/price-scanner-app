pipeline {
    agent any

    environment {
        DOCKER_BUILDKIT = 1
    }

    stages {
        stage('Clone Repository') {
            steps {
                git credentialsId: 'Github', url: 'https://github.com/sakina27/price-scanner-app.git', branch: 'main'
            }
        }

        stage('Build Scraper Docker Image') {
            steps {
                dir('scrapper') {
                    bat 'docker build -t price-scanner-scraper .'
                }
            }
        }

        stage('Build Android Docker Image') {
            steps {
                bat 'docker build -t price-scanner-android ./MyApplication'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    bat '''
                        echo %PASS% | docker login -u %USER% --password-stdin
                        docker tag price-scanner-scraper %USER%/price-scanner-scraper:latest
                        docker tag price-scanner-android %USER%/price-scanner-android:latest
                        docker push %USER%/price-scanner-scraper:latest
                        docker push %USER%/price-scanner-android:latest
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: 'k8s-config']) {
                    bat 'kubectl apply -f k8s\\'
                }
            }
        }

        stage('Fetch APK from Kubernetes') {
            steps {
                script {
                    withKubeConfig([credentialsId: 'k8s-config']) {
                        def podName = bat(
                            script: 'kubectl get pods -l app=price-scanner-android -o=jsonpath="{.items[0].metadata.name}"',
                            returnStdout: true
                        ).trim().replace('"', '')

                        bat "kubectl wait --for=condition=ready pod/${podName} --timeout=180s"
                        bat "kubectl exec ${podName} -- sh -c \"./gradlew assembleDebug\""
                        bat "kubectl cp ${podName}:/workspace/app/build/outputs/apk/debug/app-debug.apk app-debug.apk"
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'app-debug.apk', fingerprint: true
        }
    }
}
