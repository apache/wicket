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
package wapplet;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JButton;

public class Wapplet implements IAppletInitializer
{
	public void init(Container container, Object model)
	{
	    container.setBackground(Color.white);
	    container.setLayout(new FlowLayout()); 
	    container.add(new JButton("Button 1"));
	    container.add(new JButton("Button 2"));
	    container.add(new JButton("Button 3"));		
	}
}
