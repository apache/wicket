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
package examples.applet.simple;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import wicket.markup.html.applet.IApplet;
import wicket.markup.html.applet.IAppletServer;

/**
 * Simple applet with a button on it which sends a new applet model back to the
 * server.
 * 
 * @author Jonathan Locke
 */
public final class SimpleApplet implements IApplet
{
	/**
	 * Called to initalize this applet on the server side.
	 * 
	 * @see IApplet#init(IAppletServer, Container, Object)
	 */
	public void init(final IAppletServer server, final Container container, final Object model)
	{
		container.setBackground(Color.white);
		container.setLayout(new FlowLayout());
		final JButton button = new JButton(model.toString());
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				server.setModel("bar");
			};
		});
		container.add(button);
	}

	/**
	 * @see IApplet#getModel()
	 */
	public Object getModel()
	{
		return null;
	}
}
