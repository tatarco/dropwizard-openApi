package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessage;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import io.dropwizard.jersey.params.UUIDParam;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.jupiter.api.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalFormParamResourceTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(OptionalFormParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    void shouldReturnDefaultMessageWhenMessageIsNotPresent() throws IOException {
        final String defaultMessage = "Default Message";
        final Response response = target("/optional/message").request().post(Entity.form(new MultivaluedStringMap()));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMessageWhenMessageBlank() throws IOException {
        final Form form = new Form("message", "");
        final Response response = target("/optional/message").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEmpty();
    }

    @Test
    void shouldReturnMessageWhenMessageIsPresent() throws IOException {
        final String customMessage = "Custom Message";
        final Form form = new Form("message", customMessage);
        final Response response = target("/optional/message").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(customMessage);
    }

    @Test
    void shouldReturnDefaultMessageWhenMyMessageIsNotPresent() throws IOException {
        final String defaultMessage = "My Default Message";
        final Response response = target("/optional/my-message").request().post(Entity.form(new MultivaluedStringMap()));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMyMessageWhenMyMessageIsPresent() throws IOException {
        final String myMessage = "My Message";
        final Form form = new Form("mymessage", myMessage);
        final Response response = target("/optional/my-message").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(myMessage);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidUUIDIsPresent() throws IOException {
        final String invalidUUID = "invalid-uuid";
        final Form form = new Form("uuid", invalidUUID);
        final Response response = target("/optional/uuid").request().post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void shouldReturnDefaultUUIDWhenUUIDIsNotPresent() throws IOException {
        final String defaultUUID = "d5672fa8-326b-40f6-bf71-d9dacf44bcdc";
        final Response response = target("/optional/uuid").request().post(Entity.form(new MultivaluedStringMap()));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultUUID);
    }

    @Test
    void shouldReturnUUIDWhenValidUUIDIsPresent() throws IOException {
        final String uuid = "fd94b00d-bd50-46b3-b42f-905a9c9e7d78";
        final Form form = new Form("uuid", uuid);
        final Response response = target("/optional/uuid").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(uuid);
    }

    @Path("/optional")
    public static class OptionalFormParamResource {

        @POST
        @Path("/message")
        public String getMessage(@FormParam("message") Optional<String> message) {
            return message.or("Default Message");
        }

        @POST
        @Path("/my-message")
        public String getMyMessage(@FormParam("mymessage") Optional<MyMessage> myMessage) {
            return myMessage.or(new MyMessage("My Default Message")).getMessage();
        }

        @POST
        @Path("/uuid")
        public String getUUID(@FormParam("uuid") Optional<UUIDParam> uuid) {
            return uuid.or(new UUIDParam("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")).get().toString();
        }
    }
}
