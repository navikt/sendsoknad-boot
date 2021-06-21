package no.nav.modig.security.tilgangskontroll.policy.request.attributes;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.AttributeValue;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;

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
public class SubjectAttribute extends PolicyAttribute {
    public static final URN ACCESS_SUBJECT = new URN("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
    public static final URN CODEBASE = new URN("urn:oasis:names:tc:xacml:1.0:subject-category:codebase");
    public static final URN CONSUMER = new URN("urn:nav:ikt:tilgangskontroll:xacml:subject-category:consumer-subject");

    private final URN subjectCategory;

    public SubjectAttribute(URN attributeId, AttributeValue<?> attributeValue) {
        super(attributeId, attributeValue);
        subjectCategory = ACCESS_SUBJECT;
    }

    public SubjectAttribute(URN subjectCategory, URN attributeId, AttributeValue<?> attributeValue) {
        super(attributeId, attributeValue);
        Validate.notNull(subjectCategory, "subjectCategory is required for SubjectAttribute");
        this.subjectCategory = subjectCategory;
    }

    public URN getSubjectCategory() {
        return subjectCategory;
    }

    @Override
    public SubjectAttribute resolvedCopy() {
        return new SubjectAttribute(this.subjectCategory, this.getAttributeId(), this.getAttributeValue().getResolvedCopy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        SubjectAttribute that = (SubjectAttribute) o;

        return new EqualsBuilder()
                .append(this.subjectCategory, that.subjectCategory)
                .isEquals();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (subjectCategory != null ? subjectCategory.hashCode() : 0);
        return result;
    }

	public String toLogString() {
		return  "Subject= " + logDescribeAttributes();
	}

    @Override
    public String toString() {
        return "SubjectAttribute{"
                + describeAttributes()
                + "subjectCategory=" + subjectCategory
                + '}';
    }
}
