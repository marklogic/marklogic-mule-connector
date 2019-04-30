In this directory is a sample flow that selects documents from one instance of MarkLogic that requires TLS/SSL connectivity (MarkLogic_Config_Input), and inserts them into another instance of MarkLogic that does not (MarkLogic_Config_output).

At a high level, in order for SSL connectivity from MuleSoft to MarkLogic to work, MuleSoft must "trust" that MarkLogic is really who it says it is.  To do this, we must create or aquire a certificate proving the MarkLogic server's "identity", and load it into a truststore which can be used in our flow.

To make it work:

-Create a Certificate Template under the Security section on the ML admin console
	-Open the MarkLogic admin console
	-Under Configure->Security->Certificate Templates, open the Create tab
	-Fill in all the appropriate fields.  Make a note of these fields, because they might be needed later.
	-Click ok.

-Enable SSL in the MarkLogic App Server
	-Under Configure->Groups, select the desired App Server
	-set "ssl certificate template" to the newly created certificate template	

-Export a server certificate from MarkLogic by clicking the "download" button on the status tab of the certificate template that was created in the previous step.  Save this as instruction-certificate.crt
	
-Create a Java Keystore containing the certificate downloaded from MarkLogic. This Java Keystore will be used as the client's truststore:

$ keytool -import -alias instruct-ml-cert -keystore myTrustStore -file instruction-certificate.crt

-Include the truststore file (truststore.jks) in the classpath of my Anypoint Studio project.  Adding it to $PROJECT_HOME/src/main/resources is one way to do this.

-Add a "TLS Context" to the Global Configuration Elements in the AnyPoint Studio Project.  TLS Context appears under the "Component Configurations" item.

	-Set "Name" in the TLS context to "markLogicTestContext"
	-Set "Path" to the name of the truststore file (truststore.jks)
	-Set "Password" to the password for the truststore file
	-Set "Type" to JKS
	-Set "Alias" to what was used when exported cert from MarkLogic (instruct-ml-cert)

-markLogicTestContext is now an option available under "TLS Context" on the MarkLogic Config dialog.  Select it to enable the connection to talk TLS with the MarkLogic server

At this point, SSL connectivity with basic or digest authentication should function.  Additional steps are needed to enable certificate authentication, aka 2-way SSL.  Essentially, we have to get the MarkLogic server to trust the MuleSoft client, similarly to how we made the client trust the server above.  This is done by aquiring or generating a certificate for the client that is trusted by the server.

*** To enable certificate authentication, using self-signed certificates:

Much of this instruction is inspired by: https://jamielinux.com/docs/openssl-certificate-authority/introduction.html  This is highly recommended reading.
A certificate authority (CA) trusted by both the client and server will be needed.   

-Create a certificate authority that can be used to set up trust between the client and server.
-NOTE: These instructions assume a working directory of /root/ca , but any directory can be used.

$ mkdir /root/ca
$ cd /root/ca
$ mkdir certs crl newcerts private csr
$ chmod 700 private
$ touch index.txt
$ echo 1000 > serial

-Create the root key

$ openssl genrsa -aes256 -out private/ca.key.pem 4096
$ chmod 400 private/ca.key.pem

-Download the root ca openssl configuration file here: https://jamielinux.com/docs/openssl-certificate-authority/_downloads/root-config.txt
	and save it here: /root/ca/openssl.cnf.  If needed, edit openssl.cnf and replace any instances of /root/ca with the value of the actual working path.

-Create the root certificate.  Match the fields from the certificate template created earlier, and make common name something along the lines of "Root CA"

$ openssl req -config openssl.cnf \
      -key private/ca.key.pem \
      -new -x509 -days 7300 -sha256 -extensions v3_ca \
      -out certs/ca.cert.pem
$ chmod 444 certs/ca.cert.pem	  

-Create the intermediate pair

$ mkdir /root/ca/intermediate
$ cd /root/ca/intermediate
$ mkdir certs crl csr newcerts private
$ chmod 700 private
$ touch index.txt
$ echo 1000 > serial
$ echo 1000 > crlnumber

-Download the intermediate ca openssl configuration file here: https://jamielinux.com/docs/openssl-certificate-authority/_downloads/intermediate-config.txt
	and save it here: /root/ca/intermediate/openssl.cnf.  If needed, edit openssl.cnf and replace any instances of /root/ca with the value of the actual working path.

-Create the intermediate key

$ cd /root/ca
$ openssl genrsa -aes256 \
      -out intermediate/private/intermediate.key.pem 4096
$ chmod 400 intermediate/private/intermediate.key.pem

-Create the intermediate certificate
	-NOTE: The details should be the same as those entered for the CA certificate, except CN must be different, something along the lines of "Intermediate CA"

