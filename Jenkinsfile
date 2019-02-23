pipeline {
    agent any

    stages {
//        stage("init") {
//
//        }
        stage("build") {
            steps {
                sh 'java -version'
                sh './gradlew clean'
                sh './gradlew build'
            }
        }
        stage('publish') {
            when {
                branch 'master'
            }
            steps {
                sh './gradlew publish -S'
            }
        }
        stage("gradle plugin") {
            when {
                branch 'master'
            }
            steps {
                withCredentials([file(credentialsId: 'gradlePluginProperties', variable: 'PROPERTIES')]) {
                    sh '''
                    cat "$PROPERTIES" >> gradle.properties
                    ./gradlew publishPlugins
                    '''
                }
            }
        }
        stage('counter') {
            steps {
                sh './gradlew buildnumberIncrease'
            }
        }
    }

}