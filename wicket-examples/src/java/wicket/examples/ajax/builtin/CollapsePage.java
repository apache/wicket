/*
 * $Id: FormPage.java 4916 2006-03-13 23:15:39 -0800 (Mon, 13 Mar 2006)
 * ivaynberg $ $Revision: 4916 $ $Date: 2006-03-13 23:15:39 -0800 (Mon, 13 Mar
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

import java.util.TimeZone;

import wicket.ajax.AbstractAjaxTimerBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.extensions.ajax.markup.html.AjaxCollapsableBorder;
import wicket.util.time.Duration;

/**
 * Page to demonstrate wicket's ajax autocompletion components.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class CollapsePage extends BasePage
{
	/**
	 * Constructor
	 */
	public CollapsePage()
	{
		AjaxCollapsableBorder border=new AjaxCollapsableBorder("border");
		add(border);
		
		// create clock components for different timezones
		final Clock la = new Clock("la", TimeZone.getTimeZone("America/Los_Angeles"));
		final Clock ny = new Clock("ny", TimeZone.getTimeZone("America/New_York"));
		final Clock moscow = new Clock("moscow", TimeZone.getTimeZone("Europe/Moscow"));
		final Clock prague = new Clock("prague", TimeZone.getTimeZone("Europe/Prague"));
		final Clock london = new Clock("london", TimeZone.getTimeZone("Europe/London"));

		// make components print out id attrs so they can be updated via ajax
		la.setOutputMarkupId(true);
		ny.setOutputMarkupId(true);
		moscow.setOutputMarkupId(true);
		prague.setOutputMarkupId(true);
		london.setOutputMarkupId(true);

		// add the components to the container and add a markup id setter to
		// each component.
		border.add(la);
		border.add(ny);
		border.add(moscow);
		border.add(prague);
		border.add(london);

		// add the timer behavior to the la component and make it update all
		// other components as well
		la.add(new AbstractAjaxTimerBehavior(Duration.seconds(5))
		{
			/**
			 * @see wicket.ajax.AbstractAjaxTimerBehavior#onTimer(wicket.ajax.AjaxRequestTarget)
			 */
			protected void onTimer(AjaxRequestTarget target)
			{
				target.addComponent(la);
				target.addComponent(ny);
				target.addComponent(moscow);
				target.addComponent(prague);
				target.addComponent(london);
			}
		});	}


}