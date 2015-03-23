/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

/**
 * Exception indicating that preparation of a PictureUnit failed.
 *
 * @author Jordi Hendrix
 */
@SuppressWarnings("serial")
public class PUPrepareException extends Exception {

    public PUPrepareException(String message) {
        super(message);
    }
}
