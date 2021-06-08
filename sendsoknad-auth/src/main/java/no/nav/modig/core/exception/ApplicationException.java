package no.nav.modig.core.exception;

/**
 * SystemException skal brukes i tilfeller der en programfeil forÃ¥rsaker en exception Dette kan vÃ¦re manglende data o.l.
 */

public class ApplicationException extends ModigException {

    public ApplicationException(String message) {
        super(message);
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param message
     * @param cause
     * @param id
     *            i18n in wicket. Else general id for exception
     */
    public ApplicationException(String message, Throwable cause, String id) {
        super(message, cause, id);
    }

}
