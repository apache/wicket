/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
