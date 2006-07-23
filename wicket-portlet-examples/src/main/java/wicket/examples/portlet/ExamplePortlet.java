package wicket.examples.portlet;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.protocol.http.portlet.PortletPage;

/**
 * @author Janne Hietam&auml;ki
 * 
 */
public class ExamplePortlet extends PortletPage
{
	private static final Log log = LogFactory.getLog(ExamplePortlet.class);

	/**
	 * 
	 */
	public ExamplePortlet()
	{
		// This model references the page's message property and is
		// shared by the label and form component
		PropertyModel messageModel = new PropertyModel(this, "message");

		// The label displays the currently set message
		add(new Label("msg", messageModel));

		// Add a form to change the message. We don't need to do anything
		// else with this form as the shared model is automatically updated
		// on form submits
		Form form = new Form("form")
		{

			protected void onSubmit()
			{
				log.info(hashCode() + " : " + this + " Form.onSubmit()");
			}

		};
		form.add(new TextField("msgInput", messageModel));

		add(new Link("link")
		{
			public void onClick()
			{
				log.info("link clicked");
				message = "Link clicked!";
			}
		});

		add(form);
		add(new Link("link2")
		{

			public void onClick()
			{
				setResponsePage(new ExamplePortlet2(ExamplePortlet.this));
			}
		});

		// add(new Image("image",new
		// ResourceReference(ExamplePortlet.class,"wicket-logo.png")));
	}

	private String message = "[type your message to the world here]";

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

	protected void onSetWindowState(WindowState state){
		log.info("WindowState set to "+state);
		// Here we could do for example setResponsePage(MaximizedPage.class);
	}	
}