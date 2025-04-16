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
                dir('scraper') {
                    sh 'docker build -t price-scanner-scraper .'
                }
            }
        }

        stage('Build Android Docker Image & APK') {
            steps {
                dir('android-app') {
                    sh '''
                        docker build -t price-scanner-android .
                        docker run --rm -v $PWD:/output price-scanner-android cp /workspace/app/build/outputs/apk/debug/app-debug.apk /output/
                    '''
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh '''
                        echo "$PASS" | docker login -u "$USER" --password-stdin
                        docker tag price-scanner-scraper $USER/price-scanner-scraper:latest
                        docker tag price-scanner-android $USER/price-scanner-android:latest
                        docker push $USER/price-scanner-scraper:latest
                        docker push $USER/price-scanner-android:latest
                    '''
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: 'k8s-config']) {
                    sh '''
                        kubectl apply -f k8s/
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'android-app/app/build/outputs/apk/debug/app-debug.apk', fingerprint: true
        }
    }
}
