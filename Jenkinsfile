// Jenkinsfile
pipeline {
  agent any
    tools {
      jdk 'JDK17'      // must match the name you set under Manage Jenkins -> Tools
      maven 'Maven3'   // same here
    }
  options {
    timestamps()
    ansiColor('xterm')
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
  }

  parameters {
    // --- Git ---
    string(name: 'REPO_URL', defaultValue: 'https://github.com/adarshnayak1908/BookStore_Automation.git', description: 'Git repository to clone')
    string(name: 'BRANCH',   defaultValue: 'master', description: 'Branch to build')

    // --- API config (passed to tests & TokenManager via -D system properties) ---
    string(name: 'API_BASE',        defaultValue: 'http://127.0.0.1:8000', description: 'Base URI of the API under test')
    string(name: 'API_LOGIN_PATH',  defaultValue: '/login',                description: 'Login endpoint path')
    string(name: 'API_USER',        defaultValue: 'test@example.com',      description: 'Login email/username')
    password(name: 'API_PASS',      defaultValue: '',                      description: 'Login password (kept secret in Jenkins)')
    string(name: 'API_ID',          defaultValue: '1',                     description: 'User id if your LoginRequest requires it')

    // Optional: if your repo is private, set a Jenkins credentials ID and fill it here
    string(name: 'GIT_CREDENTIALS_ID', defaultValue: '', description: 'Credentials ID for private repos (leave empty if public)')
  }

  environment {
    // Maven will cache dependencies across builds in this volume path
    MAVEN_OPTS = '-Dmaven.wagon.http.pool=false -Dmaven.wagon.http.retryHandler.count=3'
  }

  stages {
    stage('Checkout') {
      steps {
        deleteDir()
        script {
          if (params.GIT_CREDENTIALS_ID?.trim()) {
            checkout([
              $class: 'GitSCM',
              branches: [[name: "*/${params.BRANCH}"]],
              userRemoteConfigs: [[url: params.REPO_URL, credentialsId: params.GIT_CREDENTIALS_ID]]
            ])
          } else {
            // public repo
            git branch: params.BRANCH, url: params.REPO_URL
          }
        }
      }
    }

    stage('Build & Test') {
      steps {
        sh '''
          echo "JAVA VERSION:"
          java -version

          echo "MAVEN VERSION:"
          mvn -v

          # Run tests and generate reports
          mvn -B -e -U \
            -Dapi.base=${API_BASE} \
            -Dapi.login.path=${API_LOGIN_PATH} \
            -Dapi.user=${API_USER} \
            -Dapi.pass=${API_PASS} \
            -Dapi.id=${API_ID} \
            clean test
        '''
      }
      post {
        always {
          // JUnit (Surefire) XMLs, if any
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
      }
    }

    stage('Publish Reports') {
      steps {
        script {
          // Publish Extent Spark HTML if present
          publishHTML(target: [
            allowMissing: true,
            keepAll: true,
            alwaysLinkToLastBuild: true,
            reportDir: 'target',
            reportFiles: 'ExtentSparkReport.html',
            reportName: 'Extent Report'
          ])

          // Also publish Cucumber HTML (if you kept cucumber html plugin)
          publishHTML(target: [
            allowMissing: true,
            keepAll: true,
            alwaysLinkToLastBuild: true,
            reportDir: 'target',
            reportFiles: 'cucumber-reports.html,cucumber-reports/index.html',
            reportName: 'Cucumber HTML'
          ])
        }

        // Archive everything useful (HTML/PDF/JSON/images)
        archiveArtifacts allowEmptyArchive: true, artifacts: '''
          target/**/*.html,
          target/**/*.pdf,
          target/**/*.json,
          target/**/*.png,
          target/**/*.jpg,
          target/extent*/**/*,
          target/spark*/**/*,
          test-output/**/*.html,
          test-output/**/*.pdf
        '''.stripIndent().trim()
      }
    }
  }

  post {
    success {
      echo '✅ Build succeeded. Open “Extent Report” in the left panel to view the report.'
    }
    unstable {
      echo '⚠️ Build unstable. Check tests and Extent report.'
    }
    failure {
      echo '❌ Build failed. See console log and reports.'
    }
    always {
      // Handy pointers in the console
      script {
        echo "If the Extent report didn't publish, verify your extent.properties points Spark to target/ExtentSparkReport.html"
      }
    }
  }
}
