package no.nav.sbl.dialogarena.sendsoknad.domain.health;

public class ApplicationStatus {

    private ApplicationStatusType status;
    private String description;
    private String logLink;


    public ApplicationStatus(ApplicationStatusType status, String description, String logLink) {
        this.status = status;
        this.description = description;
        this.logLink = logLink;
    }

    public ApplicationStatusType getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatusType status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogLink() {
        return logLink;
    }

    public void setLogLink(String logLink) {
        this.logLink = logLink;
    }
}
