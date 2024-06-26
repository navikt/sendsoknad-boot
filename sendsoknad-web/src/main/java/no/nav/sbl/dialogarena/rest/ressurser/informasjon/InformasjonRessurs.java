package no.nav.sbl.dialogarena.rest.ressurser.informasjon;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import no.nav.sbl.dialogarena.rest.Logg;
import no.nav.sbl.dialogarena.sendsoknad.domain.PersonAlder;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.InformasjonService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.LandOgPostInfoFetcherService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.consumer.PersonInfoFetcherService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import no.nav.sbl.dialogarena.utils.InnloggetBruker;

/**
 * Klassen håndterer rest kall for å hente informasjon
 */
@Controller
@Path("/informasjon")
@Produces(APPLICATION_JSON)
//@Timed // @TODO hva skall vi gjøre med dette ?
@ProtectedWithClaims(issuer = "tokenx", claimMap = {TokenUtils.ACR_LEVEL4, TokenUtils.ACR_IDPORTEN_LOA_HIGH}, combineWithOr = true)
public class InformasjonRessurs {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformasjonRessurs.class);
    private static final Logger klientlogger = LoggerFactory.getLogger("klientlogger");

    @Autowired
    private InformasjonService informasjon;
    @Autowired
    private TekstHenter tekstHenter;
    @Autowired
    private InnloggetBruker innloggetBruker;
    @Autowired
    private LandOgPostInfoFetcherService landOgPostInfoFetcherService;
    @Autowired
    private PersonaliaBolk personaliaBolk;
    @Autowired
    private PersonInfoFetcherService personInfoFetcherService;
    @Autowired
    private WebSoknadConfig webSoknadConfig;
    @Autowired
    private TjenesterRessurs tjenesterRessurs;

    @Path("/tjenester")
    public Object getTjenesterRessurs() {
        return tjenesterRessurs;
    }

    @GET
    @Path("/miljovariabler")
    public Map<String, String> hentMiljovariabler() {
    	LOGGER.debug("Henter miljøvariabler");
        return informasjon.hentMiljovariabler();
    }

    @GET
    @Path("/personalia")
    public Personalia hentPersonalia() {
        return innloggetBruker.hentPersonalia();
    }

    @GET
    @Path("/poststed")
    @Produces("text/plain")
    public String hentPoststed(@QueryParam("postnummer") String postnummer) {
        return landOgPostInfoFetcherService.getPoststed(postnummer);
    }

    @GET
    @Path("/tekster")
    public Properties hentTekster(@QueryParam("type") String type, @QueryParam("sprak") String sprak) {
    	LOGGER.debug("henter tekster");
        return tekstHenter.getBundleFor(findMatchingType(type), getLocale(sprak));
    }

    private Locale getLocale(String sprak) {
        if (sprak == null || sprak.trim().isEmpty()) {
            sprak = "nb_NO";
        }
        return LocaleUtils.toLocale(sprak);
    }

    private String findMatchingType(String type) {

        List<String> bundleNames = KravdialogInformasjonHolder.getSoknadsKonfigurasjoner().stream()
                .map(KravdialogInformasjon::getBundleName)
                .map(String::toLowerCase)
                .collect(toList());

        if (isNotEmpty(type) && !bundleNames.contains(type.toLowerCase())) {
            String prefiksetType = "soknad" + type.toLowerCase();

            if (bundleNames.contains(prefiksetType)) {
                LOGGER.debug("Changed type '{}' to '{}'", type, prefiksetType);
                type = prefiksetType;
            }
        }
        return type;
    }

    @GET
    @Path("/land")
    public List<Land> hentLand(@QueryParam("filter") String filter) {
    	LOGGER.debug("entering land");
        return landOgPostInfoFetcherService.hentLand(filter);
    }

    @GET
    @Path("/soknadstruktur")
    public SoknadStruktur hentSoknadStruktur(@QueryParam("skjemanummer") String skjemanummer, @QueryParam("filter") String filter) {
    	LOGGER.debug("Henter soknadstruktur");
        SoknadStruktur soknadStruktur = webSoknadConfig.hentStruktur(skjemanummer);
        if ("temakode".equalsIgnoreCase(filter)) {
            SoknadStruktur miniSoknadstruktur = new SoknadStruktur();
            miniSoknadstruktur.setTemaKode(soknadStruktur.getTemaKode());
            return miniSoknadstruktur;
        }
        return soknadStruktur;
    }

    @GET
    @Deprecated  // Ble brukt av den gamle tiltakspenge løsningen, henting av ytelsesstatus fra Arena via Soap ikke lenger støttet.
    @Path("/utslagskriterier")
    public Map<String, Object> hentUtslagskriterier() {
        LOGGER.info("Hent utslagskriterier");
        String uid = TokenUtils.getSubject();
        Map<String, Object> utslagskriterierResultat = new HashMap<>();
        utslagskriterierResultat.put("ytelsesstatus", personInfoFetcherService.hentYtelseStatus(uid));

        try {
            Personalia personalia = personaliaBolk.hentPersonalia(uid);
            utslagskriterierResultat.put("alder", Integer.toString(new PersonAlder(uid).getAlder()));
            utslagskriterierResultat.put("fodselsdato", personalia.getFodselsdato());
            utslagskriterierResultat.put("bosattINorge", ((Boolean) !personalia.harUtenlandskAdresse()).toString());
            utslagskriterierResultat.put("registrertAdresse", personalia.getGjeldendeAdresse().getAdresse());
            utslagskriterierResultat.put("registrertAdresseGyldigFra", personalia.getGjeldendeAdresse().getGyldigFra());
            utslagskriterierResultat.put("registrertAdresseGyldigTil", personalia.getGjeldendeAdresse().getGyldigTil());
            utslagskriterierResultat.put("erBosattIEOSLand", personalia.erBosattIEOSLand());
            utslagskriterierResultat.put("statsborgerskap", personalia.getStatsborgerskap());

        } catch (Exception e) {
            LOGGER.error("Kunne ikke hente personalia", e);
            utslagskriterierResultat.put("error", e.getMessage());
        }
        return utslagskriterierResultat;
    }

    @POST
    @Path("/actions/logg")
    public void loggFraKlient(Logg logg) {
        String level = logg.getLevel();

        switch (level) {
            case "INFO" -> klientlogger.info(logg.melding());
            case "WARN", "ERROR" -> klientlogger.warn(logg.melding());
            default -> klientlogger.debug(logg.melding());
        }
    }
}
