package no.nav.sbl.dialogarena.kodeverk;

import no.nav.modig.common.MDCOperations;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SendSoknadException;
import no.nav.tjeneste.virksomhet.kodeverk.v2.HentKodeverkHentKodeverkKodeverkIkkeFunnet;
import no.nav.tjeneste.virksomhet.kodeverk.v2.KodeverkPortType;
import no.nav.tjeneste.virksomhet.kodeverk.v2.informasjon.*;
import no.nav.tjeneste.virksomhet.kodeverk.v2.meldinger.XMLHentKodeverkRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.Collator;
import java.util.*;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.sbl.dialogarena.kodeverk.Kodeverk.EksponertKodeverk.LANDKODE;
import static no.nav.sbl.dialogarena.kodeverk.Kodeverk.EksponertKodeverk.POSTNUMMER;
import static org.joda.time.DateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Tilbyr kodeverkoppslag. Implementasjonen laster hele kodeverk fra webservice on-demand,
 * og lagrer de i en intern struktur som brukes ved oppslag. Klassen har fallback-mekanisme ved at
 * den dumper allerede innlastet kodverk til fil(er), og dersom kall til webservice feiler
 * vil ev. eksisterende fildump brukes til å populere den interne datastrukturen.
 */
public class StandardKodeverk implements Kodeverk {

    private static final Logger logger = getLogger(StandardKodeverk.class);

    private final KodeverkPortType webservice;
    private final String spraak;
    private final Map<String, XMLEnkeltKodeverk> kodeverk;
    private final Optional<File> dumpDirectory;

    /**
     * @param locale        Hvilket locale kodeverkoppslagene skal gjøres for.
     * @param dumpDirectory Katalog hvor kodverk lastet fra tjeneste vil bli dumpet til fil(er).
     *                      Her <em>må</em> applikasjonen ha full skrivetilgang (opprette, endre, slette kataloger og filer).
     *                      Ved å angi {@link Optional#empty()} ()} vil fallback til fildump være deaktivert.
     */
    public StandardKodeverk(KodeverkPortType webservice, Locale locale, Optional<File> dumpDirectory) {
        this.webservice = webservice;
        this.spraak = locale.getLanguage();
        this.dumpDirectory = dumpDirectory;
        this.kodeverk = new HashMap<>();
        if (dumpDirectory.isPresent()) {
            logger.info("Benytter katalog {} til å ta vare på kodeverk, i tilfelle tjeneste går ned.", dumpDirectory);
        } else {
            logger.info("Kodeverk-failback er ikke aktivert.");
        }
    }

    @Override
    public String getPoststed(final String postnummer) {
        return hentFoersteTermnavnFraKodeIKodeverk(postnummer, POSTNUMMER.toString());
    }

    @Override
    public String getLand(String landkode) {
        String landFraKodeverk = hentFoersteTermnavnFraKodeIKodeverk(landkode, LANDKODE.toString());

        return formaterLand(landFraKodeverk);
    }

    private String formaterLand(String land) {
        if (land == null) {
            return null;
        }
        String formaterMedSpace = setUpperCaseBeforeRegex(land.toLowerCase(), " ");
        String formaterMedDash = setUpperCaseBeforeRegex(formaterMedSpace, "-");
        return setUpperCaseBeforeRegex(formaterMedDash, "/");
    }

