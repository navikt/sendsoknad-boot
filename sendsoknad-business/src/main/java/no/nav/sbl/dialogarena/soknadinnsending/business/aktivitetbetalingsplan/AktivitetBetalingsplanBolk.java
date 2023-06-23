package no.nav.sbl.dialogarena.soknadinnsending.business.aktivitetbetalingsplan;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AuthorizationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadRefusjonDagligreise;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.SakOgAktivitetV1;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSBetalingsplan;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.datoTilString;

@Component
public class AktivitetBetalingsplanBolk implements BolkService {

    private static final Logger logger = LoggerFactory.getLogger(AktivitetBetalingsplanBolk.class);

    private final FaktaService faktaService;

    private final SakOgAktivitetV1 aktivitetWebService;


    @Autowired
    public AktivitetBetalingsplanBolk(FaktaService faktaService, @Qualifier("sakOgAktivitetEndpoint") SakOgAktivitetV1 aktivitetWebService) {
        super();
        this.faktaService = faktaService;
        this.aktivitetWebService = aktivitetWebService;
    }


    private static Function<WSBetalingsplan, Faktum> betalingplanTilFaktum(final Long soknadId) {
        return wsVedtaksinformasjon -> {
            Faktum betalingsplan = new Faktum().medKey("vedtak.betalingsplan")
                    .medUnikProperty("id")
                    .medSoknadId(soknadId)
                    .medProperty("id", wsVedtaksinformasjon.getBetalingsplanId())
                    .medProperty("fom", datoTilString(wsVedtaksinformasjon.getUtgiftsperiode().getFom()))
                    .medProperty("tom", datoTilString(wsVedtaksinformasjon.getUtgiftsperiode().getTom()))
                    .medProperty("refunderbartBeloep", "" + wsVedtaksinformasjon.getBeloep())
                    .medProperty("alleredeSokt", "" + StringUtils.isNotBlank(wsVedtaksinformasjon.getJournalpostId()));
            if (StringUtils.isNotBlank(wsVedtaksinformasjon.getJournalpostId())) {
                betalingsplan.medProperty("sokerForPeriode", "false");
            }
            return betalingsplan;
        };
    }


    @Override
    public String tilbyrBolk() {
        return SoknadRefusjonDagligreise.VEDTAKPERIODER;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        Faktum vedtakFaktum = faktaService.hentFaktumMedKey(soknadId, "vedtak");
        if (vedtakFaktum != null) {
            return hentBetalingsplanerForVedtak(soknadId, fodselsnummer, vedtakFaktum.getProperties().get("aktivitetId"),
                    vedtakFaktum.getProperties().get("id"));
        }
        return emptyList();
    }

    public List<Faktum> hentBetalingsplanerForVedtak(Long soknadId, String fodselsnummer, final String aktivitetId, final String vedtakId) {
        logger.info("Henter betalingsplanner for vedtak");
        try {
            WSFinnAktivitetOgVedtakDagligReiseListeRequest request = new WSFinnAktivitetOgVedtakDagligReiseListeRequest()
                    .withPersonident(fodselsnummer)
                    .withPeriode(new WSPeriode().withFom(LocalDate.now().minusMonths(6)).withTom(LocalDate.now().plusMonths(2)));
            WSFinnAktivitetOgVedtakDagligReiseListeResponse response = aktivitetWebService.finnAktivitetOgVedtakDagligReiseListe(request);
            if (response == null) {
                return emptyList();
            }

            return response.getAktivitetOgVedtakListe().stream()
                    .filter(wsAktivitetOgVedtak -> wsAktivitetOgVedtak.getAktivitetId().equals(aktivitetId))
                    .flatMap(wsAktivitetOgVedtak -> wsAktivitetOgVedtak.getSaksinformasjon().getVedtaksinformasjon().stream())
                    .filter(vedtak -> vedtak.getVedtakId().equals(vedtakId))
                    .flatMap(wsVedtaksinformasjon -> wsVedtaksinformasjon.getBetalingsplan().stream())
                    .map(betalingplanTilFaktum(soknadId))
                    .peek(t -> logger.info(t.toString()))
                    .collect(Collectors.toList());

        } catch (FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet e) {
            logger.debug("person ikke funnet", e);
            return emptyList();
        } catch (FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning e) {
            throw new SikkerhetsBegrensningException(e.getMessage(), e);
        }
    }
}
