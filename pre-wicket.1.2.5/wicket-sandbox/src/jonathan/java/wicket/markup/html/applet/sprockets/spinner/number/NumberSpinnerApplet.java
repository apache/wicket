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
package wicket.markup.html.applet.sprockets.spinner.number;

import java.awt.Color;
import java.awt.Container;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import wicket.markup.html.applet.IApplet;
import wicket.markup.html.applet.IAppletServer;

/**
 * THIS CLASS IS NOT PART OF THE WICKET PUBLIC API. DO NOT ATTEMPT TO USE IT.
 * 
 * Private implementation of spinner Sprocket.
 *
 * @author Jonathan Locke
 */
public class NumberSpinnerApplet implements IApplet
{
	/** The Swing spinner */
	private JSpinner spinner;
	
	/** The model for this sprocket's applet */
	private SpinnerNumberModel model;

	/**
	 * @see wicket.markup.html.applet.IApplet#init(wicket.markup.html.applet.IAppletServer, java.awt.Container, java.lang.Object)
	 */
	public void init(final IAppletServer server, final Container container, final Object modelObject)
	{
		this.model = (SpinnerNumberModel)modelObject;
		container.setBackground(Color.white);
		container.add(spinner = new JSpinner(model));
		spinner.setBackground(Color.white);
	}
	
	/**
	 * @see wicket.markup.html.applet.IApplet#getModel()
	 */
	public Object getModel()
	{
		return (SpinnerNumberModel)spinner.getModel();
	}
}
