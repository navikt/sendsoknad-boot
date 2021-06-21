package no.nav.modig.security.tilgangskontroll.policy.request.attributes;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.AttributeValue;

/**
 * Subject attributes
 * <p/>
 * Is not normally instantiated directly. Use one of the standard helper methods in
 * {@link no.nav.modig.security.tilgangskontroll.utils.AttributeUtils} or create your own helper class in your application.
 * <p/>
 * Subject attributes are normally automatically appended to the request through
 * {@link no.nav.modig.security.tilgangskontroll.policy.enrichers.SecurityContextRequestEnricher}
 * <p/>
 * Attribute IDs and types should be standarized throughout an application to facilitate policy/rule writing.
 */
public class EnvironmentAttribute extends PolicyAttribute {

    public EnvironmentAttribute(URN attributeId, AttributeValue<?> attributeValue) {
        super(attributeId, attributeValue);
    }

    @Override
    public EnvironmentAttribute resolvedCopy() {
        return new EnvironmentAttribute(this.getAttributeId(), this.getAttributeValue().getResolvedCopy());
    }

	public String toLogString() {
		return  "Environment= " + logDescribeAttributes();
	}

    @Override
    public String toString() {
        return "EnvironmentAttribute{"
                + describeAttributes()
                + '}';
    }
}
