package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

public class TiltakspengerTilJson implements AlternativRepresentasjonTransformer {
    static final String FILNAVN = "tiltakspenger.json";
    private static final Logger LOG = LoggerFactory.getLogger(TiltakspengerTilJson.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.JSON;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        try {
            JsonTiltakspengerSoknad jsonSoknad = JsonTiltakspengerSoknadConverter.tilJsonSoknad(webSoknad);
            mapper.registerModule(new JodaModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String json = mapper.writeValueAsString(jsonSoknad);

            LOG.info("JSON: {}", json);

            return new AlternativRepresentasjon()
                    .medRepresentasjonsType(getRepresentasjonsType())
                    .medMimetype(APPLICATION_JSON_VALUE)
                    .medFilnavn(FILNAVN)
                    .medUuid(UUID.randomUUID().toString())
                    .medContent(json.getBytes(UTF_8));
        } catch (JsonProcessingException e) {
            LOG.error("Failed to generate JSON for {}", webSoknad.getBrukerBehandlingId(), e);
            throw new RuntimeException("Failed to generate JSON", e);
        }
    }
}
