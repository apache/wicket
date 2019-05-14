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
	/** Contains layout css for the modal, is included automatically */
	public static final ResourceReference CSS = new CssResourceReference(
		ModalDialogReferences.class, "ModalDialog.css");

	/**
	 * Contains visual look and feel of the modal, this css resource is not included automatically
	 * since applications will most likely want to provide their own look and feel. This resource
	 * here is an example only. It can be included manually to receive the default look and feel for
	 * the modal.
	 */
	public static final ResourceReference CSS_SKIN = new CssResourceReference(
		ModalDialogReferences.class, "ModalDialog-skin.css");


	/** Contains javascript that implements modal behavior, is included automatically */
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
