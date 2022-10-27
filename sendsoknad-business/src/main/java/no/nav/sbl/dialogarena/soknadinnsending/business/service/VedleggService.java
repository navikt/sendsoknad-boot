package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.TekstHenter;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggForFaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.VedleggsGrunnlag;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.TilleggsInfoService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.pdfutility.PdfUtilities;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.integration.CacheLoader;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.SKJEMA_VALIDERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.PAAKREVDE_VEDLEGG;
import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService.SKJEMANUMMER_KVITTERING;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class VedleggService {
    private static final Logger logger = getLogger(VedleggService.class);

    private final SoknadRepository repository;
    private final VedleggRepository vedleggRepository;
    private final SoknadService soknadService;
    private final SoknadDataFletter soknadDataFletter;
    private final FillagerService fillagerService;
    private final FaktaService faktaService;
    private final TekstHenter tekstHenter;
    private final Filestorage filestorage;

    private static final long EXPIRATION_PERIOD = 120;
    private static Cache<String, Object> vedleggPng;

    private final boolean sendToSoknadsfillager;


    @Autowired
    public VedleggService(
            @Qualifier("soknadInnsendingRepository") SoknadRepository repository,
            @Qualifier("vedleggRepository") VedleggRepository vedleggRepository,
            SoknadService soknadService,
            SoknadDataFletter soknadDataFletter,
            FillagerService fillagerService,
            FaktaService faktaService,
            TekstHenter tekstHenter,
            Filestorage filestorage,
            @Value("${innsending.sendToSoknadsfillager}") String sendToSoknadsfillager) {
        super();
        this.repository = repository;
        this.vedleggRepository = vedleggRepository;
        this.soknadService = soknadService;
        this.soknadDataFletter = soknadDataFletter;
        this.fillagerService = fillagerService;
        this.faktaService = faktaService;
        this.tekstHenter = tekstHenter;
        this.filestorage = filestorage;
        this.sendToSoknadsfillager = "true".equalsIgnoreCase(sendToSoknadsfillager);
        logger.info("sendToSoknadsfillager: {}", sendToSoknadsfillager);
    }

    private Cache<String, Object> getCache() {
        if (vedleggPng == null) {
            vedleggPng = new Cache2kBuilder<String, Object>() {}
                    .eternal(false)
                    .entryCapacity(100)
                    .disableStatistics(true)
                    .expireAfterWrite(EXPIRATION_PERIOD, TimeUnit.SECONDS)
                    .keepDataAfterExpired(false).permitNullValues(false).storeByReference(true)
                    .loader(new CacheLoader<>() {
                        @Override
                        public Object load(final String key) {
                            String[] split = key.split("-", 2);
                            byte[] pdf = vedleggRepository.hentVedleggData(Long.parseLong(split[0]));
                            if (pdf == null || pdf.length == 0) {
                                logger.warn("Via cache, PDF med id {} ikke funnet, oppslag for side {} feilet", split[0], split[1]);
                                throw new OpplastingException("Kunne ikke lage forhåndsvisning, fant ikke fil", null,
                                        "vedlegg.opplasting.feil.generell");
                            }
                            try {
                                return PdfUtilities.konverterTilPng(pdf, Integer.parseInt(split[1]));
                            } catch (Exception e) {
                                throw new OpplastingException("Kunne ikke lage forhåndsvisning av opplastet fil", e,
                                        "vedlegg.opplasting.feil.generell");
                            }
                        }
                    })
                    .build();
        }
        return vedleggPng;
    }


    @Transactional
    public long lagreVedlegg(Vedlegg vedlegg, byte[] data, String behandlingsId) {
        logger.info("{}: lagreVedlegg for SoknadId={} filstørrelse={} vedlegg={}", behandlingsId, vedlegg.getSoknadId(), data != null ? data.length : "null", vedlegg.getSkjemaNummer()+"-"+vedlegg.getNavn());

        long id = vedleggRepository.opprettEllerEndreVedlegg(vedlegg, data);
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());
        sendToFilestorage(behandlingsId, vedlegg.getFillagerReferanse(), data);

        return id;
    }

    private void sendToFilestorage(String behandlingsId, String id, byte[] data) {
        if (sendToSoknadsfillager) {
            try {
                long startTime = System.currentTimeMillis();

                filestorage.store(behandlingsId, List.of(new FilElementDto(id, data, OffsetDateTime.now())));

                long timeTaken = System.currentTimeMillis() - startTime;
                logger.info("{}: Sending file with id {} to Soknadsfillager took {}ms.", behandlingsId, id, timeTaken);
            } catch (Throwable t) {
                logger.error("{}: Failed to upload file with id {} to Soknadsfillager", behandlingsId, id, t);
            }
        }
    }

    public List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse) {
        return vedleggRepository.hentVedleggUnderBehandling(behandlingsId, fillagerReferanse);
    }

    public Vedlegg hentVedlegg(Long vedleggId) {
        return hentVedlegg(vedleggId, false);
    }

    public Vedlegg hentVedlegg(Long vedleggId, boolean medInnhold) {
        Vedlegg vedlegg;

        if (medInnhold) {
            vedlegg = vedleggRepository.hentVedleggMedInnhold(vedleggId);
        } else {
            vedlegg = vedleggRepository.hentVedlegg(vedleggId);
        }

        medKodeverk(vedlegg);
        return vedlegg;
    }

    public String hentBehandlingsId(Long vedleggId) {
        return vedleggRepository.hentBehandlingsIdTilVedlegg(vedleggId);
    }

    @Transactional
    public void slettVedlegg(Long vedleggId) {
        Vedlegg vedlegg = hentVedlegg(vedleggId, false);
        WebSoknad soknad = soknadService.hentSoknadFraLokalDb(vedlegg.getSoknadId());
        Long soknadId = soknad.getSoknadId();

        vedleggRepository.slettVedlegg(soknadId, vedleggId);
        repository.settSistLagretTidspunkt(soknadId);
        if (!soknad.erEttersending()) {
            repository.settDelstegstatus(soknadId, SKJEMA_VALIDERT);
        }
        //TODO slette fil lagret i soknadsfillager?
    }

    public byte[] lagForhandsvisning(Long vedleggId, int side) {
        try {
            logger.info("Henter eller lager vedleggsside med key {} - {}", vedleggId, side);
            byte[] png = (byte[]) getCache().get(vedleggId + "-" + side);
            if (png == null || png.length == 0) {
                logger.warn("Png av side {} for vedlegg {} ikke funnet", side, vedleggId);
            }
            return png;
        } catch (Exception e) {
            logger.warn("Henting av Png av side {} for vedlegg {} feilet med {}", side, vedleggId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void genererVedleggFaktum(String behandlingsId, Long vedleggId) {
        Vedlegg forventning = vedleggRepository.hentVedlegg(vedleggId);
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        List<Vedlegg> vedleggUnderBehandling = vedleggRepository.hentVedleggUnderBehandling(behandlingsId, forventning.getFillagerReferanse());
        Long soknadId = soknad.getSoknadId();

        vedleggUnderBehandling.sort(Comparator.comparing(Vedlegg::getVedleggId));

        List<byte[]> filer = hentLagretVedlegg(vedleggUnderBehandling);
        byte[] doc = filer.size() == 1 ? filer.get(0) : PdfUtilities.mergePdfer(filer);
        forventning.leggTilInnhold(doc, antallSiderIPDF(doc, vedleggId));

        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            logger.info("{}: Lagrer fil til henvendelse. UUID={}, veldeggsstørrelse={}", soknad.getBrukerBehandlingId(), forventning.getFillagerReferanse(), doc.length);
            fillagerService.lagreFil(soknad.getBrukerBehandlingId(), forventning.getFillagerReferanse(), soknad.getAktoerId(), new ByteArrayInputStream(doc));
        }
        sendToFilestorage(soknad.getBrukerBehandlingId(), forventning.getFillagerReferanse(), doc);

        vedleggRepository.slettVedleggUnderBehandling(soknadId, forventning.getFaktumId(), forventning.getSkjemaNummer(), forventning.getSkjemanummerTillegg());
        logger.info("{}: genererVedleggFaktum skjemanr={} - tittel={} - skjemanummerTillegg={}", behandlingsId, forventning.getSkjemaNummer(), forventning.getNavn(), forventning.getSkjemanummerTillegg());
        vedleggRepository.lagreVedleggMedData(soknadId, vedleggId, forventning, doc);
    }

    private List<byte[]> hentLagretVedlegg(List<Vedlegg> vedleggUnderBehandling) {
        return vedleggUnderBehandling.stream()
                .map(v -> vedleggRepository.hentVedleggData(v.getVedleggId()))
                .collect(Collectors.toList());
    }

    public List<Vedlegg> hentPaakrevdeVedlegg(final Long faktumId) {
        List<Vedlegg> paakrevdeVedlegg = genererPaakrevdeVedlegg(faktaService.hentBehandlingsId(faktumId));
        leggTilKodeverkFelter(paakrevdeVedlegg);
        return paakrevdeVedlegg.stream()
                .filter(vedlegg -> faktumId.equals(vedlegg.getFaktumId()))
                .collect(Collectors.toList());
    }

    public List<Vedlegg> hentPaakrevdeVedlegg(String behandlingsId) {
        List<Vedlegg> paakrevdeVedleggVedNyUthenting = genererPaakrevdeVedlegg(behandlingsId);
        leggTilKodeverkFelter(paakrevdeVedleggVedNyUthenting);

        return paakrevdeVedleggVedNyUthenting;
    }

    private static final VedleggForFaktumStruktur N6_FORVENTNING = new VedleggForFaktumStruktur()
            .medFaktum(new FaktumStruktur().medId("ekstraVedlegg"))
            .medSkjemanummer("N6")
            .medOnValues(Collections.singletonList("true"))
            .medFlereTillatt();

    public List<Vedlegg> genererPaakrevdeVedlegg(String behandlingsId) {
        WebSoknad soknad = soknadDataFletter.hentSoknad(behandlingsId, true, true);
        if (soknad.erEttersending()) {
            oppdaterVedleggForForventninger(hentForventingerForEkstraVedlegg(soknad));
            return vedleggRepository.hentVedlegg(behandlingsId).stream().filter(PAAKREVDE_VEDLEGG).collect(Collectors.toList());

        } else {
            SoknadStruktur struktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer());
            List<VedleggsGrunnlag> alleMuligeVedlegg = struktur.hentAlleMuligeVedlegg(soknad, tekstHenter);
            oppdaterVedleggForForventninger(alleMuligeVedlegg);
            return hentPaakrevdeVedleggForForventninger(alleMuligeVedlegg);
        }
    }

    private List<VedleggsGrunnlag> hentForventingerForEkstraVedlegg(final WebSoknad soknad) {
        return soknad.getFaktaMedKey("ekstraVedlegg").stream()
                .map(faktum -> {
                            Vedlegg vedlegg = soknad.finnVedleggSomMatcherForventning(N6_FORVENTNING, faktum.getFaktumId());
                            return new VedleggsGrunnlag(soknad, vedlegg, tekstHenter).medGrunnlag(N6_FORVENTNING, faktum);
                        }
                ).collect(Collectors.toList());
    }

    private void oppdaterVedleggForForventninger(List<VedleggsGrunnlag> forventninger) {
        forventninger.forEach(this::oppdaterVedlegg);
    }

    private void oppdaterVedlegg(VedleggsGrunnlag vedleggsgrunnlag) {
        boolean vedleggErPaakrevd = vedleggsgrunnlag.erVedleggPaakrevd();

        if (vedleggsgrunnlag.vedleggFinnes() || vedleggErPaakrevd) {

            if (vedleggsgrunnlag.vedleggIkkeFinnes()) {
                vedleggsgrunnlag.opprettVedleggFraFaktum();
            }

            Vedlegg.Status orginalStatus = vedleggsgrunnlag.vedlegg.getInnsendingsvalg();
            Vedlegg.Status status = vedleggsgrunnlag.oppdaterInnsendingsvalg(vedleggErPaakrevd);
            VedleggForFaktumStruktur vedleggForFaktumStruktur = vedleggsgrunnlag.grunnlag.get(0).getLeft();
            List<Faktum> fakta = vedleggsgrunnlag.grunnlag.get(0).getRight();
            if (!fakta.isEmpty()) {
                Faktum faktum = fakta.size() > 1 ? getFaktumBasertPaProperties(fakta, vedleggsgrunnlag.grunnlag.get(0).getLeft()) : fakta.get(0);

                if (vedleggsgrunnlag.vedleggHarTittelFraVedleggTittelProperty(vedleggForFaktumStruktur)) {
                    String cmsnokkel = vedleggForFaktumStruktur.getVedleggTittel();
                    vedleggsgrunnlag.vedlegg.setNavn(vedleggsgrunnlag.tekstHenter.finnTekst(cmsnokkel, new Object[0], vedleggsgrunnlag.soknad.getSprak()));
                } else if (vedleggsgrunnlag.vedleggHarTittelFraProperty(vedleggForFaktumStruktur, faktum)) {
                    vedleggsgrunnlag.vedlegg.setNavn(faktum.getProperties().get(vedleggForFaktumStruktur.getProperty()));
                } else if (vedleggForFaktumStruktur.harOversetting()) {
                    String cmsnokkel = vedleggForFaktumStruktur.getOversetting().replace("${key}", faktum.getKey());
                    vedleggsgrunnlag.vedlegg.setNavn(vedleggsgrunnlag.tekstHenter.finnTekst(cmsnokkel, new Object[0], vedleggsgrunnlag.soknad.getSprak()));
                }

                if (!status.equals(orginalStatus) || vedleggsgrunnlag.vedlegg.erNyttVedlegg()) {
                    vedleggRepository.opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(vedleggsgrunnlag.vedlegg);
                }
            }
        }
    }

    private Faktum getFaktumBasertPaProperties(List<Faktum> fakta, final VedleggForFaktumStruktur vedleggFaktumStruktur) {
        return fakta.stream()
                .filter(faktum -> vedleggFaktumStruktur.getOnProperty().equals(faktum.getProperties().get(vedleggFaktumStruktur.getProperty())))
                .findFirst()
                .orElse(fakta.get(0));
    }

    private List<Vedlegg> hentPaakrevdeVedleggForForventninger(List<VedleggsGrunnlag> alleMuligeVedlegg) {
        return alleMuligeVedlegg.stream()
                .map(VedleggsGrunnlag::getVedlegg)
                .filter(PAAKREVDE_VEDLEGG)
                .peek(v -> logger.info("hentPaakrevdeVedleggForForventninger: skjemanr={} - tittel={}",v.getSkjemaNummer(), v.getNavn()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void lagreVedlegg(Vedlegg vedlegg) {
        if (nedgradertEllerForLavtInnsendingsValg(vedlegg)) {
            throw new SendSoknadException("Ugyldig innsendingsstatus, opprinnelig innsendingstatus kan aldri nedgraderes");
        }
        logger.info("lagreVedlegg: soknadId={} - skjemanr={} - tittel={}",vedlegg.getSoknadId(), vedlegg.getSkjemaNummer(), vedlegg.getNavn());
        vedleggRepository.lagreVedlegg(vedlegg.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());

        WebSoknad soknad = soknadService.hentSoknadFraLokalDb(vedlegg.getSoknadId());
        if (!soknad.erEttersending()) {
            repository.settDelstegstatus(vedlegg.getSoknadId(), SKJEMA_VALIDERT);
        }
        sendToFilestorage(soknad.getBrukerBehandlingId(), vedlegg.getFillagerReferanse(), vedlegg.getData());
    }

    public void leggTilKodeverkFelter(List<Vedlegg> vedleggListe) {
        for (Vedlegg vedlegg : vedleggListe) {
            medKodeverk(vedlegg);
        }
    }

    @Transactional
    public void lagreKvitteringSomVedlegg(String behandlingsId, byte[] kvittering) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        Vedlegg kvitteringVedlegg = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, SKJEMANUMMER_KVITTERING);
        if (kvitteringVedlegg == null) {
            kvitteringVedlegg = new Vedlegg(soknad.getSoknadId(), null, SKJEMANUMMER_KVITTERING, LastetOpp);
            oppdaterInnholdIKvittering(kvitteringVedlegg, kvittering);
            logger.debug("lagreKvitteringSomVedlegg: vedleggId={} skjemanr={} navn={}", kvitteringVedlegg.getVedleggId(), kvitteringVedlegg.getSkjemaNummer(), kvitteringVedlegg.getNavn());
            vedleggRepository.opprettEllerEndreVedlegg(kvitteringVedlegg, kvittering);
        } else {
            oppdaterInnholdIKvittering(kvitteringVedlegg, kvittering);
            logger.debug("lagreKvitteringSomVedlegg: vedleggId={} skjemanr={} navn={}", kvitteringVedlegg.getVedleggId(), kvitteringVedlegg.getSkjemaNummer(), kvitteringVedlegg.getNavn());
            vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), kvitteringVedlegg.getVedleggId(), kvitteringVedlegg, kvittering);
        }

        ByteArrayInputStream fil = new ByteArrayInputStream(kvittering);
        if (!SoknadDataFletter.GCP_ARKIVERING_ENABLED) {
            fillagerService.lagreFil(soknad.getBrukerBehandlingId(), kvitteringVedlegg.getFillagerReferanse(), soknad.getAktoerId(), fil);
        }
        sendToFilestorage(behandlingsId, kvitteringVedlegg.getFillagerReferanse(), kvittering);
    }

    private void oppdaterInnholdIKvittering(Vedlegg vedlegg, byte[] data) {
        vedlegg.medData(data);
        vedlegg.medStorrelse((long) data.length);
        vedlegg.medNavn(TilleggsInfoService.lesTittelFraJsonString(vedlegg.getNavn()));
        vedlegg.medAntallSider(antallSiderIPDF(data, vedlegg.getVedleggId()));

        if (vedlegg.getNavn() == null || vedlegg.getNavn().isEmpty()) {
            logger.warn("oppdaterInnholdIKvittering kvittering sitt navn er ikke satt");
            if ("L7".equals(vedlegg.getSkjemaNummer())) {
                vedlegg.medNavn("Kvittering");
                logger.info("Satt vedleggsnavn til Kvittering");
            }
        }
    }

    private int antallSiderIPDF(byte[] bytes, Long vedleggId) {
        try {
            return PdfUtilities.finnAntallSider(bytes);
        } catch (Exception e) {
            logger.warn("Klarte ikke å finne antall sider i kvittering, vedleggid={}. Fortsetter uten sideantall.", vedleggId, e);
            return 1;
        }
    }

    private boolean nedgradertEllerForLavtInnsendingsValg(Vedlegg vedlegg) {
        Vedlegg.Status nyttInnsendingsvalg = vedlegg.getInnsendingsvalg();
        Vedlegg.Status opprinneligInnsendingsvalg = vedlegg.getOpprinneligInnsendingsvalg();
        if (nyttInnsendingsvalg != null && opprinneligInnsendingsvalg != null) {
            return nyttInnsendingsvalg.getPrioritet() <= 1 || (nyttInnsendingsvalg.getPrioritet() < opprinneligInnsendingsvalg.getPrioritet());
        }
        return false;
    }

    public void medKodeverk(Vedlegg vedlegg) {
        VedleggHentOgPersistService.medKodeverk(vedlegg);
    }
}
