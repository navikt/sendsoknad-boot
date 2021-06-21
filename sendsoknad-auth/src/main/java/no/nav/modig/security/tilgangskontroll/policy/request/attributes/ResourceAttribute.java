package no.nav.modig.security.tilgangskontroll.policy.request.attributes;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.AttributeValue;

/**
 * Attribute of a resource.
 * <p/>
 * Is not normally instantiated directly. Use one of the standard helper methods in
 * {@link no.nav.modig.security.tilgangskontroll.utils.AttributeUtils} or create your own helper class in your application.
 * <p/>
 * Attribute IDs and types should be standarized throughout an application to facilitate policy/rule writing.
 */
public class ResourceAttribute extends PolicyAttribute {

    public ResourceAttribute(URN attributeId, AttributeValue attributeValue) {
        super(attributeId, attributeValue);
    }

    @Override
    public PolicyAttribute resolvedCopy() {
        return new ResourceAttribute(this.getAttributeId(), this.getAttributeValue().getResolvedCopy());
    }

	public String toLogString() {
		return  "Resource= " + logDescribeAttributes();
	}

    @Override
    public String toString() {
        return "ResourceAttribute{" + describeAttributes() + '}';
    }
}
