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
package wicket.markup.html.form.validation;

/**
 * Exception class for unexcepted exceptions during validation.
 */
public final class ValidationException extends RuntimeException
{ // TODO finalize javadoc

    /**
     * Construct.
     */
    public ValidationException()
    {
        super();
    }

    /**
     * Construct.
     * @param message the message
     */
    public ValidationException(String message)
    {
        super(message);
    }

    /**
     * Construct.
     * @param cause the cause
     */
    public ValidationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Construct.
     * @param message the message
     * @param cause the cause
     */
    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
