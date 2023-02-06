package pl.rtprog.java2flow.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/api/user")
public class UserAPI {
    static class UserInfo {
        public String email;
    }

    @Path("{id}")
    @GET
    public CompletionStage<UserInfo> getUser(@PathParam("id") String id) {
        CompletableFuture<UserInfo> res=new CompletableFuture<>();
        res.complete(new UserInfo());
        return res;
    }
}
