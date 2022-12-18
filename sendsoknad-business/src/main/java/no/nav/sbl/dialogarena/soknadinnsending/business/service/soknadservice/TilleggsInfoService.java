package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class TilleggsInfoService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static String lesTittelFraJsonString(String jsonString) {
        if (jsonString == null || "".equals(jsonString)) {
            return null;
        }
        try {
            Tilleggsinfo tilleggsinfo = OBJECT_MAPPER.readValue(jsonString, Tilleggsinfo.class);
            return tilleggsinfo.tittel;
        } catch (IOException e) {
            return jsonString;
        }
    }

    @SuppressWarnings("WeakerAccess")
    static class Tilleggsinfo {
        public String tittel;
        public String tema;
    }
}
