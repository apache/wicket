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
package wicket.markup.html.applet;

/**
 * Interface to the originating server that served up an applet. The setModel()
 * and getModel() methods push and pull models to/from the server side Wicket
 * Applet component.
 * 
 * @see wicket.markup.html.applet.IApplet
 * 
 * @author Jonathan Locke
 */
public interface IAppletServer
{
	/**
	 * Sets the model on the server
	 * 
	 * @param model
	 *            The model to send back to the server
	 */
	void setModel(Object model);

	/**
	 * Gets model from server
	 * 
	 * @return The model from the server
	 */
	Object getModel();
}
