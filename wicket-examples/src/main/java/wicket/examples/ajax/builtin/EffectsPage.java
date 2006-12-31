/*
 * $Id: EffectsPage.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) $
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
package wicket.examples.ajax.builtin;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.AjaxLink;
import wicket.markup.html.basic.Label;
import wicket.model.PropertyModel;

/**
 * Demonstrates ajax effects
 */
public class EffectsPage extends BasePage
{
	private int counter1 = 0;
	private int counter2 = 0;

	/**
	 * @return Value of counter1
	 */
	public int getCounter1()
	{
		return counter1;
	}

	/**
	 * @param counter1
	 *            New value for counter1
	 */
	public void setCounter1(final int counter1)
	{
		this.counter1 = counter1;
	}

	/**
	 * @return Value for counter2
	 */
	public int getCounter2()
	{
		return counter2;
	}

	/**
	 * @param counter2
	 *            New value for counter2
	 */
	public void setCounter2(final int counter2)
	{
		this.counter2 = counter2;
	}

	/**
	 * Constructor
	 */
	public EffectsPage()
	{
		final Label c1 = new Label(this, "c1", new PropertyModel(this, "counter1"));
		c1.setOutputMarkupId(true);

		final Label c2 = new Label(this, "c2", new PropertyModel(this, "counter2"));
		c2.setOutputMarkupId(true);

		new AjaxLink(this, "c1-link")
		{
			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				counter1++;
				target.addComponent(c1);
				target.appendJavascript("new Effect.Shake($('" + c1.getMarkupId() + "'));");
			}
		};

		new AjaxFallbackLink(this, "c2-link")
		{
			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				counter2++;
				if (target != null)
				{
					target.addComponent(c2);
					target.appendJavascript("new Effect.Highlight($('" + c2.getMarkupId() + "'));");
				}
			}

		};
	}
}