package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.tiltakspenger;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;

import java.util.HashMap;
import java.util.Map;

public class JsonTiltakspengerFaktum {
    private Long faktumId;
    private Long soknadId;
    private String key;
    private String value;
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

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Faktum.FaktumType getType() {
        return type;
    }
}
