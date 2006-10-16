/*
 * $Id: Initializer.java 5081 2006-03-22 00:40:55 -0800 (Wed, 22 Mar 2006)
 * ivaynberg $ $Revision$ $Date: 2006-03-22 00:40:55 -0800 (Wed, 22 Mar
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions;

import wicket.Application;
import wicket.IInitializer;
import wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;

/**
 * Initializer for the extensions package.
 * 
 * @author jcompagner
 */
public class Initializer implements IInitializer
{
	/**
	 * @see wicket.IInitializer#init(wicket.Application)
	 */
	public void init(Application application)
	{
		new UploadProgressBar.ComponentInitializer().init(application);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Wicket extensions initializer";
	}
}
