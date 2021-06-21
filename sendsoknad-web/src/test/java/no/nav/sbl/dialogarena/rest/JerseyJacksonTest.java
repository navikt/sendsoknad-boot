package no.nav.sbl.dialogarena.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nav.sbl.dialogarena.rest.providers.SoknadObjectMapperProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JerseyJacksonTest {

    @Test
    public void serializeJodaTime() throws JsonProcessingException {
        DateTime date = new DateTime(2015, 1, 1, 10, 0, DateTimeZone.UTC);

        SoknadObjectMapperProvider mapper = new SoknadObjectMapperProvider();
        String result = mapper.getContext(DateTime.class).writeValueAsString(date);

        assertTrue(result.contains("2015-01-01T10:00:00.000Z"));
    }
}
