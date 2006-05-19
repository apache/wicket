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
package wicket.markup.html.tree;

import wicket.Application;
import wicket.IInitializer;
import wicket.markup.html.PackageResource;

/**
 * This component initializer initializes the dependencies of the Tree Component.
 * 
 * @author jcompagner
 */
public class TreeComponentInitializer implements IInitializer
{
	/**
	 * @param application
	 *            The application
	 */
	public void init(Application application)
	{
		PackageResource.bind(application, Tree.class, "blank.gif");
		PackageResource.bind(application, Tree.class, "minus.gif");
		PackageResource.bind(application, Tree.class, "plus.gif");
		PackageResource.bind(application, Tree.class, "tree.css");
	}
}
