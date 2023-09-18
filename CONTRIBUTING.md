To run the tests for this project, clone this repository and perform the following actions:

1. `cd test-app`
2. Create a file named `gradle-local.properties` and add `mlPassword=value`, substituting "value" for the password 
   of your MarkLogic admin user. 
3. Run `./gradlew -i mlDeploy` to deploy a test application to MarkLogic. 
4. `cd ..` to return to the root directory of the repository.

You can now run `mvn clean test` to run the tests. 

