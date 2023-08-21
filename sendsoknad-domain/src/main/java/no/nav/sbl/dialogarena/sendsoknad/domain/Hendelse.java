package no.nav.sbl.dialogarena.sendsoknad.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Hendelse implements Serializable {

private String behandlingsid;
private HendelseType hendelseType;
private LocalDateTime hendelseTidspunkt;
private String versjon;
private String skjemanr;
private Boolean sisteHendelse;

    public String getBehandlingsid() {
        return behandlingsid;
    }

    public void setBehandlingsid(String behandlingsid) {
        this.behandlingsid = behandlingsid;
    }

    public HendelseType getHendelseType() {
        return hendelseType;
    }

    public void setHendelseType(HendelseType hendelseType) {
        this.hendelseType = hendelseType;
    }

    public LocalDateTime getHendelseTidspunkt() {
        return hendelseTidspunkt;
    }

    public void setHendelseTidspunkt(LocalDateTime hendelseTidspunkt) {
        this.hendelseTidspunkt = hendelseTidspunkt;
    }

    public String getVersjon() {
        return versjon;
    }

    public void setVersjon(String versjon) {
        this.versjon = versjon;
    }

    public String getSkjemanr() {
        return skjemanr;
    }

    public void setSkjemanr(String skjemanr) {
        this.skjemanr = skjemanr;
    }

    public Boolean getSisteHendelse() {
        return sisteHendelse;
    }

    public void setSisteHendelse(Boolean sisteHendelse) {
        this.sisteHendelse = sisteHendelse;
    }

    public Hendelse medBehandlingsid(String behandlingsid) {
        this.setBehandlingsid(behandlingsid);
        return this;
    }

    public Hendelse medHendelseType(HendelseType hendelseType) {
        this.setHendelseType(hendelseType);
        return this;
    }

    public Hendelse medHendelseTidspunkt(LocalDateTime hendelseTidspunkt) {
        this.setHendelseTidspunkt(hendelseTidspunkt);
        return this;
    }

    public Hendelse medHendelseTidspunkt(Timestamp hendelseTidspunkt) {
        this.setHendelseTidspunkt(hendelseTidspunkt.toLocalDateTime());
        return this;
    }

    public Hendelse medVersjon(String versjon) {
        this.setVersjon(versjon);
        return this;
    }

    public Hendelse medSkjemanr(String skjemanr) {
        this.setSkjemanr(skjemanr);
        return this;
    }

    public Hendelse medSisteHendelse(Boolean sisteHendelse) {
        this.setSisteHendelse(sisteHendelse);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("behandlingsid", behandlingsid)
                .append("hendelseType", hendelseType)
                .append("hendelseTidspunkt", hendelseTidspunkt)
                .append("versjon", versjon)
                .append("skjemanr", skjemanr)
                .append("sisteHendelse", sisteHendelse)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Hendelse rhs = (Hendelse) obj;
        return new EqualsBuilder()
                .append(this.behandlingsid, rhs.behandlingsid)
                .append(this.hendelseType, rhs.hendelseType)
                .append(this.hendelseTidspunkt, rhs.hendelseTidspunkt)
                .append(this.versjon, rhs.versjon)
                .append(this.skjemanr, rhs.skjemanr)
                .append(this.sisteHendelse, rhs.sisteHendelse)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(behandlingsid)
                .append(hendelseType)
                .append(hendelseTidspunkt)
                .append(versjon)
                .append(skjemanr)
                .append(sisteHendelse)
                .toHashCode();
    }



}
