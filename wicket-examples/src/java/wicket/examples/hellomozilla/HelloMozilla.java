/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.hellomozilla;

import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.protocol.http.HttpResponse;

/**
 * Simple example of how XUL could be used while still depending on the HTML components.
 *
 * @author Eelco Hillenius
 */
public class HelloMozilla extends HtmlPage
{
	/** message. */
	private String message = "Hello Mozilla!";

    /**
     * Constructor
     * @param parameters Page parameters
     */
    public HelloMozilla(final PageParameters parameters)
    {
		add(new XulDescription("hellomessage", this, "message"));
		add(new MessageForm("messageForm"));
    }

    /**
     * Set-up response header for using XUL.
     * @param cycle the request cycle
     */
    protected void configureResponse(final RequestCycle cycle)
    {
    	((HttpResponse)cycle.getResponse()).setLocale(cycle.getSession().getLocale());
    	cycle.getResponse().setContentType("application/vnd.mozilla.xul+xml");    	
    }

	/**
	 * Gets the message.
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}
	/**
	 * Sets the message.
	 * @param message the message
	 */
	public void setMessage(String message)
	{
		this.message = message;
	}

    /**
     * Form for recording a message.
     */
    private class MessageForm extends Form
    {
    	/**
    	 * Construct.
    	 * @param name form name
    	 */
    	public MessageForm(String name)
    	{
    		super(name, null);
    		add(new TextField("messageInput", HelloMozilla.this, "message"));
    	}

		/**
		 * @see wicket.markup.html.form.Form#handleSubmit()
		 */
		public void handleSubmit()
		{
			// we just let our property model handle the updating
		}
    }
}