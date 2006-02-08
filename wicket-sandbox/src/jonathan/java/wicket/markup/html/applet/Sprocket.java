/*
 * $Id$ $Revision:
 * 1.1 $ $Date$
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
package wicket.markup.html.applet;

import wicket.model.IModel;

/**
 * A Sprocket is essentially an Applet, but an applet whose intention is to
 * permit fancy Swing-based model visualization or editing. This class is also
 * here to permit interesting functionality to be layered in in the future for
 * Sprockets that are not simply applets.
 * 
 * @author Jonathan Locke
 */
public class Sprocket extends Applet
{
	public Sprocket(final String id, final Class appletClass)
	{
		super(id, appletClass);
	}

	public Sprocket(final String id, final IModel model, final Class appletClass)
	{
		super(id, model, appletClass);
	}
}
