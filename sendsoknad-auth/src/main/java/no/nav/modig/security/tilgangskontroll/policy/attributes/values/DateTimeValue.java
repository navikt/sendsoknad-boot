package no.nav.modig.security.tilgangskontroll.policy.attributes.values;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * Represents XACML attribute value of type dateTime.
 * 
 * The underlying representation is an {@link XMLGregorianCalendar} object.
 */
public class DateTimeValue extends AttributeValue<XMLGregorianCalendar> {

    public static final String DATATYPE = "http://www.w3.org/2001/XMLSchema#dateTime";

    private DateTimeValue(XMLGregorianCalendar value) {
        super(DATATYPE, value);
    }

    /**
     * Creates a new DateTimeValue from GregorianCalendar with timezone.
     * 
     * @param cal
     *            GregorianCalendar object
     */
    public DateTimeValue(GregorianCalendar cal) {
        super(DATATYPE, getDatatypeFactory().newXMLGregorianCalendar(cal));
    }

    /**
     * Creates a new DateTimeValue with timezone
     * 
     * @param year
     *            year of <code>DateTimeValue</code> to be created.
     * @param month
     *            month of <code>DateTimeValue</code> to be created.
     * @param day
     *            day of <code>DateTimeValue</code> to be created.
     * @param hour
     *            hour of <code>DateTimeValue</code> to be created.
     * @param minute
     *            minute of <code>DateTimeValue</code> to be created.
     * @param second
     *            second of <code>DateTimeValue</code> to be created.
     * @param millisecond
     *            millisecond of <code>DateTimeValue</code> to be created.
     * @param timezone
     *            timezone offset in minutes
     */
    // CHECKSTYLE:OFF
    public DateTimeValue(int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone) {
        super(DATATYPE, getDatatypeFactory().newXMLGregorianCalendar(year, month, day, hour, minute, second, millisecond, timezone));
    }

    // CHECKSTYLE:ON

    @Override
    public DateTimeValue getResolvedCopy() {
        return new DateTimeValue(getValue());
    }

    private static DatatypeFactory getDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
