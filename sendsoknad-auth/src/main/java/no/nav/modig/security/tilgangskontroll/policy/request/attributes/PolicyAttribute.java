package no.nav.modig.security.tilgangskontroll.policy.request.attributes;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.AttributeValue;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

/**
 * THIS CLASS IS NOT PART OF THE PUBLIC API, DO NOT INSTANTIATE IT DIRECTLY
 * <p/>
 * Base class for all policy attributes, used internally only.
 * <p/>
 * To create a policy attribute use {@link SubjectAttribute}, {@link ResourceAttribute} or {@link ActionAttribute}.
 */
public abstract class PolicyAttribute implements Serializable {

    private final URN attributeId;
    private final AttributeValue<?> attributeValue;

    public PolicyAttribute(URN attributeId, final AttributeValue<?> attributeValue) {
        Validate.notNull(attributeId, "attributeId is required for " + getClass().getSimpleName());
        Validate.notNull(attributeValue, "attributeValue is required for " + getClass().getSimpleName());

        this.attributeId = attributeId;
        this.attributeValue = attributeValue;
    }

    public URN getAttributeId() {
        return attributeId;
    }

    public AttributeValue<?> getAttributeValue() {
        return attributeValue;
    }

    public abstract PolicyAttribute resolvedCopy();

    @Override
    public String toString() {
        return "PolicyAttribute{" + describeAttributes() + '}';
    }

    protected String describeAttributes() {
        return "attributeId=" + attributeId
                + ", attributeValue=" + attributeValue;
    }

	public String toLogString() {
		return  "PolicyAttribute {" + logDescribeAttributes() + '}';
	}

	protected String logDescribeAttributes() {
		return (String)attributeValue.getValue();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PolicyAttribute that = (PolicyAttribute) o;

        return new EqualsBuilder()
                .append(this.attributeId, that.attributeId)
                .append(this.attributeValue, that.attributeValue)
                .isEquals();
    }

    @Override
    public int hashCode() {
        int result = attributeId != null ? attributeId.hashCode() : 0;
        result = 31 * result + (attributeValue != null ? attributeValue.hashCode() : 0);
        return result;
    }
}
