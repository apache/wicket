/*
 * $Id: IClassResolver.java 3606 2006-01-03 07:21:55 +0000 (Tue, 03 Jan 2006)
 * jonathanlocke $ $Revision$ $Date: 2006-01-03 07:21:55 +0000 (Tue, 03
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
package wicket.application;

/**
 * An interface to code which finds classes.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public interface IClassResolver
{
	/**
	 * Resolves a class by name (which may or may not involve loading it; thus
	 * the name class *resolver* not *loader*).
	 * 
	 * @param classname
	 *            Fully qualified classname to find
	 * @return Class
	 */
	Class resolveClass(final String classname);
}