package org.apache.wicket.extensions.ajax.markup.html.modal;

import java.io.Serializable;

import org.apache.wicket.Application;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

import com.github.openjson.JSONStringer;

/**
 * Presents a modal dialog to the user. See open and close methods.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ModalDialog extends Panel
{

	public static final String CONTENT_ID = "content";

	private final WebMarkupContainer container;
	private final WebMarkupContainer contentContainer;
	private boolean open = false;
	private transient boolean openedInThisRequest = false;
	private Options options;

	public ModalDialog(String id)
	{
		super(id);
		setOutputMarkupId(true);

		// Container controls the overall visibility of the modal form innards. In its initial state
		// this is set to
		// hidden so only the external tag renders. When the modal is openedInThisRequest it is
		// first repainted with
		// this shown to
		// insert the markup into the dom, and then the modal javascript is invoked to rip out this
		// tag out of the dom
		// and make it modal.
		container = new WebMarkupContainer("container");
		container.setOutputMarkupId(true);
		container.setVisible(false);
		add(container);

		// We need this here in case the modal itself is placed inside a form. If that is the case
		// and the content
		// contains a form that form's markup form tag will be rendered as div because as far as
		// wicket is concerned it
		// is part of the same dom as the page and nested forms are forbidden. By overriding
		// isRootForm() to true we are
		// forcing this form - which will be ripped out of the dom along with the content form if
		// there is one to always
		// render its tag as 'form' instead of 'div'.
		var form = new Form<Void>("form")
		{
			@Override
			public boolean isRootForm()
			{
				return true;
			}
		};
		form.setOutputMarkupId(true);
		container.add(form);

		contentContainer = form;
		contentContainer.add(new EmptyPanel(CONTENT_ID));
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(ModalDialogReferences.JS));

		// if the page is refreshed and the window was open in the previous request we need to
		// re-open it
		if (open == true && openedInThisRequest == false)
		{
			response.render(OnDomReadyHeaderItem.forScript(getOpenJavascript()));
		}
	}

	public ModalDialog open(AjaxRequestTarget target, WebMarkupContainer content)
	{
		open(target, null, content);
		return this;
	}

	public ModalDialog open(AjaxRequestTarget target, Options options, WebMarkupContainer content)
	{

		if (!content.getId().equals(CONTENT_ID))
		{
			throw new IllegalArgumentException(
				"Content must have wicket id set to ModalDialog.CONTENT_ID");
		}

		contentContainer.replace(content);

		container.setVisible(true);

		open = true;
		openedInThisRequest = true;

		target.add(this);

		this.options = options;


		// the use of prepend here is purposeful. it makes sure that any javascript contributed that
		// affects elements inside the modal is executed after said elements have been
		// placed in their correct dom location
		target.prependJavaScript(getOpenJavascript());
		return this;
	}

	protected String getOpenJavascript()
	{
		Options options = this.options;

		if (options == null)
		{
			options = new Options();
		}

		if (options.validate == null)
		{
			options.validate = Application.get().usesDevelopmentConfig();
		}

		String optionsJson = options.toJson();
		String javascript = String.format("window.wicket.modal.open('%s', %s);",
			container.getMarkupId(), optionsJson);
		// wrapping in timeout removes the execution out of wicket's ajax update workflow making
		// errors non-fatal and
		// errors easier to debug due to simplified stack trace
		javascript = String.format("window.setTimeout(function() { %s }, 0);", javascript);
		return javascript;

	}

	public ModalDialog close(AjaxRequestTarget target)
	{
		open = false;
		this.options = null;
		String javascript = String.format("window.wicket.modal.close('%s');",
			container.getMarkupId());
		javascript = String.format("window.setTimeout(function() { %s }, 0);", javascript);
		target.prependJavaScript(javascript);
		container.setVisible(false);
		contentContainer.replace(new EmptyPanel(CONTENT_ID));
		target.add(this);
		return this;
	}

	@Override
	protected void onDetach()
	{
		super.onDetach();
		openedInThisRequest = false;
	}

	public static class Options implements Serializable
	{
		private Boolean validate;
		private String console;
		private String maxWidth;
		private String maxHeight;

		public Boolean getValidate()
		{
			return validate;
		}

		public void setValidate(Boolean validate)
		{
			this.validate = validate;
		}

		public String getConsole()
		{
			return console;
		}

		public void setConsole(String console)
		{
			this.console = console;
		}

		public String getMaxWidth()
		{
			return maxWidth;
		}

		public void setMaxWidth(String maxWidth)
		{
			this.maxWidth = maxWidth;
		}

		public String getMaxHeight()
		{
			return maxHeight;
		}

		public void setMaxHeight(String maxHeight)
		{
			this.maxHeight = maxHeight;
		}

		public String toJson()
		{
			var json = new JSONWriter();

			json.object();
			json.value("validate", validate);
			json.value("console", console);
			json.value("maxWidth", maxWidth);
			json.value("maxHeight", maxHeight);
			json.endObject();
			return json.toString();
		}

		private static class JSONWriter extends JSONStringer
		{
			public JSONWriter value(String key, Object value)
			{
				if (value != null)
				{
					key(key).value(value);
				}
				return this;
			}
		}


	}

}
