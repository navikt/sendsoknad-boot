package no.nav.sbl.dialogarena.sendsoknad.domain.oppsett;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.List;

@XmlType(propOrder = {})
public class PropertyStruktur implements Serializable, StrukturConfigurable {
    private String id;
    private String type;
    private String dependOn;
    private List<String> constraints;
    private Configuration configuration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElementWrapper(name="constraints")
    @XmlElement(name="constraint")
    public List<String> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<String> constraints) {
        this.constraints = constraints;
    }

    public String getDependOn() {
        return dependOn;
    }

    public void setDependOn(String dependOn) {
        this.dependOn = dependOn;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("type", type)
                .append("dependOn", dependOn)
                .append("constraints", constraints)
                .append("configuration", configuration)
                .toString();
    }

    public boolean hasConfig(String configKey) {
        return configuration != null && configuration.containsKey(configKey);
    }

    public boolean erSynlig(Faktum faktum) {
        if(constraints != null && !constraints.isEmpty()) {
            boolean result = false;
            for (String constraint : constraints) {
                result = result || ForventningsSjekker.sjekkForventning(constraint, faktum);
            }
            return result;
        }
        return true;
    }


}
