package wicket.contrib.scriptaculous.dragdrop;

import wicket.Application;
import wicket.PageParameters;
import wicket.contrib.scriptaculous.Indicator;
import wicket.contrib.scriptaculous.Scriptaculous;
import wicket.contrib.scriptaculous.autocomplete.AutocompleteTextFieldSupport;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.IHeaderContributor;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.internal.HtmlHeaderContainer;

public class DraggableTarget extends WebMarkupContainer implements IHeaderContributor
{
	private final Class pageContribution;
	private String draggableClass;
	private String indicatorId;

	public DraggableTarget(String id, Class pageContribution)
	{
		super(id);
		this.pageContribution = pageContribution;
	}

	public void accepts(DraggableImage image)
	{
		this.draggableClass = image.getId();
	}

	public void setIndicator(Indicator indicator)
	{
		this.indicatorId = indicator.getId();
	}

	protected void onRender(MarkupStream markupStream)
	{
		super.onRender(markupStream);

		String url = urlFor(null, pageContribution, new PageParameters());

		getResponse().write("\n<script type='text/javascript'>new Ajax.Updater('");
		getResponse().write(getId());
		getResponse().write("', '");
		getResponse().write(url);
		getResponse().write("', {evalScripts:true, asynchronous:true})</script>\n");
		getResponse().write("<script type='text/javascript'>Droppables.add('");
		getResponse().write(getId());
		getResponse().write("', {accept:'");
		getResponse().write(draggableClass);
		getResponse().write("', onDrop:function(element){ new Ajax.Updater('");
		getResponse().write(getId());
		getResponse().write("', '");
		getResponse().write(url);
		getResponse().write("', {");

		if (null != indicatorId)
		{
			getResponse().write("onLoading:function(request){ Element.show('indicator')}, ");
			getResponse().write("onComplete:function(request){Element.hide('indicator')}, ");
		}

		getResponse().write("parameters:'id=' + encodeURIComponent(element.id), evalScripts:true, asynchronous:true})}, ");
		getResponse().write(" hoverclass:'");
		getResponse().write(getId());
		getResponse().write("-active'})</script>\n");
	}

	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		tag.put("id", getId());
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
		container.getResponse().write("\t<link rel='stylesheet' type='text/css' href='" + urlFor(ref.getPath()) + "'/>\n");
	}

	public String getBodyOnLoad()
	{
		return null;
	}
}
