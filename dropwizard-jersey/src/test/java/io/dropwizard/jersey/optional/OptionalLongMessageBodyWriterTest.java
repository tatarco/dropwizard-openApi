package io.dropwizard.jersey.optional;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalLongMessageBodyWriterTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(new EmptyOptionalExceptionMapper())
                .register(OptionalLongReturnResource.class);
    }

    @Test
    void presentOptionalsReturnTheirValue() {
        assertThat(target("optional-return")
                .queryParam("id", "1").request()
                .get(Long.class))
                .isEqualTo(1L);
    }

    @Test
    void presentOptionalsReturnTheirValueWithResponse() {
        assertThat(target("optional-return/response-wrapped")
                .queryParam("id", "1").request()
                .get(Long.class))
                .isEqualTo(1L);
    }

    @Test
    void absentOptionalsThrowANotFound() {
        Invocation.Builder request = target("optional-return").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(Long.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(404));
    }

    @Test
    void valueSetIgnoresDefault() {
        assertThat(target("optional-return/default").queryParam("id", "1").request().get(Long.class))
            .isEqualTo(target("optional-return/long/default").queryParam("id", "1").request().get(Long.class))
            .isEqualTo(1L);
    }

    @Test
    void valueNotSetReturnsDefault() {
        assertThat(target("optional-return/default").request().get(Long.class))
            .isEqualTo(target("optional-return/long/default").request().get(Long.class))
            .isEqualTo(0L);
    }

    @Test
    void valueEmptyReturnsDefault() {
        assertThat(target("optional-return/default").queryParam("id", "").request().get(Long.class))
            .isEqualTo(target("optional-return/long/default").queryParam("id", "").request().get(Long.class))
            .isEqualTo(0L);
    }

    @Test
    void valueInvalidReturns404() {
        Invocation.Builder request = target("optional-return/default").queryParam("id", "invalid")
            .request();
        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> request.get(Long.class));
        Invocation.Builder longRequest = target("optional-return/long/default").queryParam("id", "invalid")
            .request();
        assertThatExceptionOfType(NotFoundException.class)
            .isThrownBy(() -> longRequest.get(Long.class));
    }

    @Path("optional-return")
    @Produces(MediaType.TEXT_PLAIN)
    public static class OptionalLongReturnResource {
        @GET
        public OptionalLong showWithQueryParam(@QueryParam("id") OptionalLong id) {
            return id;
        }

        @POST
        public OptionalLong showWithFormParam(@FormParam("id") OptionalLong id) {
            return id;
        }

        @Path("response-wrapped")
        @GET
        public Response showWithQueryParamResponse(@QueryParam("id") OptionalLong id) {
            return Response.ok(id).build();
        }

        @Path("default")
        @GET
        public OptionalLong showWithQueryParamAndDefaultValue(@QueryParam("id") @DefaultValue("0") OptionalLong id) {
            return id;
        }

        @Path("long/default")
        @GET
        public Long showWithLongQueryParamAndDefaultValue(@QueryParam("id") @DefaultValue("0") Long id) {
            return id;
        }
    }
}
