package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.toInnsendingsvalg;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.TilleggsInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;

@Component
public class VedlegHentOgPersistService {
	
    private static final Logger logger = getLogger(VedleggService.class);

	private VedleggRepository vedleggRepository;
	
	private SkjemaOppslagService skjemaOppslagService;
	
	

	@Autowired
	public VedlegHentOgPersistService(VedleggRepository vedleggRepository, SkjemaOppslagService skjemaOppslagService) {
		super();
		this.vedleggRepository = vedleggRepository;
		this.skjemaOppslagService = skjemaOppslagService;
	}

	public List<Vedlegg> hentVedleggOgPersister(XMLMetadataListe xmlVedleggListe, Long soknadId) {

        List<XMLMetadata> vedlegg = xmlVedleggListe.getMetadata().stream()
                .filter(metadata -> metadata instanceof XMLVedlegg)
                .collect(Collectors.toList());

        List<Vedlegg> soknadVedlegg = new ArrayList<>();
        for (XMLMetadata xmlMetadata : vedlegg) {
            if (xmlMetadata instanceof XMLHovedskjema) {
                continue;
            }
            XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;

            Integer antallSider = xmlVedlegg.getSideantall() != null ? xmlVedlegg.getSideantall() : 0;

            Vedlegg v = new Vedlegg()
                    .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                    .medAntallSider(antallSider)
                    .medInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medOpprinneligInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medSoknadId(soknadId)
                    .medNavn(xmlVedlegg.getTilleggsinfo() != null ?
                            TilleggsInfoService.lesTittelFraJsonString(xmlVedlegg.getTilleggsinfo())
                            : xmlVedlegg.getSkjemanummerTillegg());

            String skjemanummerTillegg = xmlVedlegg.getSkjemanummerTillegg();
            if (isNotBlank(skjemanummerTillegg)) {
                v.setSkjemaNummer(v.getSkjemaNummer() + "|" + skjemanummerTillegg);
            }

            vedleggRepository.opprettEllerEndreVedlegg(v, null);
            soknadVedlegg.add(v);
        }

        leggTilKodeverkFelter(soknadVedlegg);
        return soknadVedlegg;
    }
	
	private void leggTilKodeverkFelter(List<Vedlegg> vedleggListe) {
        for (Vedlegg vedlegg : vedleggListe) {
            medKodeverk(vedlegg);
        }
    }
	
	public void medKodeverk(Vedlegg vedlegg) {
        try {
            String skjemanummer = vedlegg.getSkjemaNummer().replaceAll("\\|.*", "");
            vedlegg.leggTilURL("URL", skjemaOppslagService.getUrl(skjemanummer));
            vedlegg.setTittel(skjemaOppslagService.getTittel(skjemanummer));

        } catch (Exception e) {
            String skjemanummer = vedlegg != null ? vedlegg.getSkjemaNummer() : null;
            logger.warn("Tried to set Tittel/URL for Vedlegg with skjemanummer '" + skjemanummer + "', but got exception. Ignoring exception and continuing...", e);
        }
    }

	
}
