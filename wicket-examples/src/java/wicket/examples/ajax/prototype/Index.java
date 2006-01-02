package wicket.examples.ajax.prototype;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.RequestCycle;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.link.ILinkListener;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.request.target.ComponentRequestTarget;

/**
 * Page object.
 */
public class Index extends WebPage
{
	/** Counts the number of clicks. */
	private Integer counter = Integer.valueOf(0);

	private class AjaxLink extends WebMarkupContainer implements ILinkListener {

		public AjaxLink(String id)
		{
			super(id);
		}

		public void onLinkClicked()
		{
			counter = Integer.valueOf(counter.intValue() + 1);
			
			// set the request target to the label
			ComponentRequestTarget target = new ComponentRequestTarget(getPage().get("counter"));
			RequestCycle cycle = RequestCycle.get();
			cycle.setRequestTarget(target);
		}
		
	}
	/**
	 * Constructor.
	 */
	public Index() {
		final AjaxLink ajaxLink = new AjaxLink("link");
		add(ajaxLink);
		ajaxLink.add(new AttributeModifier("onclink", true, new AjaxLinkJavaScriptEvent()));
		add(new Label("counter", new PropertyModel(this, "counter")));
	}

	/**
	 * @return Returns the counter.
	 */
	public Integer getCounter()
	{
		return counter;
	}

	/**
	 * @param counter The counter to set.
	 */
	public void setCounter(Integer counter)
	{
		this.counter = counter;
	}
	/**
	 * Generates the onsubmit JavaScript event for the form. 
	 */
	private final class AjaxLinkJavaScriptEvent extends Model {
		/** for serialization. */
		private static final long serialVersionUID = 1L;

		private AjaxLinkJavaScriptEvent() {
			super();
		}

		/**
		 * Gets the javascript for the click event of the link.
		 * @param component the component that this model is bound to
		 * @return the JavaScript string for the Ajax call
		 */
		public Object getObject(Component component) {
			StringBuilder sb = new StringBuilder();
		
			sb.append("new Ajax.Updater('counter',");
			sb.append("'" + component.urlFor(ILinkListener.class) + "',{method:'get'}");
			sb.append(");");
			// always return false, otherwise the submit event gets sent to the server. We
			// are already processing the ajax event.
			sb.append("return false;");
		
			return sb.toString();
		}
	}
}
