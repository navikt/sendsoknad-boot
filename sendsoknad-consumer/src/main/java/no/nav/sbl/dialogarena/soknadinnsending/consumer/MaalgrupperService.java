package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.FinnMaalgruppeinformasjonListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.FinnMaalgruppeinformasjonListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.MaalgruppeV1;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSMaalgruppe;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.informasjon.WSPeriode;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeRequest;
import no.nav.tjeneste.virksomhet.maalgruppe.v1.meldinger.WSFinnMaalgruppeinformasjonListeResponse;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaalgrupperService {
    private static final Logger LOG = LoggerFactory.getLogger(MaalgrupperService.class);

    private static final String DATOFORMAT = "yyyy-MM-dd";

   
    private MaalgruppeV1 maalgruppeinformasjon;
    
    
    @Autowired
    public MaalgrupperService(@Qualifier("maalgruppeEndpoint") MaalgruppeV1 maalgruppeinformasjon) {
		super();
		this.maalgruppeinformasjon = maalgruppeinformasjon;
	}

	public List<Faktum> hentMaalgrupper(String fodselsnummer) {
        try {
            WSFinnMaalgruppeinformasjonListeResponse maalgrupper = maalgruppeinformasjon.finnMaalgruppeinformasjonListe(lagRequest(fodselsnummer));

            return maalgrupper.getMaalgruppeListe().stream()
                    .map(MaalgrupperService::maalgruppeTilFaktum)
                    .collect(Collectors.toList());

        } catch (FinnMaalgruppeinformasjonListeSikkerhetsbegrensning e) {
            throw new RuntimeException(e);
        } catch (FinnMaalgruppeinformasjonListePersonIkkeFunnet e) {
            LOG.debug("Fant ikke person: " + fodselsnummer, e);
        } catch (Exception ex) {
            //Maalgruppetjenesten er nede etter kl 8 på kvelden. Om den er nede skal vi bare returnere en tom liste.
            LOG.debug("Maalgruppetjeneste nede: ", ex);
        }
        return Collections.emptyList();
    }

    private WSFinnMaalgruppeinformasjonListeRequest lagRequest(String fodselsnummer) {
        return new WSFinnMaalgruppeinformasjonListeRequest()
                .withPersonident(fodselsnummer)
                .withPeriode(new WSPeriode()
                                .withFom(LocalDate.now().minusMonths(6))
                                .withTom(LocalDate.now().plusMonths(2))
                );
    }

    private static Faktum maalgruppeTilFaktum(WSMaalgruppe maalgruppe) {
        return new Faktum()
                .medKey("maalgruppe")
                .medProperty("navn", maalgruppe.getMaalgruppenavn())
                .medProperty("fom", datoTilString(maalgruppe.getGyldighetsperiode().getFom()))
                .medProperty("tom", datoTilString(maalgruppe.getGyldighetsperiode().getTom()))
                .medProperty("kodeverkVerdi", maalgruppe.getMaalgruppetype().getValue());
    }

    private static String datoTilString(LocalDate date) {
        return date != null ? date.toString(DATOFORMAT) : "";
    }
}
