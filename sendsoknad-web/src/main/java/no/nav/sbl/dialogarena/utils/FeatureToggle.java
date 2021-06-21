package no.nav.sbl.dialogarena.utils;

public class FeatureToggle {

    private String key;
    private Boolean active;
    private String name;
    private String description;

    public FeatureToggle(String key, Boolean active, String name, String description) {
        this.key = key;
        this.active = active;
        this.name = name;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public Boolean getActive() {
        return active;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
