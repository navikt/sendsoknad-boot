package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag.SkjemaOppslagService;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public class TilleggsInfoService {
    private static final Logger logger = getLogger(TilleggsInfoService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static String createTilleggsInfoJsonString(SkjemaOppslagService skjemaOppslagService, String skjemanummer) {
        Tilleggsinfo tilleggsinfo = new Tilleggsinfo();

        tilleggsinfo.tittel = skjemaOppslagService.getTittel(skjemanummer);
        tilleggsinfo.tema = skjemaOppslagService.getTema(skjemanummer);

        try {
            return OBJECT_MAPPER.writeValueAsString(tilleggsinfo);
        } catch (JsonProcessingException e) {
            logger.error("Could not marshal Tilleggsinfo to json", e);
            return null;
        }
    }

    public static String lesTittelFraJsonString(String jsonString) {
        if (jsonString == null || "".equals(jsonString)) {
            return null;
        }
        try {
            Tilleggsinfo tilleggsinfo = OBJECT_MAPPER.readValue(jsonString, Tilleggsinfo.class);
            return tilleggsinfo.tittel;
        } catch (IOException e) {
            logger.info("Could not demarshal json to Tilleggsinfo, string = " + jsonString);
            return jsonString;
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class Tilleggsinfo {
        public String tittel;
        public String tema;
    }
}
