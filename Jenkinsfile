pipeline {
  agent {
    docker {
      image 'maven:3-alpine'
      args ' -v $HOME/.m2/:$HOME/.m2'
    }
  }
  options {
        copyArtifactPermission('../mtarget-backend*');
        
    }
  stages {
    stage('Build') {
      steps {
        echo 'Build Logback Appenders'
        copyArtifacts(projectName: 'vesko/v.008');
        sh 'mvn clean install -DskipTest -Dgpg.skip'
      }
    }

  }
  post {
        always {
            archiveArtifacts artifacts: '?/.m2/repository/co/mailtarget/logback-appenders/**/*.jar', fingerprint: true
            archiveArtifacts artifacts: '?/.m2/repository/co/mailtarget/logback-appenders/**/*.pom', fingerprint: true
        }
    }
}