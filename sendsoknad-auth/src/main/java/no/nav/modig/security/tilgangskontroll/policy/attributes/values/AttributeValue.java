package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

/**
 * THIS CLASS IS NOT PART OF THE PUBLIC API, DO NOT INSTANTIATE IT DIRECTLY
 * <p/>
 * Base class for attribute values.
 * <p/>
 * This class is used to make sure that there is a strongly typed connection between XACML attribute types such as
 * "http://www.w3.org/2001/XMLSchema#string" and the Java counterpart.
 * <p/>
 * An attribute value might resolve at a point later than construction time. Use a {@link AttributeValueResolver} if the value
 * is to be resolved first when a PolicyRequest is created.
 * <p/>
 * This is useful when binding attribute to the value of wicket models.
 * <p/>
 * <p/>
 * As the value of an {@link AttributeValueResolver} might change at runtime it is useful to get an immutable resolved copy of
 * the value. This is supported through {@link #getResolvedCopy()}
 */
public abstract class AttributeValue<T> implements Serializable {
    private final String type;
    private final AttributeValueResolver<T> valueResolver;

    public AttributeValue(String type, final T value) {
        Validate.notNull(type, "type can not be null");
        Validate.notNull(value, "value can not be null");
        this.type = type;
        this.valueResolver = new ResolvedValue<T>(value);
    }

    public AttributeValue(String type, AttributeValueResolver<T> valueResolver) {
        Validate.notNull(type, "type can not be null");
        Validate.notNull(valueResolver, "valueResolver can not be null");
        this.type = type;
        this.valueResolver = valueResolver;
    }

    /**
     * Returns the urn for the XACML datatype.
     * 
     * @return urn for XACML datatype.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the java value of the attribute. If the attribute value is bound via a {@link AttributeValueResolver} the value
     * is resolved before being returned.
     * 
     * @return the value of the attribute
     */
    public T getValue() {
        T value = valueResolver.getValue();
        Validate.notNull(value, "resolved value can not be null");
        return value;
    }

    public AttributeValueResolver<T> getValueResolver() {
        return valueResolver;
    }

    public boolean isResolved() {
        return valueResolver instanceof ResolvedValue;
    }

    public abstract AttributeValue<T> getResolvedCopy();

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof AttributeValue)) {
            return false;
        }

        AttributeValue that = (AttributeValue) o;

        return new EqualsBuilder()
                .append(this.type, that.type)
                .append(this.valueResolver.getValue(), that.valueResolver.getValue())
                .isEquals();
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (valueResolver.getValue() != null ? valueResolver.getValue().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AttributeValue{"
                + "type='" + type + '\''
                + ", value=" + valueResolver.getValue()
                + '}';
    }

    /**
     * Holds a resolved value.
     * 
     * @param <T>
     *            Type of value
     */
    private static final class ResolvedValue<T> implements AttributeValueResolver<T> {
        private final T value;

        public ResolvedValue(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }
    }
}