    private String setUpperCaseBeforeRegex(String s, String regex) {
        String[] split = s.split(regex);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (i > 0) {
                sb.append(regex);
            }

            if (split[i].equals("og")) {
                sb.append(split[i]);
            } else {
                sb.append(split[i].substring(0, 1).toUpperCase());
                sb.append(split[i].substring(1));
            }
        }
        return sb.toString();
    }

    @PostConstruct()
    public void lastKodeverkVedOppstart() {
        try {
            lastInnNyeKodeverk();
        } catch (RuntimeException ex) {
            logger.warn("Kunne ikke hente kodeverk under oppstart av applikasjon.", ex);
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    public void lastInnNyeKodeverk() {
        logger.info("StandardKodeverk.lastInnNyeKodeverk()");

        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, MDCOperations.generateCallId());
        Map<String, XMLEnkeltKodeverk> oppdatertKodeverk = new HashMap<>();
        for (String kodeverksnavn : ALLE_KODEVERK) {
            XMLEnkeltKodeverk enkeltkodeverk = initKodeverkMedNavn(kodeverksnavn);
            oppdatertKodeverk.put(kodeverksnavn, enkeltkodeverk);
        }
        this.kodeverk.clear();
        this.kodeverk.putAll(oppdatertKodeverk);
    }

    private XMLEnkeltKodeverk initKodeverkMedNavn(String kodeverksnavn) {
        XMLEnkeltKodeverk enkeltkodeverk = hentKodeverk(kodeverksnavn);
        List<XMLKode> gyldige = getGyldigeKodeverk(enkeltkodeverk);
        enkeltkodeverk.getKode().clear();
        enkeltkodeverk.getKode().addAll(gyldige);
        return enkeltkodeverk;
    }

    private List<XMLKode> getGyldigeKodeverk(XMLEnkeltKodeverk enkeltkodeverk) {
        return enkeltkodeverk.getKode().stream()
                .filter(kode -> gyldigPeriode(kode.getGyldighetsperiode()))
                .collect(toList());
    }

    private XMLEnkeltKodeverk kodeverkMedNavn(String kodeverknavn) {
        XMLEnkeltKodeverk kodeverket = kodeverk.get(kodeverknavn);
        if (kodeverket != null) {
            return kodeverket;
        }
        kodeverk.put(kodeverknavn, initKodeverkMedNavn(kodeverknavn));
        return kodeverk.get(kodeverknavn);
    }

    public List<String> hentAlleKodenavnFraKodeverk(EksponertKodeverk kodeverknavn) {
        return kodeverkMedNavn(kodeverknavn.toString()).getKode().stream()
                .filter(kode -> harGyldigTermPeriode(kode.getTerm()))
                .map(XMLIdentifiserbarEntitet::getNavn)
                .collect(toList());
    }

    private boolean harGyldigTermPeriode(List<XMLTerm> termer) {
        if (termer == null || termer.isEmpty()) {
            return false;
        }
        return termer.stream()
                .anyMatch(term -> gyldigPeriode(term.getGyldighetsperiode()));
    }

    private String hentFoersteTermnavnFraKodeIKodeverk(String kodenavn, String kodeverknavn) {
        for (XMLKode kode : kodeverkMedNavn(kodeverknavn).getKode()) {
            if (kode.getNavn().equalsIgnoreCase(kodenavn)) {
                for (int i = 0; i < kode.getTerm().size(); i++) {
                    if (gyldigPeriode(kode.getTerm().get(i).getGyldighetsperiode())) {
                        return kode.getTerm().get(i).getNavn();
                    }
                }
            }
        }
        return null;
    }

    private boolean gyldigPeriode(List<XMLPeriode> perioder) {
        if (perioder == null || perioder.isEmpty()) {
            return false;
        }
        DateTime now = now();
        return perioder.stream()
                .anyMatch(periode -> now.isAfter(periode.getFom()) && now.isBefore(periode.getTom()));
    }

    private XMLEnkeltKodeverk hentKodeverk(String navn) {
        XMLEnkeltKodeverk kodeverket = null;
        Optional<RuntimeException> webserviceException = empty();
        try {
            kodeverket = (XMLEnkeltKodeverk) webservice.hentKodeverk(new XMLHentKodeverkRequest().withNavn(navn).withSpraak(spraak)).getKodeverk();
        } catch (HentKodeverkHentKodeverkKodeverkIkkeFunnet kodeverkIkkeFunnet) {
            throw new SendSoknadException("Kodeverk '" + navn + "' (" + spraak + "): " + kodeverkIkkeFunnet.getMessage(), kodeverkIkkeFunnet);
        } catch (RuntimeException e) {
            webserviceException = Optional.of(e);
        }

        if (webserviceException.isPresent()) {
            RuntimeException kodeverkfeil = webserviceException.get();
            if (kodeverk.containsKey(navn)) {
                logger.warn("Kodeverktjeneste feilet ({}) for {}. Benytter eksisterende kodeverk i minne.", kodeverkfeil.getMessage(), navn);
                return kodeverk.get(navn);
            }
            logger.warn("Kodeverktjeneste feilet ({})! Forsøker fallback", kodeverkfeil.getMessage());
            try {
                kodeverket = (XMLEnkeltKodeverk) readFromDump(navn);
            } catch (RuntimeException dumpException) {
                logger.warn("Fallback feilet ({}), avbryter.", dumpException.getMessage());
                kodeverkfeil.addSuppressed(dumpException);
                throw kodeverkfeil;
            }
        } else {
            dumpIfPossible(navn, kodeverket);
        }
        if (!POSTNUMMER.toString().equals(navn)) {
            sort(kodeverket.getKode(), comparing(o -> o.getTerm().get(0).getNavn(), Collator.getInstance(Locale.forLanguageTag("NO"))));
        }
        return kodeverket;
    }

    private static final JAXBContext JAXB;

    static {
        try {
            JAXB = newInstance(XMLKodeverk.class);
        } catch (JAXBException e) {
            throw new RuntimeException(createErrorMessage(e), e);
        }
    }

    private static String createErrorMessage(JAXBException e) {
        return "Unable to load class " + StandardKodeverk.class.getName() + ", error creating JAXB context for " + XMLKodeverk.class.getName() + ": " + e.getMessage();
    }

    @SuppressWarnings("unchecked")
    private XMLKodeverk readFromDump(String dumpName) {
        if (dumpDirectory.isPresent() && dumpDirectory.get().exists()) {
            File dumpFile = new File(dumpDirectory.get(), dumpName + ".xml");
            try {
                logger.info("Leser dump fra fil '{}'", dumpFile);
                return ((JAXBElement<XMLKodeverk>) JAXB.createUnmarshaller().unmarshal(dumpFile)).getValue();
            } catch (JAXBException e) {
                throw new RuntimeException("Feil ved innlasting av dump " + dumpFile + ": " + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Forsøkte å laste fildump '" + dumpName + ".xml', men fant ikke filen");
    }

    private void dumpIfPossible(String dumpName, XMLKodeverk kodeverket) {
        if (dumpDirectory.isPresent() && dumpDirectory.get().exists()) {
            File dumpFile = new File(dumpDirectory.get(), dumpName + ".xml");
            logger.info("Dumper til filen '{}'", dumpFile);
            try (Writer out = new FileWriter(dumpFile)) {
                JAXB.createMarshaller().marshal(createJAXBElement(dumpName, kodeverket), out);
            } catch (JAXBException | IOException e) {
                logger.error("Klarte ikke å dumpe '{}' til fil. {}\n{}", dumpName, e.getMessage(), e);
            }
        }
    }

    private JAXBElement<XMLKodeverk> createJAXBElement(String dumpName, XMLKodeverk kodeverket) {
        return new JAXBElement<>(createQName(dumpName), XMLKodeverk.class, kodeverket);
    }

    private QName createQName(String dumpName) {
        return new QName(StandardKodeverk.class.getName() + "." + dumpName, dumpName);
    }
}
