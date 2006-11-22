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
package wicket.markup.html.applet.sprockets.slider;

import wicket.markup.html.applet.Sprocket;
import wicket.model.IModel;

/**
 * Slider Sprocket whose model is an Integer object.
 * 
 * @author Jonathan Locke
 */
public class Slider extends Sprocket
{
	/** The applet model for this sprocket */
	private SliderAppletModel appletModel;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            The component's id
	 * @param model
	 *            The (Integer) model to change
	 * @param min
	 *            The minimum value for this slider
	 * @param max
	 *            The maximum value for this slider
	 */
	public Slider(final String id, final IModel model, final int min, final int max)
	{
		super(id, model, SliderApplet.class);
		appletModel = new SliderAppletModel();
		appletModel.min = min;
		appletModel.max = max;
		appletModel.value = ((Integer)getModelObject()).intValue();
		setAppletModel(appletModel);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            The component's id
	 * @param min
	 *            The minimum value for this slider
	 * @param max
	 *            The maximum value for this slider
	 */
	public Slider(final String id, final int min, final int max)
	{
		super(id, SliderApplet.class);
		appletModel = new SliderAppletModel();
		appletModel.min = min;
		appletModel.max = max;
		appletModel.value = ((Integer)getModelObject()).intValue();
		setAppletModel(appletModel);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Sets the SliderAppletModel for this slider and uses that information to
	 * update the Slider Sprocket's normal Wicket component model as well.
	 * 
	 * @see wicket.markup.html.applet.Applet#setAppletModel(java.lang.Object)
	 */
	public void setAppletModel(final Object object)
	{
		appletModel = (SliderAppletModel)object;
		setModelObject(new Integer(appletModel.value));
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Gets the SliderAppletModel for this slider.
	 * 
	 * @see wicket.markup.html.applet.Applet#getAppletModel()
	 */
	public Object getAppletModel()
	{
		return appletModel;
	}
}
