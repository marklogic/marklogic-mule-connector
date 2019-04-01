In this directory is a sample flow that selects documents from one instance of MarkLogic that requires TLS/SSL connectivity (MarkLogic_Config_Input), and inserts them into another instance of MarkLogic that does not (MarkLogic_Config_output).

To make it work I:

-Enabled SSL on my App Server.  This involved creating a Certificate Template under the Security section on the ML admin console.

-Exported a certificate from MarkLogic by clicking the "download" button on the status tab of the certificate template that I created in the previous step.
	
-Created a java keystore:

$ keytool -genkey -alias mule -keyalg RSA -keystore keystore.jks

	Enter keystore password: password
	Re-enter new password: password
	What is your first and last name?
	  [Unknown]:
	What is the name of your organizational unit?
	  [Unknown]:  MarkLogic
	What is the name of your organization?
	  [Unknown]:  MarkLogic
	What is the name of your City or Locality?
	  [Unknown]:
	What is the name of your State or Province?
	  [Unknown]:
	What is the two-letter country code for this unit?
	  [Unknown]:
	Is CN=Unknown, OU=MarkLogic, O=MarkLogic, L=Unknown, ST=Unknown, C=Unknown correct?
	  [no]:  yes

	Enter key password for <mule> password
			(RETURN if same as keystore password):
	Re-enter new password: password
**

-Added the certificate I downloaded from MarkLogic to the keystore I created above (NOTE! alias is important... be sure to make it something you'll remember):

$ keytool -import -trustcacerts -alias from-ml -file from-ml-certificate.crt -keystore keystore.jks
	Enter keystore password: password
	// a description of the cert is displayed here //
	Trust this certificate? [no]:  yes
	Certificate was added to keystore

-Included in the keystore file (keystore.jks) in the classpath of my Anypoint Studio project (added it to $PROJECT_HOME/src/main/resources)

-Added a "TLS Context" to the Global Configuration Elements in my AnyPoint Studio Project.  TLS Context appears under the "Component Configurations" item.

	-Set "Name" in the TLS context to "markLogicTestContext"
	-Set "Path" to the name of my keystore file (keystore.jks)
	-Set "Password" to the password for the keystore file (password)
	-Set "Type" to JKS
	-Set "Alias" to what was used when exported cert from MarkLogic

-markLogicTestContext is now an option available under "TLS Context" on the MarkLogic Config dialog.  Select it to enable the connection to talk TLS with the MarkLogic server