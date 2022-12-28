package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.AAPUtlandetInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadTilleggsstonader;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto;
import no.nav.sbl.soknadinnsending.innsending.brukernotifikasjon.BrukernotifikasjonService;
import no.nav.soknad.arkivering.soknadsfillager.model.FileData;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus.FERDIG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur.sammenlignEtterDependOn;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadDataFletter {

    private static final Logger logger = getLogger(SoknadDataFletter.class);
    private static final boolean MED_DATA = true;
    private static final boolean MED_VEDLEGG = true;

    private final ApplicationContext applicationContext;
    private final VedleggRepository vedleggRepository;
    private final FaktaService faktaService;
    private final SoknadRepository lokalDb;
    private final HendelseRepository hendelseRepository;
    private final WebSoknadConfig config;
    AlternativRepresentasjonService alternativRepresentasjonService;
    private final SoknadMetricsService soknadMetricsService;
    private final InnsendingService innsendingService;
    private final Filestorage filestorage;
    private final BrukernotifikasjonService brukernotifikasjonService;

    private Map<String, BolkService> bolker;

    @Autowired
    public SoknadDataFletter(
            ApplicationContext applicationContext,
            VedleggRepository vedleggRepository,
            FaktaService faktaService,
            @Qualifier("soknadInnsendingRepository") SoknadRepository lokalDb,
            HendelseRepository hendelseRepository,
            WebSoknadConfig config,
            AlternativRepresentasjonService alternativRepresentasjonService,
            SoknadMetricsService soknadMetricsService,
            InnsendingService innsendingService,
            Filestorage filestorage,
            Map<String, BolkService> bolker,
            BrukernotifikasjonService brukernotifikasjonService
    ) {
        this.applicationContext = applicationContext;
        this.vedleggRepository = vedleggRepository;
        this.faktaService = faktaService;
        this.lokalDb = lokalDb;
        this.hendelseRepository = hendelseRepository;
        this.config = config;
        this.alternativRepresentasjonService = alternativRepresentasjonService;
        this.soknadMetricsService = soknadMetricsService;
        this.innsendingService = innsendingService;
        this.filestorage = filestorage;
        this.bolker = bolker;
        this.brukernotifikasjonService = brukernotifikasjonService;
    }


    @PostConstruct
    public void initBolker() {
        bolker = applicationContext.getBeansOfType(BolkService.class);
    }


    @Transactional
    public String startSoknad(String skjemanummer, String fnr) {

        KravdialogInformasjon kravdialog = KravdialogInformasjonHolder.hentKonfigurasjon(skjemanummer);

        int versjon = kravdialog.getSkjemaVersjon();
        String tittel = SkjemaOppslagService.getTittel(skjemanummer);
        String mainUuid = randomUUID().toString();
        String behandlingsId = UUID.randomUUID().toString();

        Long soknadId = lagreSoknadILokalDb(skjemanummer, mainUuid, fnr, behandlingsId, versjon).getSoknadId();
        faktaService.lagreFaktum(soknadId, bolkerFaktum(soknadId));
        faktaService.lagreSystemFaktum(soknadId, personalia(soknadId));

        lagreTommeFaktaFraStrukturTilLokalDb(soknadId, skjemanummer);

        soknadMetricsService.startetSoknad(skjemanummer, false);
        try {
            brukernotifikasjonService.newNotification(tittel, behandlingsId, behandlingsId, false, fnr);
        } catch (Exception e) {
            logger.error("{}: Failed to create new Brukernotifikasjon", behandlingsId, e);
            throw e;
        }
        return behandlingsId;
    }


    private void lagreTommeFaktaFraStrukturTilLokalDb(Long soknadId, String skjemanummer) {
        List<FaktumStruktur> faktaStruktur = config.hentStruktur(skjemanummer).getFakta();
        faktaStruktur.sort(sammenlignEtterDependOn());

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
        WebSoknad soknad;

        if (medVedlegg) {
            soknad = lokalDb.hentSoknadMedVedlegg(behandlingsId);
        } else {
            soknad = lokalDb.hentSoknad(behandlingsId);
        }
        if (soknad == null) {
            throw new IkkeFunnetException("Søknad ikke funnet", new RuntimeException("Ukjent søknad"), behandlingsId);
        }

        if (medData) {
            soknad = populerSoknadMedData(soknad);
            storeVedleggThatAreNotInFilestorage(soknad);
        }


        if (erForbiUtfyllingssteget(soknad) && erSoknadTillegsstonader(soknad))
            sjekkDatoVerdierOgOppdaterDelstegStatus(soknad);

        logger.info("{}: hentSoknad status={}, erEttersending: {}, antall vedlegg={}",
                behandlingsId, soknad.getStatus(), soknad.erEttersending(), soknad.getVedlegg().size());
        return soknad;
    }

    private boolean erForbiUtfyllingssteget(WebSoknad soknad) {
        return !(soknad.getDelstegStatus() == DelstegStatus.OPPRETTET ||
                soknad.getDelstegStatus() == DelstegStatus.UTFYLLING);
    }

    private boolean erSoknadTillegsstonader(WebSoknad soknad) {
        return new SoknadTilleggsstonader().getSkjemanummer().contains(soknad.getskjemaNummer());
    }

    void sjekkDatoVerdierOgOppdaterDelstegStatus(WebSoknad soknad) {

        DateTimeFormatter formaterer = DateTimeFormat.forPattern("yyyy-MM-dd");

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

                        logger.warn(soknad.getBrukerBehandlingId() + ": catch IllegalArgumentException " + e.getMessage()
                                + " -  Søknad med skjemanr: " + soknad.getskjemaNummer() + " har ikke gyldig dato-property for faktum " + faktum.getKey()
                                + " -  BehandlingId: " + soknad.getBrukerBehandlingId());
                    }
                });
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

    private WebSoknad populerSoknadMedData(WebSoknad soknad) {
        Integer versjon = hendelseRepository.hentVersjon(soknad.getBrukerBehandlingId());

        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medVersjon(versjon)
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));

        lagreSystemFakta(soknad);

        soknad = lokalDb.hentSoknadMedData(soknad.getSoknadId());
        soknad.medSoknadPrefix(config.getSoknadTypePrefix(soknad.getSoknadId()))
                .medSoknadUrl(config.getSoknadUrl(soknad.getSoknadId()))
                .medStegliste(config.getStegliste(soknad.getSoknadId()))
                .medFortsettSoknadUrl(config.getFortsettSoknadUrl(soknad.getSoknadId()));
        return soknad;
    }

    private void lagreSystemFakta(WebSoknad soknad) {
        String uid = soknad.getAktoerId();

        List<BolkService> bolkServices;
        if (soknad.erEttersending()) {
            bolkServices = Collections.singletonList(bolker.get(PersonaliaBolk.class.getName()));
        } else {
            bolkServices = WebSoknadConfig.getSoknadBolker(soknad, bolker.values());
        }

        List<Faktum> systemfaktum = bolkServices.stream()
                .map(bolk -> bolk.genererSystemFakta(uid, soknad.getSoknadId()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        faktaService.lagreSystemFakta(soknad, systemfaktum);
    }

    @Transactional
    public WebSoknad sendSoknad(String behandlingsId, byte[] pdf, byte[] fullSoknad) {
        WebSoknad soknad = hentSoknad(behandlingsId, MED_DATA, MED_VEDLEGG);

        logger.info("{}: Sender inn søknad med skjemanummer {}", behandlingsId, soknad.getskjemaNummer());
        String fullSoknadId = UUID.randomUUID().toString();
        storeFile(behandlingsId, pdf, soknad.getUuid());
        storeFile(behandlingsId, fullSoknad, fullSoknadId);

        List<AlternativRepresentasjon> alternativeRepresentations = getAndStoreAlternativeRepresentations(soknad);

        try {
            List<Vedlegg> vedlegg = hentVedleggOgKvittering(soknad);
            logger.info("{}: Soknad har {} vedlegg ({} kvittering) og {} alternative representasjoner. Har full Soknad = {}",
                    behandlingsId, vedlegg.size(),
                    vedlegg.stream().anyMatch(v -> SKJEMANUMMER_KVITTERING.equals(v.getSkjemaNummer())) ? "inklusive" : "eksklusive",
                    alternativeRepresentations.size(), fullSoknad != null);

            List<FileData> vedleggMetaData = filestorage.getFileMetadata(behandlingsId, vedlegg.stream().map(Vedlegg::getFillagerReferanse).collect(Collectors.toList()));
            List<Vedlegg> filtrertVedlegg = vedlegg.stream().filter(v-> harOpplastetFil(behandlingsId, v, vedleggMetaData)).collect(Collectors.toList());

            innsendingService.sendSoknad(soknad, alternativeRepresentations, filtrertVedlegg, pdf, fullSoknad, fullSoknadId);

            DateTime now = DateTime.now();
            soknad.setSistLagret(now);
            soknad.setInnsendtDato(now);
            soknad.medStatus(FERDIG);
            lokalDb.oppdaterSoknadEtterInnsending(soknad);
        } catch (Exception e) {
            logger.error("{}: Error when sending Soknad for archiving!", behandlingsId, e);
            throw e;
        }

        soknadMetricsService.sendtSoknad(soknad.getskjemaNummer(), soknad.erEttersending());
        return soknad;
    }

    private boolean harOpplastetFil(String behandlingsId, Vedlegg vedlegg, List<FileData> vedleggMetaData) {
        boolean funnet =  vedleggMetaData.stream().anyMatch(v-> v.getId().equals(vedlegg.getFillagerReferanse()) && Objects.equals(v.getStatus(), "ok"));
        if (!funnet) {
            logger.warn("{}: vedlegg {} ikke lastet opp til soknadsfillager", behandlingsId, vedlegg.getFillagerReferanse());
        }
        return funnet;
    }

    private List<Vedlegg> hentVedleggOgKvittering(WebSoknad soknad) {
        ArrayList<Vedlegg> vedleggForventninger = new ArrayList<>(soknad.hentOpplastedeVedlegg());
        String aapUtlandSkjemanummer = new AAPUtlandetInformasjon().getSkjemanummer().get(0);

        if (!aapUtlandSkjemanummer.equals(soknad.getskjemaNummer())) {
            Vedlegg kvittering = vedleggRepository
                    .hentVedleggForskjemaNummer(soknad.getSoknadId(), null, SKJEMANUMMER_KVITTERING);

            if (kvittering != null) {
                vedleggForventninger.add(kvittering);
            }
        }
        return vedleggForventninger;
    }

    private void storeVedleggThatAreNotInFilestorage(WebSoknad soknad) {
        try {
            String behandlingsId = soknad.getBrukerBehandlingId();

            Map<String, Vedlegg> allVedlegg = soknad.getVedlegg().stream()
                    .filter(v -> v.getInnsendingsvalg().er(Vedlegg.Status.LastetOpp))
                    .filter(v -> v.getStorrelse() != null && v.getStorrelse() > 0 && v.getData() != null)
                    .collect(Collectors.toMap(Vedlegg::getFillagerReferanse, p -> p));
            var allVedleggReferences = List.copyOf(allVedlegg.keySet());

            logger.info("{}: storeVedleggThatAreNotInFilestorage Vedlegg before querying getFileMetadata: {}. Querying for the status of {} vedlegg. allVedlegg.size(): {}",
                    behandlingsId,
                    soknad.getVedlegg().stream()
                            .map(v -> "{" + v.getSkjemaNummer() + ", " + v.getNavn() + ", " + v.getFillagerReferanse() + ", " + v.getInnsendingsvalg() + ", " + v.getStorrelse() + "}")
                            .collect(Collectors.joining(", ")),
                    allVedleggReferences.size(),
                    allVedlegg.size()
            );


            var filesNotFound = filestorage.getFileMetadata(behandlingsId, allVedleggReferences).stream()
                    .peek(v -> logger.info("{}: Response from getFileMetadata: Id: {}, Status: {}", behandlingsId, v.getId(), v.getStatus()))
                    .filter(v -> "not-found".equals(v.getStatus()))
                    .collect(Collectors.toList());

            if (filesNotFound.size() > 0) {
                var toUpload = filesNotFound.stream()
                        .map(fileData -> allVedlegg.get(fileData.getId()))
                        .map(v -> new FilElementDto(v.getFillagerReferanse(), v.getData(), OffsetDateTime.now()))
                        .collect(Collectors.toList());

                String ids = toUpload.stream().map(FilElementDto::getId).collect(Collectors.joining(", "));
                logger.info("{}: These vedlegg are missing from Filestorage and will be uploaded: {}", behandlingsId, ids);
                filestorage.store(behandlingsId, toUpload);
            }
        } catch (Exception e) {
            logger.error("{}: storeVedleggThatAreNotInFilestorage Error when checking/storing files to Soknadsfillager", soknad.getBrukerBehandlingId(), e);
        }
    }

    private void storeFile(String behandlingsId, byte[] content, String fileId) {
        if (content != null) {
            long startTime = System.currentTimeMillis();
            try {
                filestorage.store(behandlingsId, List.of(new FilElementDto(fileId, content, OffsetDateTime.now())));
            } catch (Exception e) {
                logger.error("{}: Error when sending file to filestorage! Id: {}", behandlingsId, fileId, e);
                throw e;
            }
            logger.info("{}: Sending to Soknadsfillager took {}ms.", behandlingsId, System.currentTimeMillis() - startTime);
        }
    }

    public void deleteFiles(String behandlingsId, List<String> fileids) {
        long startTime = System.currentTimeMillis();
        try {
            filestorage.delete(behandlingsId, fileids);
        } catch (Exception e) {
            logger.error("{}: Error when deleting files in filestorage! Ids: {}", behandlingsId, String.join(",", fileids), e);
            throw e;
        }
        logger.info("{}: Sending to Soknadsfillager took {}ms.", behandlingsId, System.currentTimeMillis() - startTime);
    }

    private List<AlternativRepresentasjon> getAndStoreAlternativeRepresentations(WebSoknad soknad) {
        if (!soknad.erEttersending()) {
            var altReps = alternativRepresentasjonService.hentAlternativeRepresentasjoner(soknad);
            for (AlternativRepresentasjon r : altReps) {
                storeFile(soknad.getBrukerBehandlingId(), r.getContent(), r.getUuid());
            }
            return altReps;
        }
        return Collections.emptyList();
    }

    public Long hentOpprinneligInnsendtDato(String behandlingsId) {
        WebSoknad webSoknad = lokalDb.hentOpprinneligInnsendtSoknad(behandlingsId);
        if (webSoknad != null) {
            return webSoknad.getInnsendtDato() != null ?
                webSoknad.getInnsendtDato().getMillis() : null;
        } else {
            return null;
        }
    }

    public String hentSisteInnsendteBehandlingsId(String behandlingsId) {
        WebSoknad webSoknad = lokalDb.hentNyesteSoknadGittBehandlingskjedeId(behandlingsId);
        if (webSoknad != null) {
            return webSoknad.getBrukerBehandlingId();
        } else {
            return null;
        }
    }
}
