package com.marklogic.mule.extension;

import org.junit.Test;

import javax.net.ssl.SSLPeerUnverifiedException;

import static org.junit.Assert.*;

public class ReadWithSSLTest extends AbstractFlowTester {

    @Override
    protected String getFlowTestFile() {
        return "insecure-ssl.xml";
    }

    @Test
    public void insecureWithAnyHostnameVerifier() {
        DocumentData data = runFlowGetDocumentData("hostnameVerifierANY");
        assertEquals("/metadataSamples/json/hello.json", data.getAttributes().getUri());
    }

    @Test
    public void insecureWithCommonHostnameVerifier() {
        try {
            runFlow("hostnameVerifierCOMMON");
            fail("Expected an exception due to the COMMON hostname verifier not trusting the self-signed " +
                "certificate in used by the test SSL app server. Only the ANY hostname verifier will work.");
        } catch (Exception ex) {
            assertTrue(
                "Unexpected error; expected error to fail due to hostname not being verifiable: " + ex.getMessage(),
                ex.getMessage().contains("javax.net.ssl.SSLPeerUnverifiedException:")
            );
            assertTrue(
                "Unexpected exception; expecting a MarkLogicIOException and then SSLPeerUnverifiedException: " + ex.getCause(),
                ex.getCause().getCause() instanceof SSLPeerUnverifiedException
            );
        }
    }
}
