/*
 * $Id: IStringIterator.java 4710 2006-03-02 08:46:15 +0000 (Thu, 02 Mar 2006)
 * eelco12 $ $Revision$ $Date: 2006-03-02 08:46:15 +0000 (Thu, 02 Mar
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.string;

/**
 * Typesafe string iterator interface
 * 
 * @author Jonathan Locke
 */
public interface IStringIterator
{
	/**
	 * @return True if there is a next string
	 */
	boolean hasNext();

	/**
	 * @return The next string!
	 */
	String next();
}