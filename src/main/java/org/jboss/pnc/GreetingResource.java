package org.jboss.pnc;

import io.quarkus.panache.common.Parameters;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Set;

@Path("/")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("create")
    @Transactional
    public String create() {
        FinalLog finalLog = new FinalLog();
        finalLog.tags = Set.of("yo", "yu");
        finalLog.persist();
        return "Hello from Quarkus REST";
    }

    @GET
    @Path("delete")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String delete() {
        Parameters parameters =Parameters.with("tag", "yu");
        FinalLog.delete("from FinalLog where :tag in elements(tags)", parameters);
        return "here";
    }
}
