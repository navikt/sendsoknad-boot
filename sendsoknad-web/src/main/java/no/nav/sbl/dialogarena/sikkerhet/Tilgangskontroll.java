package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tilgangskontrollimplementasjon som bruker EnforcementPoint fra modig.security
 */
@Named("tilgangskontroll")
public class Tilgangskontroll {

    private static final Logger logger = getLogger(Tilgangskontroll.class);

    @Autowired
    private SoknadService soknadService;

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        Long soknadId = null;
        String aktoerId = "undefined";
        try {
            WebSoknad soknad = soknadService.hentSoknad(behandlingsId, false, false);
            soknadId = soknad.getSoknadId();
            aktoerId = soknad.getAktoerId();
        } catch (Exception e) {
            logger.error("{}: Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.",
                    behandlingsId, behandlingsId, e);
        }
        verifiserBrukerHarTilgangTilSoknad(aktoerId, soknadId);
    }

    public void verifiserBrukerHarTilgangTilHenvendelse(String behandlingsId) {
        String aktorId = TokenUtils.getSubject();
        if (Objects.isNull(aktorId)) {
            logger.warn("{}: verifiserBrukerHarTilgangTilHenvendelse - Bruker har ikke tilgang til søknad", behandlingsId);
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }
    }

    void verifiserBrukerHarTilgangTilSoknad(String eier, Long soknadId) {
        String aktorId = TokenUtils.getSubject();
        if (Objects.isNull(eier) || !eier.equals(aktorId)) {
            logger.warn("verifiserBrukerHarTilgangTilSoknad - Bruker har ikke tilgang til søknad med id: {}", soknadId);
            throw new AuthorizationException("Bruker har ikke tilgang til søknaden.");
        }
    }
}
