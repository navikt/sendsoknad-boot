package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Represents XACML attribute value of type date
 */
public class DateValue extends AttributeValue<XMLGregorianCalendar> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#date";

    private DateValue(XMLGregorianCalendar value) {
        super(DATATYPE, value);
    }

    /**
     * Creates a date without timezone
     * 
     * Months starts with 1
     * 
     * @param year
     *            year of <code>DateValue</code> to be created.
     * @param month
     *            month of <code>DateValue</code> to be created.
     * @param day
     *            day of <code>DateValue</code> to be created.
     */
    public DateValue(int year, int month, int day) {
        super(DATATYPE, getDatatypeFactory().newXMLGregorianCalendar(year, month, day, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED));
    }

    /**
     * Creates a date with timezone
     * 
     * Months starts with 1
     * 
     * @param year
     *            year of <code>DateValue</code> to be created.
     * @param month
     *            month of <code>DateValue</code> to be created.
     * @param day
     *            day of <code>DateValue</code> to be created.
     * @param timezone
     *            timezone offset in minutes.
     * 
     */
    public DateValue(int year, int month, int day, int timezone) {
        super(DATATYPE, getDatatypeFactory().newXMLGregorianCalendar(year, month, day, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED, timezone));
    }

    @Override
    public DateValue getResolvedCopy() {
        return new DateValue(getValue());
    }

    private static DatatypeFactory getDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
