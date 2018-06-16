pipeline {
    agent {
        docker { image 'maven' }
    }
    stages {
        stage('Build') {
            steps {
               echo 'This is a minimal pipeline!'
               sh "mvn --batch-mode -V -U -e clean install"
            }
        }
    }
    post {
            always {
                archiveArtifacts artifacts: '**/target/surefire-reports/*.xml', fingerprint: true
                junit '**/target/surefire-reports/*.xml'
            }
        }
}