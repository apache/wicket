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
package nl.openedge.util.hibernate;


/**
 * Configuration exception.
 */
public final class ConfigException extends Exception
{
    /**
     * Construct.
     */
    public ConfigException()
    {
        super();
    }

    /**
     * Construct.
     * @param message message
     */
    public ConfigException(String message)
    {
        super(message);
    }

    /**
     * Construct.
     * @param cause cause
     */
    public ConfigException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Construct.
     * @param message message
     * @param cause cause
     */
    public ConfigException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
