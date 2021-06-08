package no.nav.sbl.dialogarena.soknadinnsending.consumer.skjemaoppslag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SkjemaOgVedleggsdata {
    @JsonProperty("Skjemanummer")
    private String skjemanummer;
    @JsonProperty("Vedleggsid")
    private String vedleggsid;
    @JsonProperty("Tittel")
    private String tittel;
    @JsonProperty("Tema")
    private String tema;
    @JsonProperty("Lenke")
    private String url;

    public String getSkjemanummer() {
        return skjemanummer;
    }

    public String getVedleggsid() {
        return vedleggsid;
    }

    public String getTittel() {
        return tittel;
    }

    public String getTema() {
        return tema;
    }

    public String getUrl() {
        return url;
    }

    public void setSkjemanummer(String skjemanummer) {
        this.skjemanummer = skjemanummer;
    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "skjemanummer='" + skjemanummer + '\'' +
                ", vedleggsid='" + vedleggsid + '\'' +
                ", tittel='" + tittel + '\'';
    }
}
