package uk.ac.ebi.pride.archive.web.service.controller.viewer;

/**
 * @author florian@ebi.ac.uk
 * @since 0.1.2
 */
@SuppressWarnings("unused")
public class InvalidDataException extends Exception {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDataException(Throwable cause) {
        super(cause);
    }
}
