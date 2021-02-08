pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args ' -v $HOME/.m2/:$HOME/.m2'
    }

  }
  stages {
    stage('Build') {
      steps {
        echo 'Build Logback Appenders'
        sh 'mvn clean install -DskipTest -Dgpg.skip'
      }
    }

  }
  post {
        always {
            archiveArtifacts artifacts: '?/.m2/repository/co/mailtarget/logback-appenders/**/*.jar', fingerprint: true
        }
    }
}