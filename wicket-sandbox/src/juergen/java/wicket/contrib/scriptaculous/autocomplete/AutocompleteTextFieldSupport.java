package wicket.contrib.scriptaculous.autocomplete;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.contrib.scriptaculous.Scriptaculous;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.form.TextField;
import wicket.markup.html.internal.HtmlHeaderContainer;

/**
 * support class for all autocomplete text fields. handles binding of needed css
 * and javascript.
 * 
 * @author <a href="mailto:wireframe6464@users.sourceforge.net">Ryan Sonnek</a>
 */
public class AutocompleteTextFieldSupport extends TextField implements IHeaderContributor
{

	public AutocompleteTextFieldSupport(String id)
	{
		super(id);
	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("id", getId());
		tag.put("autocomplete", "off");
	}

	public void renderHead(HtmlHeaderContainer container)
	{
		super.renderHead(container);

		Scriptaculous.get().renderHead(container);
		addCssReference(container, AutocompleteTextFieldSupport.class, "style.css");
	}

	private void addCssReference(final HtmlHeaderContainer container, final Class clazz, final String name)
	{
		PackageResourceReference ref = new PackageResourceReference(Application.get(), clazz, name);
		write(container, "\t<link rel='stylesheet' type='text/css' href='" + urlFor(ref.getPath()) + "'/>\n");
	}
	
	/**
	 * Writes the given string to the header container.
	 * 
	 * @param container
	 *            the header container
	 * @param s
	 *            the string to write
	 */
	protected void write(MarkupContainer container, String s)
	{
		container.getResponse().write(s);
	}

	/**
	 * adds a placeholder div where auto completion results will be populated.
	 */
	protected void onRender(MarkupStream markupStream)
	{
		super.onRender(markupStream);

		getResponse().write(
				"<div class='auto_complete' id='" + getAutocompleteId() + "'></div>");
	}

	protected final String getAutocompleteId()
	{
		return getId() + "_autocomplete";
	}

	public String getBodyOnLoad()
	{
		return null;
	}
}
