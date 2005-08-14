/*
 * $Id$
 * $Revision$ $Date$
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
package wicket;

/**
 * Resolves a class by using the classloader that loaded this class.
 * 
 * @see ApplicationSettings
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultClassResolver implements IClassResolver
{
	/**
	 * @see wicket.IClassResolver#resolveClass(java.lang.String)
	 */
	public final Class resolveClass(String classname)
	{
		try
		{
			return DefaultClassResolver.class.getClassLoader().loadClass(classname);
		}
		catch (ClassNotFoundException ex)
		{
			throw new WicketRuntimeException("Unable to load class with name: " + classname);
		}
	}
}

