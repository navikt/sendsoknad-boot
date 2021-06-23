package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadTilleggsstonader;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MigrasjonHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedlegFraHenvendelsePopulator;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;

public class HentSoknadDataService {
	
	private static final Logger logger = getLogger(SoknadDataFletter.class);
	
	private SoknadRepository lokalDb;
	
	VedlegFraHenvendelsePopulator vedleggService;
	
	private MigrasjonHandterer migrasjonHandterer;
	 
	private WebSoknadConfig config;
	
	private HenvendelseService henvendelseService;
	
	private FillagerService fillagerService;
	
	private HendelseRepository hendelseRepository;
	
	private FaktaService faktaService;
	
	private Map<String, BolkService> bolker;
	
	
    
    
    
    
	    
	   
    
	
	public HentSoknadDataService(SoknadRepository lokalDb, VedlegFraHenvendelsePopulator vedleggService,
			MigrasjonHandterer migrasjonHandterer, WebSoknadConfig config, HenvendelseService henvendelseService,
			FillagerService fillagerService, HendelseRepository hendelseRepository, FaktaService faktaService,
			Map<String, BolkService> bolker, Predicate<Entry<String, String>> isDatoProperty) {
		super();
		this.lokalDb = lokalDb;
		this.vedleggService = vedleggService;
		this.migrasjonHandterer = migrasjonHandterer;
		this.config = config;
		this.henvendelseService = henvendelseService;
		this.fillagerService = fillagerService;
		this.hendelseRepository = hendelseRepository;
		this.faktaService = faktaService;
		this.bolker = bolker;
		this.isDatoProperty = isDatoProperty;
	}

