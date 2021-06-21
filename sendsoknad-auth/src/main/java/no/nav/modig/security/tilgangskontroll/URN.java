package no.nav.modig.security.tilgangskontroll;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Klasse for typen Uniform Resource Name. For namespace brukt i Skatteetaten se wiki.<br>
 * Se http://en.wikipedia.org/wiki/Uniform_Resource_Name <br>
 * Spesifikasjon: http://www.ietf.org/rfc/rfc2141.txt
 */
public class URN implements Serializable, Comparable<URN> {

    private static final long serialVersionUID = 1L;

    public static final String URN_PATTERN_STRING = "^urn:[a-zA-Z0-9]{0,31}:([a-zA-Z_0-9()+,-.:=@;$_!*']|%[0-9a-f{2}])+";
    private static final Pattern URN_PATTERN = Pattern
            .compile(URN_PATTERN_STRING);

    private final String uniformResourceName;

    public URN(String uniformResourceName) {
        Validate.matchesPattern(uniformResourceName, URN_PATTERN_STRING, "Ugyldig URN: %s", uniformResourceName);
        this.uniformResourceName = uniformResourceName;
    }

    public String getURN() {
        return uniformResourceName;
    }

    /**
     * Static factory for URN
     * 
     * @param uniformResourceName
     * @return konkret URN instans
     */
    public static URN urn(String uniformResourceName) {
        return new URN(uniformResourceName);
    }

    /**
     * Sjekker at det er gyldig URN. OBS - Sjekker ikke namespace.
     * 
     * @param uniformResourceName
     *            versien som skal sjekkes
     * @return true hvis det er en gyldig URN
     */
    public static boolean validateURN(String uniformResourceName) {
        return URN_PATTERN.matcher(uniformResourceName).matches();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime + ((uniformResourceName == null) ? 0 : uniformResourceName.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof URN)) {
            return false;
        }
        URN other = (URN) obj;
        if (uniformResourceName == null) {
            if (other.uniformResourceName != null) {
                return false;
            }
        } else if (!uniformResourceName.equals(other.uniformResourceName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "URN [urn=" + uniformResourceName + "]";
    }

    @Override
    public int compareTo(URN o) {
        return this.getURN().compareTo(o.getURN());
    }
}
