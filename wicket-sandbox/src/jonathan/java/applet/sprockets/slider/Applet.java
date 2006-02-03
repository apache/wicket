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
package applet.sprockets.slider;

import java.awt.Color;
import java.awt.Container;

import javax.swing.JSlider;

import applet.IApplet;
import applet.IAppletServer;

public class Applet implements IApplet
{
	JSlider slider;
	AppletModel model;
	
	public void init(IAppletServer server, Container container, Object modelObject)
	{
		this.model = (AppletModel)modelObject;
		container.setBackground(Color.white);
		container.add(slider = new JSlider(model.min, model.max, model.value));
	}

	public Object getModel()
	{
		model.value = slider.getValue();
		return model;
	}
}
