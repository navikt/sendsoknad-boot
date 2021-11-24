package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tilleggsstonader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;

import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType.JSON;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
public class TiltakspengerTilJson implements AlternativRepresentasjonTransformer {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return JSON;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        try {
            String json = mapper.writer().writeValueAsString(webSoknad);
            log.info("JSON: {}", json);
            return new AlternativRepresentasjon()
                    .medRepresentasjonsType(JSON)
                    .medMimetype(APPLICATION_JSON_VALUE)
                    .medFilnavn("Tiltakspenger.json")
                    .medUuid(UUID.randomUUID().toString())
                    .medContent(json.getBytes(UTF_8));
        } catch (JsonProcessingException e) {
            log.error("Failed to generate JSON", e);
            throw new RuntimeException("Failed to generate JSON", e); // Check if we can return null or empty instead.
        }
    }
}
