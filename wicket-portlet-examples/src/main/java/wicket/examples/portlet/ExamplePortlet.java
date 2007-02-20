package wicket.examples.portlet;
import javax.portlet.WindowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.ResourceReference;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.model.PropertyModel;
import wicket.protocol.http.portlet.PortletPage;

/**
 * @author Janne Hietam&auml;ki
 * 
 */
public class ExamplePortlet extends PortletPage<Object>
{
	private static final Logger log = LoggerFactory.getLogger(ExamplePortlet.class);

	/**
	 * 
	 */
	public ExamplePortlet()
	{
		// This model references the page's message property and is
		// shared by the label and form component
		PropertyModel<String> messageModel = new PropertyModel<String>(this, "message");

		// The label displays the currently set message
		new Label(this, "msg", messageModel);

		// Add a form to change the message. We don't need to do anything
		// else with this form as the shared model is automatically updated
		// on form submits
		Form form = new Form(this, "form")
		{

			protected void onSubmit()
			{
				log.info(hashCode() + " : " + this + " Form.onSubmit()");
			}

		};
		
		new TextField<String>(form, "msgInput", messageModel);

		new Link<String>(this, "link")
		{
			public void onClick()
			{
				log.info("link clicked");
				message = "Link clicked!";
			}
		};

		new Link<String>(this, "link2")
		{

			public void onClick()
			{
				setResponsePage(new ExamplePortlet2(ExamplePortlet.this));
			}
		};

		new Image(this, "image",new ResourceReference(ExamplePortlet.class,"wicket-logo.png"));
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