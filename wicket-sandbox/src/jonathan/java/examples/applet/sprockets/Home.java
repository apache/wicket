/*
 * $Id$ $Revision$
 * $Date$
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
package examples.applet.sprockets;

import wicket.markup.html.WebPage;
import wicket.markup.html.applet.sprockets.slider.Slider;
import wicket.markup.html.applet.sprockets.spinner.number.NumberSpinner;
import wicket.markup.html.form.Form;
import wicket.model.PropertyModel;

/**
 * Home page of the applet example.
 * 
 * @author Jonathan Locke
 */
public final class Home extends WebPage
{
	private int sliderValue;
	private int spinnerValue;

	/**
	 * Constructor.
	 */
	public Home()
	{
		final Form form = new Form("form")
		{
			protected void onSubmit()
			{
				System.out.println("Slider value: " + getSliderValue());
				System.out.println("Spinner value: " + getSpinnerValue());
			}
		};
		form.add(new Slider("slider", new PropertyModel(Home.this, "sliderValue"), 0, 100));
		form.add(new NumberSpinner("spinner", new PropertyModel(Home.this, "spinnerValue"),
				new Integer(0), new Integer(100), new Integer(10)));
		add(form);
	}

	public void setSliderValue(final int value)
	{
		this.sliderValue = value;
	}

	public int getSliderValue()
	{
		return sliderValue;
	}

	public void setSpinnerValue(final int value)
	{
		this.spinnerValue = value;
	}

	public int getSpinnerValue()
	{
		return spinnerValue;
	}
}
