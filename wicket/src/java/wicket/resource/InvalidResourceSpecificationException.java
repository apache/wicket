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
package wicket.resource;

/**
 * Exception thrown if a request is made to obtain a resource for which
 * the specified information is invalid. This is normally the case where
 * the information supplied to loader methods is incomplete.
 *
 * @author Chris Turner
 */
public class InvalidResourceSpecificationException extends RuntimeException {

	/** Serial Version ID */
	private static final long serialVersionUID = 638223850328845976L;

	/**
     * Create the exception.
     */
    public InvalidResourceSpecificationException() {
    }

    /**
     * Create the exception with the given message.
     *
     * @param message The message
     */
    public InvalidResourceSpecificationException(final String message) {
        super(message);
    }

    /**
     * Create the exception, wrapping the given cause.
     *
     * @param cause The cause
     */
    public InvalidResourceSpecificationException(final Throwable cause) {
        super(cause);
    }

    /**
     * Create the exception with the given message, wrapping the
     * given cause.
     *
     * @param message The message
     * @param cause The cause
     */
    public InvalidResourceSpecificationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
