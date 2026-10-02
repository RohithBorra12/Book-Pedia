// CI/CD pipeline (use a Jenkins "Multibranch Pipeline" job pointing at your Git repo).
//
//   feature/* branch  -> build + test only
//   develop branch    -> build + test + push images + deploy to DEV automatically
//   main branch       -> build + test + push images + wait for approval + deploy to PROD
//
// Jenkins agent needs: JDK 21, Maven, Docker, kubectl (and AWS CLI if you use ECR/EKS).
// Create a Jenkins credential "docker-registry" (username/password) for your registry.

pipeline {
    agent any

    environment {
        REGISTRY = 'your-dockerhub-username'   // or 123456789012.dkr.ecr.ap-south-1.amazonaws.com
        TAG      = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Unit Test') {
            parallel {
                stage('user-service')  { steps { dir('user-service')  { sh 'mvn -B clean package' } } }
                stage('book-service')  { steps { dir('book-service')  { sh 'mvn -B clean package' } } }
                stage('order-service') { steps { dir('order-service') { sh 'mvn -B clean package' } } }
                stage('frontend')      { steps { dir('frontend')      { sh 'npm ci && npm run build' } } }
            }
            post {
                always { junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml' }
            }
        }

        // Optional: uncomment when you have a SonarQube server configured in Jenkins
        // stage('Code Quality') {
        //     steps { withSonarQubeEnv('sonar') { dir('order-service') { sh 'mvn -B sonar:sonar' } } }
        // }

        stage('Docker Build & Push') {
            when { anyOf { branch 'develop'; branch 'main' } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-registry',
                                                  usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
                    sh 'echo "$REG_PASS" | docker login -u "$REG_USER" --password-stdin'
                    // For AWS ECR instead:
                    // sh 'aws ecr get-login-password | docker login --username AWS --password-stdin $REGISTRY'
                }
                script {
                    ['user-service', 'book-service', 'order-service', 'frontend'].each { svc ->
                        sh "docker build -t ${env.REGISTRY}/bookstore-${svc}:${env.TAG} ${svc}"
                        sh "docker push ${env.REGISTRY}/bookstore-${svc}:${env.TAG}"
                    }
                }
            }
        }

        stage('Deploy to DEV') {
            when { branch 'develop' }
            steps { sh "./k8s/deploy.sh dev ${env.REGISTRY} ${env.TAG}" }
        }

        stage('Approve PROD') {
            when { branch 'main' }
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    input message: "Deploy build ${env.TAG} to PRODUCTION?", ok: 'Deploy'
                }
            }
        }

        stage('Deploy to PROD') {
            when { branch 'main' }
            steps { sh "./k8s/deploy.sh prod ${env.REGISTRY} ${env.TAG}" }
        }
    }

    post {
        success { echo "Pipeline finished for ${env.BRANCH_NAME} build ${env.TAG}" }
        failure { echo "Pipeline FAILED for ${env.BRANCH_NAME} - check the stage logs above" }
    }
}
