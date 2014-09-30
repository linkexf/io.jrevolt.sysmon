package io.jrevolt.sysmon.rest;

import io.jrevolt.sysmon.model.DomainDef;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 * @version $Id$
 */
@Path("/") @Produces({"application/json", "application/xml"})
public interface RestService {


	@GET @Path("version")
	String version();

	@GET @Path("restart")
	default void restart() { throw new UnsupportedOperationException(); }

	@GET @Path("domain")
	DomainDef getDomainDef();

	@GET @Path("jnlp/{resource}")
	Response resource(@PathParam("resource") String resource);

	@POST @Path("checkAll")
	void checkAll();

}