	public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg, boolean populerSystemfakta) {
        WebSoknad soknadFraLokalDb;

        if (medVedlegg) {
            soknadFraLokalDb = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        } else {
            soknadFraLokalDb = lokalDb.hentSoknad(behandlingsId);
        }

        WebSoknad soknad;
        if (medData) {
            soknad = soknadFraLokalDb != null ? lokalDb.hentSoknadMedData(soknadFraLokalDb.getSoknadId()) : hentFraHenvendelse(behandlingsId, true);
        } else {
            soknad = soknadFraLokalDb != null ? soknadFraLokalDb : hentFraHenvendelse(behandlingsId, false);
        }

        if (medData) {
            soknad = populerSoknadMedData(populerSystemfakta, soknad);
        }

        return erForbiUtfyllingssteget(soknad) ? sjekkDatoVerdierOgOppdaterDelstegStatus(soknad) : soknad;
    }
	
	 private Predicate<Map.Entry<String, String>> isDatoProperty = property -> {
	        List<String> datoKeys = new ArrayList<>();
	        datoKeys.add("tom");
	        datoKeys.add("fom");
	        return datoKeys.contains(property.getKey());
	    };
	
	public WebSoknad sjekkDatoVerdierOgOppdaterDelstegStatus(WebSoknad soknad) {

        DateTimeFormatter formaterer = DateTimeFormat.forPattern("yyyy-MM-dd");

        if (new SoknadTilleggsstonader().getSkjemanummer().contains(soknad.getskjemaNummer())) {
            soknad.getFakta().stream()
                    .filter(erFaktumViVetFeiler(soknad))
                    .forEach(faktum -> {
                        try {
                            faktum.getProperties().entrySet().stream()
                                    .filter(isDatoProperty)
                                    .forEach(property -> {
                                        if (property.getValue() == null) {
                                            throw new IllegalArgumentException("Invalid format: value = null");
                                        }
                                        formaterer.parseLocalDate(property.getValue());
                                    });
                        } catch (IllegalArgumentException e) {
                            soknad.medDelstegStatus(DelstegStatus.UTFYLLING);

                            logger.warn("catch IllegalArgumentException " + e.getMessage()
                                    + " -  Søknad med skjemanr: " + soknad.getskjemaNummer() + " har ikke gyldig dato-property for faktum " + faktum.getKey()
                                    + " -  BehandlingId: " + soknad.getBrukerBehandlingId());

                    //        Event event = MetricsFactory.createEvent("stofo.korruptdato");
                    //        event.addTagToReport("stofo.korruptdato.behandlingId", soknad.getBrukerBehandlingId());
                    //        event.report();
                        }
                    });
        }
        return soknad;
    }
	
	private Predicate<Faktum> erFaktumViVetFeiler(WebSoknad soknad) {
        List<String> faktumFeilerKeys = new ArrayList<>();
        boolean harValgtFlereReisesamlinger = soknad.getValueForFaktum("informasjonsside.stonad.reisesamling").equals("true") &&
                soknad.getValueForFaktum("reise.samling.fleresamlinger").equalsIgnoreCase("flere");
        boolean harValgtDagligReise = soknad.getValueForFaktum("informasjonsside.stonad.reiseaktivitet").equals("true");
        boolean harValgtBostotte = soknad.getValueForFaktum("informasjonsside.stonad.bostotte").equals("true");

        if (harValgtFlereReisesamlinger) {
            faktumFeilerKeys.add("reise.samling.fleresamlinger.samling");
        }
        if (harValgtDagligReise) {
            faktumFeilerKeys.add("reise.samling.aktivitetsperiode");
        }
        if (harValgtBostotte) {
            faktumFeilerKeys.add("bostotte.samling");
        }
        return faktum -> faktumFeilerKeys.contains(faktum.getKey());
    }

	
	private boolean erForbiUtfyllingssteget(WebSoknad soknad) {
        return !(soknad.getDelstegStatus() == DelstegStatus.OPPRETTET ||
                soknad.getDelstegStatus() == DelstegStatus.UTFYLLING);
    }
	
	private WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        Optional<XMLMetadata> hovedskjemaOptional = ((XMLMetadataListe) wsSoknadsdata.getAny()).getMetadata().stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLHovedskjema)
                .findFirst();

        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.orElseThrow(() -> new SendSoknadException("Kunne ikke hente opp søknad"));

        SoknadInnsendingStatus status = valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            WebSoknad soknadFraFillager = unmarshal(new ByteArrayInputStream(fillagerService.hentFil(hovedskjema.getUuid())), WebSoknad.class);
            soknadFraFillager.medOppretteDato(wsSoknadsdata.getOpprettetDato());
            lokalDb.populerFraStruktur(soknadFraFillager);
            vedleggService.populerVedleggMedDataFraHenvendelse(soknadFraFillager, fillagerService.hentFiler(soknadFraFillager.getBrukerBehandlingId()));
            if (hentFaktumOgVedlegg) {
                return lokalDb.hentSoknadMedVedlegg(behandlingsId);
            }
            return lokalDb.hentSoknad(behandlingsId);
        } else {
            // søkndadsdata er slettet i henvendelse, har kun metadata
            return new WebSoknad()
                    .medBehandlingId(behandlingsId)
                    .medStatus(status)
                    .medskjemaNummer(hovedskjema.getSkjemanummer());
        }
    }

	
	private WebSoknad populerSoknadMedData(boolean populerSystemfakta, WebSoknad soknad) {
        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medVersjon(hendelseRepository.hentVersjon(soknad.getBrukerBehandlingId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));


        soknad = migrasjonHandterer.handterMigrasjon(soknad);

        if (populerSystemfakta) {
            String uid = soknad.getAktoerId();

            if (soknad.erEttersending()) {
                faktaService.lagreSystemFakta(soknad, bolker.get(PersonaliaBolk.class.getName()).genererSystemFakta(uid, soknad.getSoknadId()));
            } else {
                List<Faktum> systemfaktum = new ArrayList<>();
                for (BolkService bolk : WebSoknadConfig.getSoknadBolker(soknad, bolker.values())) {
                    systemfaktum.addAll(bolk.genererSystemFakta(uid, soknad.getSoknadId()));
                }
                faktaService.lagreSystemFakta(soknad, systemfaktum);
            }
        }

        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
        return soknad;
    }


}
