package org.lab.dev.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.lab.dev.security.TokenService;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/user")
@Tag(name = " user", description = "All the user methods")
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    TokenService tokenService;

    @POST
    @Path("/access-token")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccessToken(@QueryParam("username") String username, @QueryParam("password") String password) throws IOException, InterruptedException {
        return tokenService.getAccessToken(username, password);
    }

    @GET
    @RolesAllowed("user")
    @Path("/current/info")
    public JsonWebToken getCurrentUserInfo() {
        return jwt;
    }

    @GET()
    @Path("/current/info-alternative")
    public Principal getCurrentUserInfoAlternative(@Context SecurityContext ctx) {
        return ctx.getUserPrincipal();
    }

    @GET
    @RolesAllowed("user")
    @Path("/current/info/claims")
    public Map<String, Object> getCurrentUserInfoClaims() {
        return jwt.getClaimNames()
                .stream()
                .map(name -> Map.entry(name, jwt.getClaim(name)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue)
                );
    }
}
