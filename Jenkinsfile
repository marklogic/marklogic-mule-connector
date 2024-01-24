@Library('shared-libraries') _
pipeline{
  agent {label 'devExpLinuxPool'}
  environment{
    JAVA_HOME_DIR="/home/builder/java/openjdk-1.8.0-262"
    JAVA11_HOME_DIR="/home/builder/java/jdk-11.0.20"
    MVN_HOME="/home/builder/maven/apache-maven-3.9.5"
    GRADLE_DIR   =".gradle"
    DMC_USER     = credentials('MLBUILD_USER')
    DMC_PASSWORD = credentials('MLBUILD_PASSWORD')
    scannerHome = tool 'SONAR_Progress'
  }
  options {
    checkoutToSubdirectory 'marklogic-mule-connector'
    buildDiscarder logRotator(artifactDaysToKeepStr: '7', artifactNumToKeepStr: '', daysToKeepStr: '30', numToKeepStr: '')
  }
  stages{
    stage('tests'){
      steps{
      withSonarQubeEnv('SONAR_Progress') {
      withCredentials([string(credentialsId: 'mule-connector-repo-password', variable: 'mule-repository-password')]) {
        copyRPM 'Release','11.1.0'
        setUpML '$WORKSPACE/xdmp/src/Mark*.rpm'
        sh label:'runtests', script: '''#!/bin/bash
          export JAVA_HOME=$JAVA_HOME_DIR
          export GRADLE_USER_HOME=$WORKSPACE/$GRADLE_DIR
          export PATH=$JAVA_HOME/bin:$MVN_HOME/bin:$GRADLE_USER_HOME:$PATH
          cd $WORKSPACE/marklogic-mule-connector/test-app
          echo "mlPassword=admin" > gradle-local.properties
          ./gradlew -i mlDeploy
          cd $WORKSPACE/marklogic-mule-connector/
          mvn --settings ./settings.xml clean install -Dmule.repository.password=$mule-repository-password
           export JAVA_HOME=$JAVA11_HOME_DIR
           export GRADLE_USER_HOME=$WORKSPACE/$GRADLE_DIR
           export PATH=$JAVA_HOME/bin:$MVN_HOME/bin:$GRADLE_USER_HOME:$PATH
           mvn sonar:sonar  -Dsonar.projectKey=marklogic_marklogic-mule-connector_AYw7_z2UhXuvzhhRmJgp -Dsonar.projectName=ADP-ML-DevExp-marklogic-mule-connector
        '''
        junit '**/target/surefire-reports/**/*.xml'
        }
        }
      }
    }
  }
}