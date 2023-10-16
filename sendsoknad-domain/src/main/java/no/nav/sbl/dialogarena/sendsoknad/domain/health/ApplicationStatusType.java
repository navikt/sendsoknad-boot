package no.nav.sbl.dialogarena.sendsoknad.domain.health;

public enum ApplicationStatusType {

    OK("OK"),
    ISSUE("ISSUE"),
    DOWN("DOWN");

    private final String value;

    ApplicationStatusType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
