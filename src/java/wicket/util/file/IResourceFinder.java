/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.util.file;

import java.net.URL;

/**
 * Knows how to manage paths and folders, and how to find resources in them.
 *
 * @author jcompagner
 */
public interface IResourceFinder
{
	/**
	 * Looks for a given pathname along this path
	 * 
	 * @param pathname
	 *            The filename with possible path
	 * @return The url located on the path
	 */
	public URL find(final String pathname);

	/**
	 * @param path
	 *            add a path that is lookup through the servlet context
	 * @return The path, for invocation chaining
	 */
	public IResourceFinder add(String path);

	/**
	 * @param folder
	 *            Folder to add to path
	 * @return The path, for invocation chaining
	 */
	public IResourceFinder add(final Folder folder);
}
