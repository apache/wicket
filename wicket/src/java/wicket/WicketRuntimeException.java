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
package wicket;

/**
 * Runtime exception thrown during request processing.
 * @author Jonathan Locke
 */
public class WicketRuntimeException extends RuntimeException
{ // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = 3796104527069637919L;

	/**
     * Constructor
     */
    public WicketRuntimeException()
    {
        super();
    }

    /**
     * Constructor
     * @param message
     */
    public WicketRuntimeException(final String message)
    {
        super(message);
    }

    /**
     * Constructor
     * @param message
     * @param cause
     */
    public WicketRuntimeException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructor
     * @param cause
     */
    public WicketRuntimeException(final Throwable cause)
    {
        super(cause);
    }
}


