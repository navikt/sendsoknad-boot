package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetOgVedtakDagligReiseListeResponse;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.sakogaktivitet.v1.meldinger.WSFinnAktivitetsinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class AktivitetService {

    private static final Predicate<Faktum> BARE_AKTIVITETER_SOM_KAN_HA_STONADER = faktum ->
            faktum.harPropertySomMatcher("erStoenadsberettiget", "true");
    private static final Logger logger = LoggerFactory.getLogger(AktivitetService.class);

    private final SakOgAktivitetV1 aktivitetWebService;



    @Autowired
    public AktivitetService(@Qualifier("sakOgAktivitetEndpoint") SakOgAktivitetV1 aktivitetWebService) {
		this.aktivitetWebService = aktivitetWebService;
	}

	public List<Faktum> hentAktiviteter(String fodselnummer) {
        try {
            WSFinnAktivitetsinformasjonListeResponse aktiviteter = aktivitetWebService.finnAktivitetsinformasjonListe(lagAktivitetsRequest(fodselnummer));
            if (aktiviteter == null) {
                logger.info("HentAktiviteter: ingen funnet");
                return Collections.emptyList();
            }
            return aktiviteter.getAktivitetListe().stream()
                    .map(new AktiviteterTransformer())
                    .filter(BARE_AKTIVITETER_SOM_KAN_HA_STONADER)
                    .peek(l -> logger.info("HentAktiviteter"+ l.getValue()))
                    .collect(toList());

        } catch (FinnAktivitetsinformasjonListePersonIkkeFunnet e) {
            logger.debug("Person ikke funnet i arena: {}", fodselnummer, e);
            return Collections.emptyList();
        } catch (FinnAktivitetsinformasjonListeSikkerhetsbegrensning e) {
            throw new SikkerhetsBegrensningException(e.getMessage(), e);
        }
    }

    public List<Faktum> hentVedtak(String fodselsnummer) {
        try {
            WSFinnAktivitetOgVedtakDagligReiseListeRequest request = new WSFinnAktivitetOgVedtakDagligReiseListeRequest()
                    .withPersonident(fodselsnummer)
                    .withPeriode(new WSPeriode().withFom(LocalDate.now().minusMonths(6)).withTom(LocalDate.now().plusMonths(2)));
            WSFinnAktivitetOgVedtakDagligReiseListeResponse response = aktivitetWebService.finnAktivitetOgVedtakDagligReiseListe(request);

            if (response == null) {
                logger.info("hentVedtak: ikke funnet");
                return Collections.emptyList();
            }
            logger.info("hentVedtak: funnet");
            return response.getAktivitetOgVedtakListe().stream()
                    .flatMap(new VedtakTransformer())
                    .peek(l -> logger.info("HentVedtak:"+ l.getValue()))
                    .collect(toList());

        } catch (FinnAktivitetOgVedtakDagligReiseListePersonIkkeFunnet e) {
            logger.debug("Person ikke funnet i arena: {}", fodselsnummer, e);
            return Collections.emptyList();
        } catch (FinnAktivitetOgVedtakDagligReiseListeSikkerhetsbegrensning e) {
            throw new SikkerhetsBegrensningException(e.getMessage(), e);
        }
    }

    private WSFinnAktivitetsinformasjonListeRequest lagAktivitetsRequest(String fodselnummer) {
        return new WSFinnAktivitetsinformasjonListeRequest()
                .withPersonident(fodselnummer)
                .withPeriode(new WSPeriode().withFom(LocalDate.now().minusMonths(6)).withTom(LocalDate.now().plusMonths(2)));
    }


    private static class AktiviteterTransformer implements Function<WSAktivitet, Faktum> {

        @Override
        public Faktum apply(WSAktivitet wsAktivitet) {
            Faktum faktum = new Faktum()
                    .medKey("aktivitet")
                    .medProperty("id", wsAktivitet.getAktivitetId())
                    .medProperty("navn", wsAktivitet.getAktivitetsnavn())
                    .medProperty("type", wsAktivitet.getAktivitetstype().getValue())
                    .medProperty("arrangoer", wsAktivitet.getArrangoer());

            WSPeriode periode = wsAktivitet.getPeriode();
            faktum.medProperty("fom", ServiceUtils.datoTilString(periode.getFom()));
            faktum.medProperty("tom", ServiceUtils.datoTilString(periode.getTom()));
            faktum.medProperty("erStoenadsberettiget", "" + wsAktivitet.isErStoenadsberettigetAktivitet());

            return faktum;
        }

    }

    private static class VedtakTransformer implements Function<WSAktivitetOgVedtak, Stream<Faktum>> {
        @Override
        public Stream<Faktum> apply(WSAktivitetOgVedtak wsAktivitetOgVedtak) {
            return wsAktivitetOgVedtak.getSaksinformasjon().getVedtaksinformasjon().stream()
                    .map(wsVedtaksinformasjon -> transformerTilFaktum(wsVedtaksinformasjon, wsAktivitetOgVedtak));

        }

        private Faktum transformerTilFaktum(WSVedtaksinformasjon input, WSAktivitetOgVedtak aktivitet) {
            Faktum faktum = new Faktum()
                    .medKey("vedtak")
                    .medProperty("aktivitetId", aktivitet.getAktivitetId())
                    .medProperty("aktivitetNavn", aktivitet.getAktivitetsnavn())
                    .medProperty("tema", hentTema(aktivitet.getSaksinformasjon()))
                    .medProperty("erStoenadsberettiget", "" + aktivitet.isErStoenadsberettigetAktivitet())
                    .medProperty("forventetDagligParkeringsutgift", ServiceUtils.nullToBlank(input.getForventetDagligParkeringsutgift()))
                    .medProperty("dagsats", ServiceUtils.nullToBlank(input.getDagsats()))
                    .medProperty("trengerParkering", ServiceUtils.nullToBlank(input.isTrengerParkering()))
                    .medProperty("id", input.getVedtakId());
            WSPeriode periode = aktivitet.getPeriode();
            faktum.medProperty("aktivitetFom", ServiceUtils.datoTilString(periode.getFom()));
            faktum.medProperty("aktivitetTom", ServiceUtils.datoTilString(periode.getTom()));

            periode = input.getPeriode();
            faktum.medProperty("fom", ServiceUtils.datoTilString(periode.getFom()));
            faktum.medProperty("tom", ServiceUtils.datoTilString(periode.getTom()));

            return faktum;
        }

        private String hentTema(WSSaksinformasjon saksinformasjon) {
            return Optional.ofNullable(saksinformasjon)
                    .map(WSSaksinformasjon::getSakstype)
                    .map(WSKodeverdi::getValue)
                    .orElse("TSO");
        }
    }

}
