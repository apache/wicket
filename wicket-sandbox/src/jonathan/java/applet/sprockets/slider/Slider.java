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

import wicket.model.IModel;
import applet.Sprocket;

/**
 * Slider whose model is an Integer object.
 * 
 * @author Jonathan Locke
 */
public class Slider extends Sprocket
{
	private AppletModel appletModel;
	
	public Slider(String id, IModel model, int min, int max)
	{
		super(id, model, Applet.class);
		appletModel = new AppletModel();
		appletModel.min = min;
		appletModel.max = max;
		appletModel.value = ((Integer)getModelObject()).intValue();
		setAppletModel(appletModel);
	}
	
	public void setAppletModel(Object object)
	{
		appletModel = (AppletModel)object;
		setModelObject(new Integer(appletModel.value));
	}
	
	public Object getAppletModel()
	{
		return appletModel;
	}
}
