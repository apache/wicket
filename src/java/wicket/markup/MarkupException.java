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
package wicket.markup;

import wicket.RenderException;
import wicket.util.resource.Resource;


/**
 * Exception thrown when markup parsing fails
 * @author Jonathan Locke
 */
public final class MarkupException extends RenderException
{ // TODO finalize javadoc
    /** Serial Version ID */
	private static final long serialVersionUID = 8431706931407810523L;
	
	private final MarkupStream markupStream;

    /**
     * Constructor
     * @param message The problem description
     */
    public MarkupException(final String message)
    {
        super(message);
        markupStream = null;
    }

    /**
     * @param resource The markup resource where this exception occurred
     * @param message The message
     */
    public MarkupException(final Resource resource, final String message)
    {
        super(resource.toString() + ": " + message);
        markupStream = null;
    }

    /**
     * @param resource The markup where this exception occurred
     * @param message The message
     * @param cause The causing exception
     */
    public MarkupException(final Resource resource, final String message, final Throwable cause)
    {
        super(resource.toString() + ": " + message, cause);
        markupStream = null;
    }

    /**
     * @param markupStream The markup stream where this exception occurred
     * @param message The message
     */
    public MarkupException(final MarkupStream markupStream, final String message)
    {
        super(message + "\n" + markupStream.toString());
        this.markupStream = markupStream;
    }

    /**
     * @return Returns the markupStream.
     */
    public MarkupStream getMarkupStream()
    {
        return markupStream;
    }
}

///////////////////////////////// End of File /////////////////////////////////