$ cd /root/ca
$ openssl req -config intermediate/openssl.cnf -new -sha256 \
      -key intermediate/private/intermediate.key.pem \
      -out intermediate/csr/intermediate.csr.pem
	  
$ openssl ca -config openssl.cnf -extensions v3_intermediate_ca \
      -days 3650 -notext -md sha256 \
      -in intermediate/csr/intermediate.csr.pem \
      -out intermediate/certs/intermediate.cert.pem	  
	  
-Add the certificate authority to the list of those trusted by the MarkLogic server 
	-In the admin console, Configure->Security->Certificate Authorities
	-Open the import tab
	-Paste the entire contents of the PEM encoded itermediate CA certificate (intermediate/certs/intermediate.cert.pem) into the provided text area, and press ok
	-Open the import tab
	-Paste the entire contents of the PEM encoded root CA certificate (certs/ca.cert.pem) into the provided text area, and press ok
	
-Add the certificate authority to the list of those trusted by the application server in MarkLogic, and set it to Certificate authentication
	-In the admin console, select the desired application server from the App Servers on the main Configuration page
	-Set "authentication" to "certificate"
	-Make sure "ssl certificate template" is set to the certificate template created earlier
	-Expand "Show" under "ssl client certificate authorities"
	-Expand the name of the certificate authority
	-Check all the checkboxes under the selected certificate authority... Root CA and Intermediate CA should both be there.
	
-Download the MarkLogic server certificate request
	-In the admin console, Configure->Security->Certificate Templates
	-Select the appropriate certificate template
	-Open the status tab
	-The MarkLogic server's hostname should be in the Needed Certificate Requests section.  If the server name is not there, ensure that the application server has "ssl certificate template" set to this template
	-Click request
	-The Pending Certificate Requests section should now have the current server's hostname in it.  Click the download button to get the server certificate request. 
	-Extract the .csr file from the downloaded zip file, and save the .csr file to /root/ca/intermediate/csr

-Sign the server certificate request

$ cd /root/ca
$ openssl ca -config intermediate/openssl.cnf \
      -extensions server_cert -days 375 -notext -md sha256 \
      -in intermediate/csr/<servername>.csr \
      -out intermediate/certs/<servername>.pem

-Import the signed certificate back into MarkLogic
	-In the admin console, open Configure->Security->Certificate Templates
	-Select the appropriate certificate template
	-Open the import tab
	-Paste the entire contents of the PEM encoded, signed server certificate (certs/<servername>.pem) into the provided text area, and press ok

-Create a Java Keystore containing the signed MarkLogic certificate. This Java Keystore will be used as the client's truststore:

$ keytool -import -alias <servername> -keystore truststore.jks -file intermediate/certs/<servername>.pem
	
-Create the client user in MarkLogic
	-In the admin console, Configure->Security->Users
	-Open the Create tab
	-Fill in the fields.  The username must match the common name in the client certificate we will create later.  For the purpose of these instructions, the username will be instruction-user
	-Select the roles that are needed for the application
	-Click ok to create the user
	
-Create a private key for the client	

$ cd /root/ca
$ openssl genrsa -aes256 \
      -out intermediate/private/instruction-user.key.pem 2048
$ chmod 400 intermediate/private/instruction-user.key.pem

-Create a certificate for the client.  Note: the Common Name must be the username of the user above; instruction-user in this case.  The other details don't need to be anything specific.

$ cd /root/ca
$ openssl req -config intermediate/openssl.cnf \
      -key intermediate/private/instruction-user.key.pem \
      -new -sha256 -out intermediate/csr/instruction-user.csr.pem
$ openssl ca -config intermediate/openssl.cnf \
      -extensions usr_cert -days 375 -notext -md sha256 \
      -in intermediate/csr/instruction-user.csr.pem \
      -out intermediate/certs/instruction-user.cert.pem

-Convert the certificate and private key to PKCS 12 and create a java keystore from it
$ cd /root/ca
$ openssl pkcs12 -export -in intermediate/certs/instruction-user.cert.pem -inkey intermediate/private/instruction-user.key.pem -name instruction-user -out instruction-user.p12
$ keytool -importkeystore -destkeystore keystore.jks -srckeystore instruction-user.p12 -srcstoretype PKCS12	    

-Include the keystore file (keystore.jks) and the truststore file (truststore.jks) in the classpath of my Anypoint Studio project.  Adding them to $PROJECT_HOME/src/main/resources is one way to do this.
-Ensure that the global TLS Context element is configured such that the Trust Store Configuration and Key Store Configuration are correct:
	-The path for the Trust Store Configuration should be "truststore.jks"
	-The path for the Key Store Configuration should be "keystore.jks"
	-The alias for the key store configuration should be 

For troubleshooting help, visit this page: https://help.marklogic.com/Knowledgebase/Article/View/573/0/troubleshooting-marklogic-certificate-based-authentication