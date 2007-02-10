package wicket.contrib.scriptaculous.dragdrop;

import wicket.Application;
import wicket.MarkupContainer;
import wicket.contrib.scriptaculous.Scriptaculous;
import wicket.contrib.scriptaculous.autocomplete.AutocompleteTextFieldSupport;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.image.Image;
import wicket.markup.html.internal.HtmlHeaderContainer;

public class DraggableImage extends Image implements IHeaderContributor
{
	private final String id;

	public DraggableImage(String wicketId, String id, String img)
	{
		super(wicketId, img);
		this.id = id;
	}

	protected void onRender(MarkupStream markupStream)
	{
		super.onRender(markupStream);

		getResponse().write("\n<script type='text/javascript'>new Draggable('");
		getResponse().write(id);
		getResponse().write("', {revert:true})</script>");
	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		
		tag.put("id", id);
		tag.put("class", getId());
	}

	public void renderHead(HtmlHeaderContainer container)
	{
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

	public String getBodyOnLoad()
	{
		return null;
	}
}
