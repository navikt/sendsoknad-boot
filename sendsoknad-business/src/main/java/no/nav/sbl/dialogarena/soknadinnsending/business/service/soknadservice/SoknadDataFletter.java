package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadTilleggsstonader;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.MigrasjonHandterer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggFraHenvendelsePopulator;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingskjedeElement;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.joda.time.DateTime;
import org.joda.time.base.BaseDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.sort;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.ELDSTE_FORST;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.NYESTE_FORST;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.TilleggsInfoService.createTilleggsInfoJsonString;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);
    private static final boolean MED_DATA = true;
    private static final boolean MED_VEDLEGG = true;
    private final Predicate<WSBehandlingskjedeElement> STATUS_FERDIG = soknad -> FERDIG.equals(valueOf(soknad.getStatus()));

    private final ApplicationContext applicationContext;
    private final HenvendelseService henvendelseService;
    private final FillagerService fillagerService;
    private final VedleggFraHenvendelsePopulator vedleggFraHenvendelsePopulator;
    private final FaktaService faktaService;
    private final SoknadRepository lokalDb;
    private final HendelseRepository hendelseRepository;
    private final WebSoknadConfig config;
    AlternativRepresentasjonService alternativRepresentasjonService;
    private final SoknadMetricsService soknadMetricsService;
    private final MigrasjonHandterer migrasjonHandterer;
    private final SkjemaOppslagService skjemaOppslagService;
    private final LegacyInnsendingService legacyInnsendingService;

    private Map<String, BolkService> bolker;


    @Autowired
    public SoknadDataFletter(ApplicationContext applicationContext, HenvendelseService henvendelseService,
                             FillagerService fillagerService, VedleggFraHenvendelsePopulator vedleggService, FaktaService faktaService,
                             @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb, HendelseRepository hendelseRepository, WebSoknadConfig config,
                             AlternativRepresentasjonService alternativRepresentasjonService,
                             SoknadMetricsService soknadMetricsService, MigrasjonHandterer migrasjonHandterer,
                             SkjemaOppslagService skjemaOppslagService, LegacyInnsendingService legacyInnsendingService,
                             Map<String, BolkService> bolker) {
        super();
        this.applicationContext = applicationContext;
        this.henvendelseService = henvendelseService;
        this.fillagerService = fillagerService;
        this.vedleggFraHenvendelsePopulator = vedleggService;
        this.faktaService = faktaService;
        this.lokalDb = lokalDb;
        this.hendelseRepository = hendelseRepository;
        this.config = config;
        this.alternativRepresentasjonService = alternativRepresentasjonService;
        this.soknadMetricsService = soknadMetricsService;
        this.migrasjonHandterer = migrasjonHandterer;
        this.skjemaOppslagService = skjemaOppslagService;
        this.legacyInnsendingService = legacyInnsendingService;
        this.bolker = bolker;
    }


    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
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
            vedleggFraHenvendelsePopulator.populerVedleggMedDataFraHenvendelse(soknadFraFillager, fillagerService.hentFiler(soknadFraFillager.getBrukerBehandlingId()));
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

    @Transactional
    public String startSoknad(String skjemanummer, String aktorId) {

        KravdialogInformasjon kravdialog = KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);
        String soknadnavn = kravdialog.getSoknadTypePrefix();
        SoknadType soknadType = kravdialog.getSoknadstype();
        String tilleggsInfo = createTilleggsInfoJsonString(skjemaOppslagService, skjemanummer);
        String mainUuid = randomUUID().toString();

        //Timer startTimer = createDebugTimer("startTimer", soknadnavn, mainUuid);

        // Timer henvendelseTimer = createDebugTimer("startHenvendelse", soknadnavn, mainUuid);
        String behandlingsId = henvendelseService.startSoknad(aktorId, skjemanummer, tilleggsInfo, mainUuid, soknadType);
        // henvendelseTimer.stop();
        // henvendelseTimer.report();


        //   Timer oprettIDbTimer = createDebugTimer("oprettIDb", soknadnavn, mainUuid);
        int versjon = kravdialog.getSkjemaVersjon();
        Long soknadId = lagreSoknadILokalDb(skjemanummer, mainUuid, aktorId, behandlingsId, versjon).getSoknadId();
        faktaService.lagreFaktum(soknadId, bolkerFaktum(soknadId));
        faktaService.lagreSystemFaktum(soknadId, personalia(soknadId));


        // oprettIDbTimer.stop();
        // oprettIDbTimer.report();

        lagreTommeFaktaFraStrukturTilLokalDb(soknadId, skjemanummer, soknadnavn, mainUuid);

        soknadMetricsService.startetSoknad(skjemanummer, false);

        // startTimer.stop();
        // startTimer.report();
        return behandlingsId;
    }

   /* private Timer createDebugTimer(String name, String soknadsType, String id) {
        Timer timer = MetricsFactory.createTimer("debug.startsoknad." + name);
        timer.addFieldToReport("soknadstype", soknadsType);
        timer.addFieldToReport("randomid", id);
        timer.start();
        return timer;
    }*/

    private void lagreTommeFaktaFraStrukturTilLokalDb(Long soknadId, String skjemanummer, String soknadsType, String id) {
        //  Timer strukturTimer = createDebugTimer("lagStruktur", soknadsType, id);
        List<FaktumStruktur> faktaStruktur = config.hentStruktur(skjemanummer).getFakta();
        sort(faktaStruktur, sammenlignEtterDependOn());
        //  strukturTimer.stop();
        //  strukturTimer.report();

        //  Timer lagreTimer = createDebugTimer("lagreTommeFakta", soknadsType, id);

        List<Faktum> fakta = new ArrayList<>();
        List<Long> faktumIder = lokalDb.hentLedigeFaktumIder(faktaStruktur.size());
        Map<String, Long> faktumKeyTilFaktumId = new HashMap<>();
        int idNr = 0;

        for (FaktumStruktur faktumStruktur : faktaStruktur) {
            if (faktumStruktur.ikkeSystemFaktum() && faktumStruktur.ikkeFlereTillatt()) {
                Long faktumId = faktumIder.get(idNr++);

                Faktum faktum = new Faktum()
                        .medFaktumId(faktumId)
                        .medSoknadId(soknadId)
                        .medKey(faktumStruktur.getId())
                        .medType(BRUKERREGISTRERT);

                faktumKeyTilFaktumId.put(faktumStruktur.getId(), faktumId);

                if (faktumStruktur.getDependOn() != null) {
                    Long parentId = faktumKeyTilFaktumId.get(faktumStruktur.getDependOn().getId());
                    faktum.setParrentFaktum(parentId);
                }

                fakta.add(faktum);
            }
        }

        lokalDb.batchOpprettTommeFakta(fakta);

        //    lagreTimer.stop();
        //    lagreTimer.report();
    }

    private WebSoknad lagreSoknadILokalDb(String skjemanummer, String uuid, String aktorId, String behandlingsId, int versjon) {
        WebSoknad nySoknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId)
                .medskjemaNummer(skjemanummer)
                .medUuid(uuid)
                .medAktorId(aktorId)
                .medOppretteDato(DateTime.now())
                .medVersjon(versjon);

        Long soknadId = lokalDb.opprettSoknad(nySoknad);
        nySoknad.setSoknadId(soknadId);
        return nySoknad;
    }

    private Faktum bolkerFaktum(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medKey("bolker")
                .medType(BRUKERREGISTRERT);
    }

    private Faktum personalia(Long soknadId) {
        return new Faktum()
                .medSoknadId(soknadId)
                .medType(SYSTEMREGISTRERT)
                .medKey("personalia");
    }

    public WebSoknad hentSoknad(String behandlingsId, boolean medData, boolean medVedlegg) {
        return hentSoknad(behandlingsId, medData, medVedlegg, true);
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

    private boolean erForbiUtfyllingssteget(WebSoknad soknad) {
        return !(soknad.getDelstegStatus() == DelstegStatus.OPPRETTET ||
                soknad.getDelstegStatus() == DelstegStatus.UTFYLLING);
    }

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

    private final Predicate<Map.Entry<String, String>> isDatoProperty = property -> {
        List<String> datoKeys = new ArrayList<>();
        datoKeys.add("tom");
        datoKeys.add("fom");
        return datoKeys.contains(property.getKey());
    };

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

    public void sendSoknad(String behandlingsId, byte[] pdf, byte[] fullSoknad) {
        WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);

        logger.info("Sender inn søknadfor behandling {}", soknad.getBrukerBehandlingId());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        legacyInnsendingService.sendSoknad(soknad, pdf, fullSoknad);

        lokalDb.slettSoknad(soknad, HendelseType.INNSENDT);
        soknadMetricsService.sendtSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
    }


    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        return henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(STATUS_FERDIG)
                .min(ELDSTE_FORST)
                .map(WSBehandlingskjedeElement::getInnsendtDato)
                .map(BaseDateTime::getMillis)
                .orElseThrow(() -> new SendSoknadException(String.format("Kunne ikke hente ut opprinneligInnsendtDato for %s", behandlingsId)));
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        return henvendelseService.hentBehandlingskjede(behandlingsId).stream()
                .filter(STATUS_FERDIG)
                .min(NYESTE_FORST)
                .get()
                .getBehandlingsId();
    }
}
