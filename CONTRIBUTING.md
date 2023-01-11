To run the tests in this project, first deploy a test application to your MarkLogic instance:

    ./gradlew -i mlDeploy

This currently assumes that admin/admin will work for authentication; this will be improved in the near future. 
This will deploy an application with an app server on port 8007 in your MarkLogic instance; please ensure that 
this port is available. 

Instructions on running sonar tests coming soon!
