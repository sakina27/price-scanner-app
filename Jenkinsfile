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

        stage('Build Android Docker Image & APK') {
            steps {
                dir('MyApplication') {
                    bat '''
                        docker build -t price-scanner-android .
                        docker run --rm -v "%cd%:/output" price-scanner-android sh -c "cp /workspace/app/build/outputs/apk/debug/app-debug.apk /output/"
                    '''
                }
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
    }

    post {
        always {
            archiveArtifacts artifacts: 'android-app\\app\\build\\outputs\\apk\\debug\\app-debug.apk', fingerprint: true
        }
    }
}
