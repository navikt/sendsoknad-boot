package no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Skjemaer {
    @JsonProperty("Skjemaer")
    private List<SkjemaOgVedleggsdata> skjemaer;

    public List<SkjemaOgVedleggsdata> getSkjemaer() {
        return skjemaer;
    }
}
