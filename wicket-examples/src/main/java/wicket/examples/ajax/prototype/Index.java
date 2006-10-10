/*
 * $Id$ $Revision$ $Date:
 * 2006-05-26 07:08:28 +0200 (vr, 26 mei 2006) $
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
package wicket.examples.ajax.prototype;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ILinkListener;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.request.target.component.ComponentRequestTarget;
import wicket.util.string.AppendingStringBuffer;

/**
 * Example displaying partial page rendering using the counting link example and
 * prototype.js. Prototype.js is a javascript library that provides several
 * handy JavaScript functions, amongst others an Ajax.Updater function, which
 * updates the HTML document with the response of the Ajax call.
 * 
 * @author ivaynberg
 */
public class Index extends WicketExamplePage
{
	/** Click count. */
	private int count = 0;

	/** Label showing count */
	private final Label counter;

	/**
	 * Constructor.
	 */
	public Index()
	{
		// Add the Ajaxian link to the page...
		new Link(this, "link")
		{
			/**
			 * Handles a click on the link. This method is accessed normally
			 * using a standard http request, but in this example, we use Ajax
			 * to perform the call.
			 */
			@Override
			public void onClick()
			{
				// Increment count
				count++;

				// The response should refresh the label displaying the counter.
				getRequestCycle().setRequestTarget(new ComponentRequestTarget(counter));
			}

			/**
			 * Alter the javascript 'onclick' event to emit the Ajax call and
			 * update the counter label.
			 * 
			 * @see wicket.markup.html.link.Link#getOnClickScript(java.lang.CharSequence)
			 */
			@Override
			protected CharSequence getOnClickScript(CharSequence url)
			{
				return new AppendingStringBuffer("new Ajax.Updater('counter', '").append(
						urlFor(ILinkListener.INTERFACE))
						.append("', {method:'get'}); return false;");
			}
		};

		// Add the label
		counter = new Label(this, "counter", new PropertyModel(this, "count"));
	}

	/**
	 * @return Returns the count.
	 */
	public int getCount()
	{
		return count;
	}
}