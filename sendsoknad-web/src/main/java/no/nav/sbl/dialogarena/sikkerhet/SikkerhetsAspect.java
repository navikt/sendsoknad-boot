package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;

import static no.nav.sbl.dialogarena.sikkerhet.XsrfGenerator.sjekkXsrfToken;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Aspect
@Component
public class SikkerhetsAspect {

    private static final Logger logger = getLogger(SikkerhetsAspect.class);

    @Autowired
    private Tilgangskontroll tilgangskontroll;
    @Autowired
    private FaktaService faktaService;
    @Autowired
    private VedleggService vedleggService;

    @Pointcut("@annotation(no.nav.sbl.dialogarena.sikkerhet.SjekkTilgangTilSoknad)")
    public void requestMapping() {
    }

    @Before(value = "requestMapping() && args(id, ..) && @annotation(tilgang)", argNames = "id, tilgang")
    public void sjekkOmBrukerHarTilgang(Object id, SjekkTilgangTilSoknad tilgang) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        logger.info("performing tilgangsjekk");
        String behandlingsId;
        switch (tilgang.type()) {
            case Faktum:
                behandlingsId = faktaService.hentBehandlingsId((Long) id);
                break;
            case Vedlegg:
                behandlingsId = vedleggService.hentBehandlingsId((Long) id);
                break;
            case Behandling: // Fall-through
            default:
                behandlingsId = (String) id;
        }

        if (behandlingsId == null) {
            throw new NotFoundException("Fant ikke ressurs.");
        }

        logger.info("Sjekker tilgang til ressurs med behandlingsId {} og type {}", behandlingsId, tilgang.type());
        if (tilgang.sjekkXsrf() && skrivOperasjon(request)) {
            sjekkXsrfToken(request.getHeader("X-XSRF-TOKEN"), behandlingsId);
        }

        if (tilgang.type() == SjekkTilgangTilSoknad.Type.Henvendelse) {
            tilgangskontroll.verifiserBrukerHarTilgangTilHenvendelse(behandlingsId);
        } else {
            tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(behandlingsId);
        }
    }

    private static boolean skrivOperasjon(HttpServletRequest request) {
        String method = request.getMethod();
        return method.equals(POST.name()) || method.equals(PUT.name()) || method.equals(DELETE.name());
    }
}
