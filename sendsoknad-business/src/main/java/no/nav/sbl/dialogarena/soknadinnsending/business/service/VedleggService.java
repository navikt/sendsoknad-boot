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
import no.nav.sbl.pdfutility.PdfUtilities;
import no.nav.sbl.soknadinnsending.fillager.Filestorage;
import no.nav.sbl.soknadinnsending.fillager.dto.FilElementDto;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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
    private final FaktaService faktaService;
    private final TekstHenter tekstHenter;
    private final Filestorage filestorage;

    private static final long EXPIRATION_PERIOD = 120;
    private static Cache<String, Object> vedleggPng;


    @Autowired
    public VedleggService(
            @Qualifier("soknadInnsendingRepository") SoknadRepository repository,
            @Qualifier("vedleggRepository") VedleggRepository vedleggRepository,
            SoknadService soknadService,
            SoknadDataFletter soknadDataFletter,
            FaktaService faktaService,
            TekstHenter tekstHenter,
            Filestorage filestorage
    ) {
        this.repository = repository;
        this.vedleggRepository = vedleggRepository;
        this.soknadService = soknadService;
        this.soknadDataFletter = soknadDataFletter;
        this.faktaService = faktaService;
        this.tekstHenter = tekstHenter;
        this.filestorage = filestorage;
    }

    private Cache<String, Object> getCache(String behandlingsId) {
        if (vedleggPng == null) {
            vedleggPng = new Cache2kBuilder<String, Object>() {}
                    .eternal(false)
                    .entryCapacity(100)
                    .disableStatistics(true)
                    .expireAfterWrite(EXPIRATION_PERIOD, TimeUnit.SECONDS)
                    .keepDataAfterExpired(false).permitNullValues(false).storeByReference(true)
                    .loader(key -> {
                        String[] split = key.split("-", 2);
                        long id = Long.parseLong(split[0]);
                        int sideNr = Integer.parseInt(split[1]);

                        byte[] pdf = vedleggRepository.hentVedleggData(id);
                        if (pdf == null || pdf.length == 0) {
                            logger.warn("{}: Via cache, PDF med id {} ikke funnet, oppslag for side {} feilet", behandlingsId, id, sideNr);
                            throw new OpplastingException("Kunne ikke lage forhåndsvisning, fant ikke fil", null,
                                    "vedlegg.opplasting.feil.generell");
                        }
                        try {
                            return PdfUtilities.konverterTilPng(behandlingsId, pdf, sideNr);
                        } catch (Exception e) {
                            throw new OpplastingException("Kunne ikke lage forhåndsvisning av opplastet fil", e,
                                    "vedlegg.opplasting.feil.generell");
                        }
                    })
                    .build();
        }
        return vedleggPng;
    }


    @Transactional
    public long lagreVedlegg(Vedlegg vedlegg, byte[] data, String behandlingsId) {
        logger.info("{}: lagreVedlegg for SoknadId={} filstørrelse={} vedlegg={}",
                behandlingsId, vedlegg.getSoknadId(), data != null ? data.length : "null", vedlegg.getSkjemaNummer() + "-" + vedlegg.getNavn());

        if (vedlegg.getFillagerReferanse() == null || "".equals(vedlegg.getFillagerReferanse())) {
            vedlegg.setFillagerReferanse(UUID.randomUUID().toString());
        }
        long id = vedleggRepository.opprettEllerEndreVedlegg(behandlingsId, vedlegg, data);
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());
        sendToFilestorage(behandlingsId, vedlegg.getFillagerReferanse(), data);

        return id;
    }

    private void sendToFilestorage(String behandlingsId, String id, byte[] data) {
        try {
            if (data == null || data.length == 0) {
                logger.info("{}: no file for id {} uploaded yet skip sending to soknadsfillager.", behandlingsId, id);
                return;
            }
            long startTime = System.currentTimeMillis();

            filestorage.store(behandlingsId, List.of(new FilElementDto(id, data, OffsetDateTime.now())));

            long timeTaken = System.currentTimeMillis() - startTime;
            logger.info("{}: Sending file with id {} to Soknadsfillager took {}ms.", behandlingsId, id, timeTaken);
        } catch (Exception e) {
            logger.error("{}: Failed to upload file with id {} to Soknadsfillager", behandlingsId, id, e);
            throw e;
        }
    }

    public List<Vedlegg> hentVedleggUnderBehandling(String behandlingsId, String fillagerReferanse) {
        return vedleggRepository.hentVedleggUnderBehandling(behandlingsId, fillagerReferanse);
    }

    public Vedlegg hentVedlegg(Long vedleggId) {
        return hentVedlegg(vedleggId, false);
    }

    public Vedlegg hentVedlegg(Long vedleggId, boolean medInnhold) {
        String behandlingsId = hentBehandlingsIdTilVedlegg(vedleggId);
        logger.info("{}: Henter vedlegg med id {}, {} innhold", behandlingsId, vedleggId, medInnhold ? "med" : "uten");

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
        logger.info("{}: Sletter vedlegg med id {} for soknad med id {}", soknad.getBrukerBehandlingId(), vedleggId, soknadId);

        vedleggRepository.slettVedlegg(soknadId, vedleggId);
        repository.settSistLagretTidspunkt(soknadId);
        if (!soknad.erEttersending()) {
            repository.settDelstegstatus(soknadId, SKJEMA_VALIDERT);
        }
        //TODO slette fil lagret i soknadsfillager?
    }

    public byte[] lagForhandsvisning(Long vedleggId, int side) {
        String behandlingsId = hentBehandlingsIdTilVedlegg(vedleggId);
        try {
            logger.info("{}: Henter eller lager vedleggsside med key {} - {}", behandlingsId, vedleggId, side);
            byte[] png = (byte[]) getCache(behandlingsId).get(vedleggId + "-" + side);
            if (png == null || png.length == 0) {
                logger.warn("{}: Png av side {} for vedlegg {} ikke funnet", behandlingsId, side, vedleggId);
            }
            return png;
        } catch (Exception e) {
            logger.warn("{}: Henting av Png av side {} for vedlegg {} feilet med {}", behandlingsId, side, vedleggId, e.getMessage(), e);
            throw e;
        }
    }

    private String hentBehandlingsIdTilVedlegg(Long vedleggId) {
        try {
            return vedleggRepository.hentBehandlingsIdTilVedlegg(vedleggId);
        } catch (Exception e) {
            // TODO: Finnes ikke denne feilmeldinga i loggene så trenger vi ikke å beskydde med en try-catch
            logger.error("Klarte ikke å hente behandlingsId til vedlegg {}", vedleggId, e);
            return null;
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
        forventning.leggTilInnhold(doc, antallSiderIPDF(behandlingsId, doc, vedleggId));

        sendToFilestorage(soknad.getBrukerBehandlingId(), forventning.getFillagerReferanse(), doc);

        vedleggRepository.slettVedleggUnderBehandling(soknadId, forventning.getFaktumId(), forventning.getSkjemaNummer(), forventning.getSkjemanummerTillegg());
        logger.info("{}: genererVedleggFaktum skjemanr={} - tittel={} - skjemanummerTillegg={}", behandlingsId, forventning.getSkjemaNummer(), forventning.getNavn(), forventning.getSkjemanummerTillegg());
        vedleggRepository.lagreVedleggMedData(behandlingsId, soknadId, vedleggId, forventning, doc);
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
            oppdaterVedleggForForventninger(behandlingsId, hentForventingerForEkstraVedlegg(soknad));
            return vedleggRepository.hentVedlegg(behandlingsId).stream().filter(PAAKREVDE_VEDLEGG).collect(Collectors.toList());

        } else {
            SoknadStruktur struktur = soknadService.hentSoknadStruktur(soknad.getskjemaNummer());
            List<VedleggsGrunnlag> alleMuligeVedlegg = struktur.hentAlleMuligeVedlegg(soknad, tekstHenter);
            oppdaterVedleggForForventninger(behandlingsId, alleMuligeVedlegg);
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

    private void oppdaterVedleggForForventninger(String behandlingsId, List<VedleggsGrunnlag> forventninger) {
        forventninger
                .stream().filter(vedleggsgrunnlag -> vedleggsgrunnlag.vedleggFinnes() || vedleggsgrunnlag.erVedleggPaakrevd())
                .forEach(vedleggsGrunnlag -> oppdaterVedlegg(behandlingsId, vedleggsGrunnlag));
    }

    private void oppdaterVedlegg(String behandlingsId, VedleggsGrunnlag vedleggsgrunnlag) {

        if (vedleggsgrunnlag.vedleggIkkeFinnes()) {
            vedleggsgrunnlag.opprettVedleggFraFaktum();
        }

        Vedlegg.Status orginalStatus = vedleggsgrunnlag.vedlegg.getInnsendingsvalg();
        Vedlegg.Status status = vedleggsgrunnlag.oppdaterInnsendingsvalg(vedleggsgrunnlag.erVedleggPaakrevd());
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
                logger.info("{}: oppdaterVedlegg: skjemanr:{}, tittel={}, navn={}, SkjemanummerTillegg={}",
                        behandlingsId, vedleggsgrunnlag.vedlegg.getSkjemaNummer(), vedleggsgrunnlag.vedlegg.getTittel(),
                        vedleggsgrunnlag.vedlegg.getNavn(), vedleggsgrunnlag.vedlegg.getSkjemanummerTillegg());

                vedleggRepository.opprettEllerLagreVedleggVedNyGenereringUtenEndringAvData(behandlingsId, vedleggsgrunnlag.vedlegg);
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
                .collect(Collectors.toList());
    }

    @Transactional
    public void lagreVedlegg(Vedlegg vedlegg) {
        if (nedgradertEllerForLavtInnsendingsValg(vedlegg)) {
            throw new SendSoknadException("Ugyldig innsendingsstatus, opprinnelig innsendingstatus kan aldri nedgraderes");
        }
        WebSoknad soknad = soknadService.hentSoknadFraLokalDb(vedlegg.getSoknadId());

        logger.info("{}: lagreVedlegg: soknadId={} - skjemanr={} - tittel={}",
                soknad.getBrukerBehandlingId(), vedlegg.getSoknadId(), vedlegg.getSkjemaNummer(), vedlegg.getNavn());
        vedleggRepository.lagreVedlegg(soknad.getBrukerBehandlingId(), vedlegg.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
        repository.settSistLagretTidspunkt(vedlegg.getSoknadId());

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
            oppdaterInnholdIKvittering(behandlingsId, kvitteringVedlegg, kvittering);
            vedleggRepository.opprettEllerEndreVedlegg(behandlingsId, kvitteringVedlegg, kvittering);
        } else {
            oppdaterInnholdIKvittering(behandlingsId, kvitteringVedlegg, kvittering);
            vedleggRepository.lagreVedleggMedData(behandlingsId, soknad.getSoknadId(), kvitteringVedlegg.getVedleggId(), kvitteringVedlegg, kvittering);
        }
        logger.debug("{}: lagreKvitteringSomVedlegg: vedleggId={} skjemanr={} navn={}",
                behandlingsId, kvitteringVedlegg.getVedleggId(), kvitteringVedlegg.getSkjemaNummer(), kvitteringVedlegg.getNavn());

        sendToFilestorage(behandlingsId, kvitteringVedlegg.getFillagerReferanse(), kvittering);
    }

    private void oppdaterInnholdIKvittering(String behandlingsId, Vedlegg vedlegg, byte[] data) {
        vedlegg.medData(data);
        vedlegg.medStorrelse((long) data.length);
        vedlegg.medNavn(TilleggsInfoService.lesTittelFraJsonString(vedlegg.getNavn()));
        vedlegg.medAntallSider(antallSiderIPDF(behandlingsId, data, vedlegg.getVedleggId()));

        if (vedlegg.getNavn() == null || vedlegg.getNavn().isEmpty()) {
            if ("L7".equals(vedlegg.getSkjemaNummer())) {
                String navn = "Kvittering";
                vedlegg.medNavn(navn);
                logger.info("{}: Navn på Kvittering ikke satt - setter vedleggsnavn til {}", behandlingsId, navn);
            } else {
                logger.warn("{}: Kvittering sitt navn er ikke satt for vedlegg med skjemanummer {}",
                        behandlingsId, vedlegg.getSkjemaNummer());
            }
        }
    }

    private int antallSiderIPDF(String behandlingsId, byte[] bytes, Long vedleggId) {
        try {
            return PdfUtilities.finnAntallSider(bytes);
        } catch (Exception e) {
            logger.warn("{}: Klarte ikke å finne antall sider i kvittering, vedleggid={}. Setter sideantall til 1.",
                    behandlingsId, vedleggId, e);
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
