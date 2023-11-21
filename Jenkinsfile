@Library('shared-libraries') _
pipeline{
  agent {label 'devExpLinuxPool'}
  environment{
    JAVA_HOME_DIR="/home/builder/java/openjdk-1.8.0-262"
    MVN_HOME="/home/builder/maven/apache-maven-3.9.5"
    GRADLE_DIR   =".gradle"
    DMC_USER     = credentials('MLBUILD_USER')
    DMC_PASSWORD = credentials('MLBUILD_PASSWORD')
  }
  options {
    checkoutToSubdirectory 'marklogic-mule-connector'
    buildDiscarder logRotator(artifactDaysToKeepStr: '7', artifactNumToKeepStr: '', daysToKeepStr: '30', numToKeepStr: '')
  }
  stages{
    stage('tests'){
      steps{
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
          mvn dependency:resolve
          sed -i.bak 's/4.3.0-20220221/4.5.0-20220221/g' ~/.m2/repository/com/mulesoft/munit/2.3.11/munit-2.3.11.pom
          mvn clean package -DskipTests
          mvn test
        '''
        junit '**/target/surefire-reports/**/*.xml'
      }
    }
  }
}