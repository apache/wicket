package org.apache.wicket.extensions.ajax.markup.html.modal;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;

/**
 * References for {@link ModalDialog}
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class ModalDialogReferences
{
	public static final ResourceReference CSS = new CssResourceReference(
		ModalDialogReferences.class, "ModalDialog.css");

	public static final ResourceReference CSS_SKIN = new CssResourceReference(
		ModalDialogReferences.class, "ModalDialog-skin.css");


	public static final ResourceReference JS = new JQueryPluginResourceReference(
		ModalDialogReferences.class, "ModalDialog.js")
	{
		@Override
		public List<HeaderItem> getDependencies()
		{
			List<HeaderItem> deps = super.getDependencies();
			deps.add(CssHeaderItem.forReference(CSS));
			return deps;
		}
	};


}
