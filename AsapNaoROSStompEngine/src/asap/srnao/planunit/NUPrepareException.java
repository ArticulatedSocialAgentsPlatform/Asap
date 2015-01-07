/*******************************************************************************
 *******************************************************************************/
package asap.srnao.planunit;

/**
 * Exception indicating that preparation of a NaoUnit failed.
 *
 * @author Daniel
 */
@SuppressWarnings("serial")
public class NUPrepareException extends Exception {

    public NUPrepareException(String message) {
        super(message);
    }
}
