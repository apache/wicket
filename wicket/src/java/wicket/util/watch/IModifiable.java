/*
 * $Id: IModifiable.java 3585 2006-01-02 07:37:31 +0000 (Mon, 02 Jan 2006)
 * jonathanlocke $ $Revision$ $Date: 2006-01-02 07:37:31 +0000 (Mon, 02
 * Jan 2006) $
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
package wicket.util.watch;

import wicket.util.time.Time;

/**
 * Interface to get the last time something was modified.
 * 
 * @author Jonathan Locke
 */
public interface IModifiable
{
	/**
	 * Gets the last time this modifiable thing changed.
	 * 
	 * @return The last modification time.
	 */
	Time lastModifiedTime();
}
