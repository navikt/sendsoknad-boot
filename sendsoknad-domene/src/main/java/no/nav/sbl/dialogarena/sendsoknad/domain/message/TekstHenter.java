package no.nav.sbl.dialogarena.sendsoknad.domain.message;

import org.slf4j.Logger;

import java.util.*;

import static java.util.ResourceBundle.getBundle;
import static org.slf4j.LoggerFactory.getLogger;

public class TekstHenter {
    private static final Logger logger = getLogger(TekstHenter.class);

    private static final String BOKMAAL = "nb";
    private static final String NYNORSK = "nn";
    private static final String ENGELSK = "en";

    private Map<String, Properties> teksterForNb;
    private Map<String, Properties> teksterForNn;
    private Map<String, Properties> teksterForEn;

    public TekstHenter() {
        this.teksterForNb = new HashMap<>();
        this.teksterForNn = new HashMap<>();
        this.teksterForEn = new HashMap<>();
    }

    public TekstHenter lesInnTeksterFraDiskForLocale(Locale locale) {
        Map<String, Properties> tekstMap = hentRiktigTekstMapForLocale(locale);

        for (String type : hentTyperForLocale(locale)) {
            ResourceBundle tekstBundleForType = getBundle("tekster/" + type, locale, new UTF8Control());
            tekstMap.put(type, tekstBundleTilProperties(tekstBundleForType));
        }
        return this;
    }

    public Properties getBundleFor(String type, Locale locale) {
        Map<String, Properties> tekstMap = hentRiktigTekstMapForLocale(locale);

        if (type == null || type.equals("")) {
            return hentAlleBundlerForLocale(locale, tekstMap);
        }

        Properties teksterForType = getTekster(tekstMap, type);
        Properties fellesTekster = getTekster(tekstMap, "sendsoknad");

        Properties mergetTekster = new Properties();
        mergetTekster.putAll(teksterForType);
        mergetTekster.putAll(fellesTekster);

        return mergetTekster;
    }

    public String finnTekst(String tekstNokkel, Object[] args, Locale locale) {
        String tekst = getBundleFor("", locale).getProperty(tekstNokkel);

        if (tekst == null) {
            return tekstNokkel;
        }

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                tekst = tekst.replace("{" + i + "}", (String) args[i]);
            }
        }
        return tekst;
    }

    Map<String, Properties> getTeksterForNb() {
        return teksterForNb;
    }

    Map<String, Properties> getTeksterForNn() {
        return teksterForNn;
    }

    Map<String, Properties> getTeksterForEn() {
        return teksterForEn;
    }

    private static Properties tekstBundleTilProperties(ResourceBundle tekstBundle) {
        Properties tekstProperties = new Properties();
        Collection<String> tekstBundleKeys = Collections.list(tekstBundle.getKeys());

        tekstBundleKeys.forEach(key -> tekstProperties.put(key, tekstBundle.getString(key)));
        return tekstProperties;
    }

    private Map<String, Properties> hentRiktigTekstMapForLocale(Locale locale) {
        switch(locale.getLanguage()) {
            case BOKMAAL:
                return teksterForNb;
            case NYNORSK:
                return teksterForNn;
            case ENGELSK:
                return teksterForEn;
            default:
                throw new IllegalArgumentException("Ikke støtte for språk " + locale.getLanguage());
        }
    }

    private static List<String> hentTyperForLocale(Locale locale) {
        List<String> typer = new ArrayList<>();
        switch (locale.getLanguage()) {
            case BOKMAAL:
                typer.addAll(Arrays.asList("bilstonad", "refusjondagligreise", "sendsoknad",
                        "soknad-aap-utland", "soknadaap", "soknadtilleggsstonader", "tiltakspenger"));
                break;
            case NYNORSK:
                typer.addAll(Arrays.asList("sendsoknad", "soknadaap"));
                break;
            case ENGELSK:
                typer.addAll(Collections.singletonList("sendsoknad"));
                break;
            default:
                throw new IllegalArgumentException("Ikke støtte for språk " + locale.getLanguage());
        }

        return typer;
    }

    private static Properties hentAlleBundlerForLocale(Locale locale, Map<String, Properties> tekstMap) {
        List<String> typer = hentTyperForLocale(locale);
        Properties mergetTekster = new Properties();

        typer.forEach(type -> mergetTekster.putAll(getTekster(tekstMap, type)));
        return mergetTekster;
    }

    static Properties getTekster(Map<String, Properties> tekstMap, String type) {
        Properties tekster = tekstMap.get(type);
        if (tekster == null) {
            logger.warn("Failed to get text for key '{}'", type);
            return new Properties();
        }
        return tekster;
    }
}
