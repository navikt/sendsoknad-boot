package no.nav.sbl.dialogarena.sikkerhet;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.StringValue;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.EnvironmentRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.enrichers.SecurityContextRequestEnricher;
import no.nav.modig.security.tilgangskontroll.policy.pdp.DecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pdp.picketlink.PicketLinkDecisionPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.EnforcementPoint;
import no.nav.modig.security.tilgangskontroll.policy.pep.PEPImpl;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;
import no.nav.sbl.dialogarena.config.SikkerhetsConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.tokensupport.TokenUtils;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.util.Objects;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static no.nav.modig.security.tilgangskontroll.utils.AttributeUtils.*;
import static no.nav.modig.security.tilgangskontroll.utils.RequestUtils.forRequest;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tilgangskontrollimplementasjon som bruker EnforcementPoint fra modig.security
 */
@Named("tilgangskontroll")
public class Tilgangskontroll {

    private static final Logger logger = getLogger(Tilgangskontroll.class);

    public static final String URN_ENDEPUNKT = "urn:nav:ikt:tilgangskontroll:xacml:resource:endepunkt";

    private final EnforcementPoint pep;
    @Autowired
    private SoknadService soknadService;

    public Tilgangskontroll() {
        DecisionPoint pdp = new PicketLinkDecisionPoint(SikkerhetsConfig.class.getResource("/security/policyConfig.xml"));
        pep = new PEPImpl(pdp);
        ((PEPImpl) pep).setRequestEnrichers(asList(new EnvironmentRequestEnricher(), new SecurityContextRequestEnricher()));
    }

    public void verifiserBrukerHarTilgangTilSoknad(String behandlingsId) {
        Long soknadId = null;
        String aktoerId = "undefined";
        try {
            WebSoknad soknad = soknadService.hentSoknad(behandlingsId, false, false);
            soknadId = soknad.getSoknadId();
            aktoerId = soknad.getAktoerId();
        } catch (Exception e) {
            logger.warn("Kunne ikke avgjøre hvem som eier søknad med behandlingsId {} -> Ikke tilgang.", behandlingsId, e);
        }
        verifiserBrukerHarTilgangTilSoknad(aktoerId, soknadId);
    }

    public void verifiserBrukerHarTilgangTilHenvendelse(String behandlingsId) {
        String aktorId = TokenUtils.getSubject();

        SubjectAttribute aktorSubjectId = new SubjectAttribute(new URN("urn:nav:ikt:tilgangskontroll:xacml:subject:aktor-id"), new StringValue(aktorId));

        pep.assertAccess(
                forRequest(resourceType("HENVENDELSE"),
                        resourceId(behandlingsId),
                        ownerId(aktorId),
                        resourceAttribute(URN_ENDEPUNKT, "Ekstern"),
                        actionId("Read"), aktorSubjectId));
    }

    public void verifiserBrukerHarTilgangTilSoknad(String eier, Long soknadId) {
        if (Objects.isNull(eier)) {
            throw new AuthorizationException("");
        }
        String aktorId = TokenUtils.getSubject();
        SubjectAttribute aktorSubjectId = new SubjectAttribute(new URN("urn:nav:ikt:tilgangskontroll:xacml:subject:aktor-id"), new StringValue(aktorId));

        try {
            pep.assertAccess(
                    forRequest(
                            resourceType("Soknad"),
                            resourceId(valueOf(soknadId)),
                            ownerId(eier),
                            aktorSubjectId));

        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthorizationException(e.getMessage());
        }
    }
}
