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
package wicket.util.string;

/**
 * Typesafe interface to an ordered sequence of strings
 * @author Jonathan Locke
 */
public interface IStringSequence
{
    /**
     * @return Typesafe string iterator
     */
    public IStringIterator iterator();

    /**
     * @return Number of strings in this sequence
     */
    public int size();

    /**
     * @param index The index
     * @return The string at the given index
     * @throws IndexOutOfBoundsException
     */
    public String get(int index) throws IndexOutOfBoundsException;
}

///////////////////////////////// End of File /////////////////////////////////
