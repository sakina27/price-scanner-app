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

        stage('Build APK') {
            steps {
                dir('MyApplication') {
                    bat '.\\gradlew assembleDebug'
                }
            }
        }

        stage('Build Scraper Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    bat """
                        docker login -u $USER -p $PASS
                        docker info
                    """
                }
                dir('scrapper') {
                    bat 'docker build -t price-scanner-scraper .'
                }
            }
        }

        stage('Build Android Docker Image') {
            steps {
                dir('MyApplication') {
                    bat 'docker build -t price-scanner-android .'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    bat """
                        echo Logging into Docker Hub...
                        docker logout
                        docker login -u %USER% -p %PASS%

                        docker tag price-scanner-scraper %USER%/price-scanner-scraper:latest
                        docker tag price-scanner-android %USER%/price-scanner-android:latest

                        docker push %USER%/price-scanner-scraper:latest
                        docker push %USER%/price-scanner-android:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: 'k8s-config']) {
                    bat '''
                        kubectl config view
                        kubectl cluster-info
                        kubectl get nodes
                        kubectl apply -f k8s\\ --validate=false
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'MyApplication\\app\\build\\outputs\\apk\\debug\\app-debug.apk', fingerprint: true
        }
    }
}
