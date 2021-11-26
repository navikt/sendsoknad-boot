package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.FaktumEgenskap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class JsonTiltakspengerFaktum {
    private Long faktumId;
    private Long soknadId;
    private Long parrentFaktum;
    private String key;
    private String value;
    private Set<JsonFaktumEgenskap> faktumEgenskaper;
    private Map<String, String> properties = new HashMap<>();
    private Faktum.FaktumType type;

    public JsonTiltakspengerFaktum medFaktumId(Long faktumId) {
        this.faktumId = faktumId;
        return this;
    }

    public JsonTiltakspengerFaktum medSoknadId(Long soknadId) {
        this.soknadId = soknadId;
        return this;
    }

    JsonTiltakspengerFaktum medParentFaktum(Long parrentFaktum) {
        this.parrentFaktum = parrentFaktum;
        return this;
    }

    JsonTiltakspengerFaktum medFaktumKey(String faktumKey) {
        this.key = faktumKey;
        return this;
    }

    JsonTiltakspengerFaktum medFaktumValue(String faktumValue) {
        this.value = faktumValue;
        return this;
    }

    JsonTiltakspengerFaktum medFaktumProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    JsonTiltakspengerFaktum medFaktumEgenskaper(Set<FaktumEgenskap> faktumEgenskaper) {
        this.faktumEgenskaper = faktumEgenskaper.stream().map(faktumEgenskap -> new JsonFaktumEgenskap()
                        .medFaktumId(faktumEgenskap.getFaktumId())
                        .medSoknadId(faktumEgenskap.getSoknadId())
                        .medKey(faktumEgenskap.getKey())
                        .medValue(faktumEgenskap.getValue())
                        .medSystemEgenskap(faktumEgenskap.getSystemEgenskap()))
                .collect(toSet());

        return this;
    }


    JsonTiltakspengerFaktum medFaktumType(Faktum.FaktumType faktumType) {
        this.type = faktumType;
        return this;
    }

    public Long getFaktumId() {
        return faktumId;
    }

    public Long getSoknadId() {
        return soknadId;
    }

    public Long getParrentFaktum() {
        return parrentFaktum;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Set<JsonFaktumEgenskap> getFaktumEgenskaper() {
        return faktumEgenskaper;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Faktum.FaktumType getType() {
        return type;
    }
}
