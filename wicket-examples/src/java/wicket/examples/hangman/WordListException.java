// (c)2004 Templemore Technologies Limited. All Rights Reserved
// http://www.templemore.co.uk/copyright.html
package wicket.examples.hangman;

/**
 * Exception thrown when an error occurs reading the word list. This
 * is a runtime exception as failure to read the word list is fatal
 * to the functioning of the application.
 *
 * @author Chris Turner
 * @version 1.0
 */
public class WordListException extends RuntimeException {

    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Create a new exception instance.
     */
    public WordListException() {
        super();
    }

    /**
     * Create a new exception instance with the supplied message.
     *
     * @param message The message
     */
    public WordListException(final String message) {
        super(message);
    }

    /**
     * Create a new exception instance wrapping the supplied cause
     * exception.
     *
     * @param cause The cause exception
     */
    public WordListException(final Throwable cause) {
        super(cause);
    }

    /**
     * Create a new exception instance with the supplied message and
     * wrapping the supplied cause exception.
     *
     * @param message The message
     * @param cause The cause exception
     */
    public WordListException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
