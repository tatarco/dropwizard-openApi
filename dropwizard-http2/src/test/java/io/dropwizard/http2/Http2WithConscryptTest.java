package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.Security;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
class Http2WithConscryptTest extends AbstractHttp2Test {

    static {
        Security.addProvider(new OpenSSLProvider());
    }

    private static final String PREFIX = "tls_conscrypt";
    final DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
        FakeApplication.class, resourceFilePath("test-http2-with-conscrypt.yml"),
        PREFIX,
        config(PREFIX, "server.connector.keyStorePath", resourceFilePath("stores/http2_server.jks")),
        config(PREFIX, "server.connector.trustStorePath", resourceFilePath("stores/http2_client.jts"))
    );

    @Test
    void testHttp1WithCustomCipher() throws Exception {
        assertResponse(http1Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_1_1);
    }

    @Test
    void testHttp2WithCustomCipher() throws Exception {
        assertResponse(http2Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_2);
    }

}
