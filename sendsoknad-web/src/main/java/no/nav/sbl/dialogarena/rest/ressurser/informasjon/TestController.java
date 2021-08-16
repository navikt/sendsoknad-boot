package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.springframework.stereotype.Controller;

@Controller
@Path("/test")
public class TestController {

	@GET()
	@Path("/ping")
	public String ping() {
		return "pingg";
	}
}
