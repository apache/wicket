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
public class Home extends WebPage
{
	private int value;

	/**
	 * Constructor.
	 */
	public Home()
	{
		Form form = new Form("form")
		{
			protected void onSubmit()
			{
				System.out.println("Submitted value: " + getValue());
			}
		};
		form.add(new Slider("slider", new PropertyModel(Home.this, "value"), 0, 100));
		form.add(new NumberSpinner("spinner", new PropertyModel(Home.this, "value"),
				new Integer(0), new Integer(100), new Integer(10)));
		add(form);
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
