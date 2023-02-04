package pl.rtprog.java2flow.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/api/user")
public class UserAPI {
    static class UserInfo {
        public String email;
    }

    @Path("{id}")
    @GET
    public UserInfo getUser(@PathParam("id") String id) {
        return new UserInfo();
    }
}
