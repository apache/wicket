package wicket.extensions.ajax.markup.html.form.upload;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.IInitializer;
import wicket.MarkupContainer;
import wicket.ResourceReference;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.JavaScriptReference;
import wicket.model.Model;

/**
 * ProgressbarPanel.
 * 
 * @author Andrew Lombardi
 */
public class UploadProgressBar extends Panel
{
	/**
	 * Initializer for this component; binds static resources.
	 */
	public final static class ComponentInitializer implements IInitializer
	{
		/**
		 * @see wicket.IInitializer#init(wicket.Application)
		 */
		public void init(Application application)
		{
			// register the upload status resource
			Application.get().getSharedResources().add(RESOURCE_NAME, new UploadStatusResource());
		}
	}

	private static final PackageResourceReference JS_PROGRESSBAR = new PackageResourceReference(
			UploadProgressBar.class, "progressbar.js");

	private static final String RESOURCE_NAME = UploadProgressBar.class.getName();

	private static final long serialVersionUID = 1L;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public UploadProgressBar(MarkupContainer parent, final String id, final Form form)
	{
		super(parent, id);
		setOutputMarkupId(true);
		form.setOutputMarkupId(true);
		setRenderBodyOnly(true);

		new JavaScriptReference(this, "javascript", JS_PROGRESSBAR);

		final WebMarkupContainer barDiv = new WebMarkupContainer(this, "bar");
		barDiv.setOutputMarkupId(true);

		final WebMarkupContainer statusDiv = new WebMarkupContainer(this, "status");
		statusDiv.setOutputMarkupId(true);

		form.add(new AttributeModifier("onsubmit", true, new Model<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				ResourceReference ref = new ResourceReference(RESOURCE_NAME);

				return "var def=new wupb.Def('" + form.getMarkupId() + "', '"
						+ statusDiv.getMarkupId() + "', '" + barDiv.getMarkupId() + "', '"
						+ getPage().urlFor(ref) + "'); wupb.start(def); return false;";
			}
		}));
	}
}
