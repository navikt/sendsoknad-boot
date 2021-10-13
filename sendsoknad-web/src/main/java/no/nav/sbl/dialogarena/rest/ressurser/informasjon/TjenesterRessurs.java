package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.AktivitetOgMaalgrupperFetcherService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.security.token.support.core.api.Protected;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Produces(APPLICATION_JSON)
//@TODO hva skall vi gjøre med dette ?@Timed
public class TjenesterRessurs {

    @Autowired
    private AktivitetOgMaalgrupperFetcherService aktivitetOgMaalgrupperFetcherService;

    @GET
    @Path("/aktiviteter")
    @Protected
    public List<Faktum> hentAktiviteter() {
        return aktivitetOgMaalgrupperFetcherService.hentAktiviteter(TokenUtils.getFoedselsnummer());
    }

    @GET
    @Path("/vedtak")
    @Protected
    public List<Faktum> hentVedtak() {
        return aktivitetOgMaalgrupperFetcherService.hentVedtak(TokenUtils.getFoedselsnummer());
    }

    @GET
    @Path("/maalgrupper")
    @Protected
    public List<Faktum> hentMaalgrupper() {
        return aktivitetOgMaalgrupperFetcherService.hentMaalgrupper(TokenUtils.getFoedselsnummer());
    }
}
