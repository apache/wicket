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
package wicket.util.lang;

/**
 * Utilities for working with primitive values
 * @author Jonathan Locke
 */
public final class Primitives
{ // TODO finalize javadoc
    /**
     * @param value The long value
     * @return Hash code
     */
    public static int hashCode(final long value)
    {
        return (int) value ^ (int) (value >> 32);
    }
}

///////////////////////////////// End of File /////////////////////////////////
