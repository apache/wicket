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

import javax.swing.SpinnerNumberModel;

import wicket.markup.html.applet.Sprocket;
import wicket.model.IModel;

/**
 * Spinner Sprocket whose model is an Number object.
 * 
 * @author Jonathan Locke
 */
public class NumberSpinner extends Sprocket
{
	/** The applet model for this sprocket */
	private SpinnerNumberModel appletModel;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            The component's id
	 * @param model
	 *            The (Integer) model to change
	 * @param minimum
	 *            The minimum value for this spinner
	 * @param maximum
	 *            The maximum value for this spinner
	 * @param stepSize
	 *            The step size for this spinner
	 */
	public NumberSpinner(final String id, final IModel model, final Number minimum, final Number maximum, final Number stepSize)
	{
		super(id, model, NumberSpinnerApplet.class);
		appletModel = new SpinnerNumberModel();
		appletModel.setValue((Comparable)getModelObject());
		appletModel.setMaximum((Comparable)maximum);
		appletModel.setMinimum((Comparable)minimum);
		appletModel.setStepSize(stepSize);
		setAppletModel(appletModel);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            The component's id
	 * @param minimum
	 *            The minimum value for this spinner
	 * @param maximum
	 *            The maximum value for this spinner
	 * @param stepSize
	 *            The step size for this spinner
	 */
	public NumberSpinner(final String id, final Number minimum, final Number maximum, final Number stepSize)
	{
		super(id, NumberSpinnerApplet.class);
		appletModel = new SpinnerNumberModel();
		appletModel.setValue(getModelObject());
		appletModel.setMaximum((Comparable)maximum);
		appletModel.setMinimum((Comparable)minimum);
		appletModel.setStepSize(stepSize);
		setAppletModel(appletModel);
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Sets the applet model for this sprocket.
	 * 
	 * @see wicket.markup.html.applet.Applet#setAppletModel(java.lang.Object)
	 */
	public void setAppletModel(final Object object)
	{
		appletModel = (SpinnerNumberModel)object;
		setModelObject(appletModel.getValue());
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Gets the applet model for this sprocket.
	 * 
	 * @see wicket.markup.html.applet.Applet#getAppletModel()
	 */
	public Object getAppletModel()
	{
		return appletModel;
	}
}
