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
package wicket.util.parse.metapattern;

/**
 * Thrown if an attempt is made to re-bind a Group to a second capturing
 * group or MetaPattern.
 * 
 * @author Jonathan Locke
 */
public final class GroupAlreadyBoundException extends RuntimeException
{
	/** serialVersionUID. */
	private static final long serialVersionUID = 5110506464403807991L;

	/**
     * Constructor.
     */
    public GroupAlreadyBoundException()
    {
        super();
    }
}

///////////////////////////////// End of File /////////////////////////////////
