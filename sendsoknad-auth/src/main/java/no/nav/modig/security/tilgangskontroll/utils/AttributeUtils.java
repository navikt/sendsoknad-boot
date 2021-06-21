package no.nav.modig.security.tilgangskontroll.utils;

import no.nav.modig.security.tilgangskontroll.URN;
import no.nav.modig.security.tilgangskontroll.policy.attributes.AttributeIds;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.AttributeValueResolver;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DateTimeValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.DateValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.StringValue;
import no.nav.modig.security.tilgangskontroll.policy.attributes.values.TimeValue;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.ActionAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.ActionId;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.EnvironmentAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.ResourceAttribute;
import no.nav.modig.security.tilgangskontroll.policy.request.attributes.SubjectAttribute;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A collection of utils for instantiating attributes for policy requests
 */
public final class AttributeUtils {

    // **************** Action attributes ***************************************

    public static ActionAttribute actionId(String actionId) {
        return new ActionId(new StringValue(actionId));
    }

    // ***************** Resource attributes **************************************
    public static ResourceAttribute personFodselsnummer(String fodselsnummer) {
        return new ResourceAttribute(AttributeIds.FODSELS_NUMMER, new StringValue(fodselsnummer));
    }

    public static ResourceAttribute ownerId(String ownerId) {
        return new ResourceAttribute(AttributeIds.OWNER_ID, new StringValue(ownerId));
    }

    public static ResourceAttribute ownerId(AttributeValueResolver<String> resolver) {
        return new ResourceAttribute(AttributeIds.OWNER_ID, new StringValue(resolver));
    }

    public static ResourceAttribute resourceId(String resourceId) {
        return new ResourceAttribute(AttributeIds.RESOURCE_ID, new StringValue(resourceId));
    }

    public static ResourceAttribute resourceId(AttributeValueResolver<String> resourceId) {
        return new ResourceAttribute(AttributeIds.RESOURCE_ID, new StringValue(resourceId));
    }

    public static ResourceAttribute resourceType(String resourceType) {
        return new ResourceAttribute(AttributeIds.RESOURCE_TYPE, new StringValue(resourceType));
    }

    public static ResourceAttribute componentActionName(String name) {
        return new ResourceAttribute(AttributeIds.COMPONENT_ACTION_NAME, new StringValue(name));
    }

    public static ResourceAttribute componentId(String id) {
        return new ResourceAttribute(AttributeIds.COMPONENT_ID, new StringValue(id));
    }

    public static ResourceAttribute componentType(String type) {
        return new ResourceAttribute(AttributeIds.COMPONENT_TYPE, new StringValue(type));
    }

    public static ResourceAttribute pageType(String type) {
        return new ResourceAttribute(AttributeIds.PAGE_TYPE, new StringValue(type));
    }

    // ***************** Subject attributes **************************************

    public static SubjectAttribute subjectId(String subjectId) {
        return new SubjectAttribute(AttributeIds.SUBJECT_ID, new StringValue(subjectId));
    }

    public static SubjectAttribute subjectId(URN subjectCategory, String subjectId) {
        return new SubjectAttribute(subjectCategory, AttributeIds.SUBJECT_ID, new StringValue(subjectId));
    }

    public static SubjectAttribute authenticationLevel(String authenticationLevel) {
        return new SubjectAttribute(AttributeIds.AUTHENTICATION_LEVEL, new StringValue(authenticationLevel));
    }

    public static SubjectAttribute authenticationLevel(URN subjectCategory, String authenticationLevel) {
        return new SubjectAttribute(subjectCategory, AttributeIds.AUTHENTICATION_LEVEL, new StringValue(authenticationLevel));
    }

    public static SubjectAttribute identType(String identType) {
        return new SubjectAttribute(AttributeIds.IDENT_TYPE, new StringValue(identType));
    }

    public static SubjectAttribute identType(URN subjectCategory, String identType) {
        return new SubjectAttribute(subjectCategory, AttributeIds.IDENT_TYPE, new StringValue(identType));
    }

    // ****************** Environment attributes **********************************

    public static EnvironmentAttribute currentTime() {
        GregorianCalendar cal = new GregorianCalendar();

        return new EnvironmentAttribute(AttributeIds.CURRENT_TIME, new TimeValue(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)));
    }

    public static EnvironmentAttribute currentDate() {
        GregorianCalendar cal = new GregorianCalendar();
        // Calendar.MONTH is zero based
        // DateValue is based on XMLGregorianCalendar and starts with JANUARY as 1.
        return new EnvironmentAttribute(AttributeIds.CURRENT_DATE, new DateValue(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)));
    }

    public static EnvironmentAttribute currentDateTime() {
        return new EnvironmentAttribute(AttributeIds.CURRENT_DATE_TIME, new DateTimeValue(new GregorianCalendar()));
    }

    // ****************** Generic attribute ***************************************
    public static SubjectAttribute subjectAttribute(String name, String value) {
        return new SubjectAttribute(new URN(name), new StringValue(value));
    }

    public static ResourceAttribute resourceAttribute(String name, String value) {
        return new ResourceAttribute(new URN(name), new StringValue(value));
    }

    public static ActionAttribute actionAttribute(String name, String value) {
        return new ActionAttribute(new URN(name), new StringValue(value));
    }

    public static EnvironmentAttribute environmentAttribute(String name, String value) {
        return new EnvironmentAttribute(new URN(name), new StringValue(value));
    }

    private AttributeUtils() {
    }

    static {
        new AttributeUtils();
    }
}
