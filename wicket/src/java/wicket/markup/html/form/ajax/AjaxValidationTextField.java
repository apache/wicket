/**
 * 
 */
package wicket.markup.html.form.ajax;

import java.util.List;

import wicket.FeedbackMessage;
import wicket.RequestCycle;
import wicket.markup.ComponentTag;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.IModel;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.StringResourceStream;

/**
 * @author jcompagner
 *
 */
public class AjaxValidationTextField extends TextField implements IAjaxValidator
{
	FeedbackPanel feedback;
	/**
	 * @param id
	 */
	public AjaxValidationTextField(String id, FeedbackPanel feedback)
	{
		super(id);
		this.feedback = feedback;
	}

	/**
	 * @param id
	 * @param type
	 */
	public AjaxValidationTextField(String id, Class type)
	{
		super(id, type);
	}

	/**
	 * @param id
	 * @param object
	 */
	public AjaxValidationTextField(String id, FeedbackPanel feedback, IModel object)
	{
		super(id, object);
		this.feedback = feedback;
	}

	/**
	 * @param id
	 * @param model
	 * @param type
	 */
	public AjaxValidationTextField(String id, IModel model, Class type)
	{
		super(id, model, type);
	}

	/**
	 * @see wicket.markup.html.form.ajax.IAjaxValidator#validateInput()
	 */
	public IResourceStream validateInput()
	{
		validate();
		List lst = getPage().getFeedbackMessages().messages(this, true, false, FeedbackMessage.ERROR);
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		sb.append("<response>");
		sb.append("var feedbackul =  document.getElementById('feedbackul');");
		sb.append("var children = feedbackul.childNodes;");
		sb.append("if(children.length > 0) feedbackul.removeChild(children[0]);");
		for (int i = 0; i < lst.size(); i++)
		{
			FeedbackMessage message = (FeedbackMessage)lst.get(i);
			
			sb.append("var li = document.createElement('li');");
			sb.append("var linkText = document.createTextNode('");
			sb.append(message.getMessage().replace('\'','"'));
			sb.append("');");
			sb.append("li.appendChild(linkText);");
			sb.append("document.getElementById('feedbackul').appendChild(li);");
		}
		sb.append("</response>");
		return new StringResourceStream(sb.toString(),"text/xml");
	}
	
	protected void onComponentTag(final ComponentTag tag)
	{
		// url that points to this components IOnChangeListener method
		final String url = urlFor(IAjaxValidator.class);

		// NOTE: do not encode the url as that would give invalid JavaScript
		tag.put("onChange", "ajaxSend('" + url + "&" + getPath()+  "=' + this.value,textfieldValidation);");

		super.onComponentTag(tag);
	}

	static
	{
		RequestCycle.registerAjaxListenerInterface(IAjaxValidator.class);
	}
}
