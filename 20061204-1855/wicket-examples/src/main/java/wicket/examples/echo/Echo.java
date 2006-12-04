/*
 * $Id: HelloWorld.java 4942 2006-03-14 22:38:34 -0800 (Tue, 14 Mar 2006)
 * ivaynberg $ $Revision: 4942 $ $Date: 2006-03-14 22:38:34 -0800 (Tue, 14 Mar
 * 2006) $
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
package wicket.examples.echo;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.model.PropertyModel;

/**
 * The simplest form application possible. Just prints any user input to a
 * label.
 * 
 * @author Eelco Hillenius
 */
public class Echo extends WicketExamplePage
{
	private String message = "[type your message to the world here]";

	/**
	 * Constructor.
	 */
	public Echo()
	{
		// This model references the page's message property and is
		// shared by the label and form component
		PropertyModel messageModel = new PropertyModel(this, "message");

		// The label displays the currently set message
		add(new Label("msg", messageModel));

		// Add a form to change the message. We don't need to do anything
		// else with this form as the shared model is automatically updated
		// on form submits
		Form form = new Form("form");
		form.add(new TextField("msgInput", messageModel));
		add(form);
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}
}