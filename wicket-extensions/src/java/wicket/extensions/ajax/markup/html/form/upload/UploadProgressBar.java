package wicket.extensions.ajax.markup.html.form.upload;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.IInitializer;
import wicket.RequestCycle;
import wicket.ResourceReference;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.behavior.AbstractAjaxBehavior;
import wicket.markup.html.PackageResource;
import wicket.markup.html.PackageResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.collections.MiniMap;
import wicket.util.io.Streams;
import wicket.util.string.interpolator.MapVariableInterpolator;

/**
 * ProgressbarPanel
 * 
 * @author Andrew Lombardi
 */
public class UploadProgressBar extends Panel
{

	private static final String RESOURCE_NAME = UploadProgressBar.class.getName();

	private static final long serialVersionUID = 1L;

	private static final PackageResourceReference GIF_PROGRESSBAR = new PackageResourceReference(
			UploadProgressBar.class, "progress-bar.gif");

	private static final PackageResourceReference GIF_PROGRESSBAR_REMAINDER = new PackageResourceReference(
			UploadProgressBar.class, "progress-remainder.gif");


	private final Form form;


	/**
	 * @param id
	 * @param form
	 */
	public UploadProgressBar(String id, final Form form)
	{
		super(id);
		this.form = form; // ?still needed
		setOutputMarkupId(true);
		form.setOutputMarkupId(true);
		setRenderBodyOnly(true);

		final WebMarkupContainer barDiv=new WebMarkupContainer("bar");
		barDiv.setOutputMarkupId(true);
		add(barDiv);

		final WebMarkupContainer statusDiv=new WebMarkupContainer("status");
		statusDiv.setOutputMarkupId(true);
		add(statusDiv);

		add(new ProgressbarScriptIncluder());

		ScriptLabel progressBarCSS = new ScriptLabel("progressBarCSS", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.model.IModel#getObject(wicket.Component)
			 */
			public Object getObject(Component component)
			{
				return getCSSComponentInitializationScript();
			}
		});
		add(progressBarCSS);

		ScriptLabel progressBarJS = new ScriptLabel("progressBarJS", new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.model.IModel#getObject(wicket.Component)
			 */
			public Object getObject(Component component)
			{
				String javascriptFile = getPackagedTextFileContents("progressbar.js");
				Map variables = new HashMap();

				ResourceReference ref = new ResourceReference(RESOURCE_NAME);
				String statusUrl = getPage().urlFor(ref);

				variables.put("bar-id", barDiv.getMarkupId());
				variables.put("status-id", statusDiv.getMarkupId());
				
				variables.put("statusUrl", statusUrl);
				variables.put("form-id", form.getMarkupId());
				MapVariableInterpolator interpolator = new MapVariableInterpolator(javascriptFile,
						variables);
				return "<script>"+interpolator.toString()+"</script>";
			}
		});
		add(progressBarJS);

		
		
		form.add(new AttributeModifier("onsubmit", true, new Model("wupb.start();")));
	}

	/**
	 * Label for outputting script contents
	 * 
	 * @author ivaynberg
	 */
	private static class ScriptLabel extends Label
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param id
		 * @param model
		 */
		public ScriptLabel(String id, IModel model)
		{
			super(id, model);
			setEscapeModelStrings(false);
			setRenderBodyOnly(true);
		}

	}

	/**
	 * Initializes the .css file with the correct images
	 * 
	 * @return the css file
	 */
	protected String getCSSComponentInitializationScript()
	{
		String cssFile = getPackagedTextFileContents("progressbar.css");
		Map variables = new MiniMap(2);

		final String progressbarUrl = RequestCycle.get().urlFor(GIF_PROGRESSBAR);
		final String progressbarRemainderUrl = RequestCycle.get().urlFor(GIF_PROGRESSBAR_REMAINDER);

		variables.put("progressbar", progressbarUrl);
		variables.put("progressbarRemainder", progressbarRemainderUrl);
		MapVariableInterpolator interpolator = new MapVariableInterpolator(cssFile, variables);
		return interpolator.toString();
	}



	private String getPackagedTextFileContents(String fileName)
	{
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		if (inputStream == null)
		{
			throw new IllegalArgumentException("file " + fileName + " was not found; requested by "
					+ getClass());
		}

		try
		{
			return Streams.readString(inputStream);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Javascript contributor
	 * 
	 * @author ivaynberg
	 * 
	 */
	private static final class ProgressbarScriptIncluder extends AbstractAjaxBehavior
	{

		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.behavior.AbstractAjaxBehavior#getImplementationId()
		 */
		protected String getImplementationId()
		{
			return "wicket-progressbar";
		}

		/**
		 * @see wicket.behavior.AbstractAjaxBehavior#onRenderHeadInitContribution(wicket.Response)
		 */
		protected void onRenderHeadInitContribution(final Response response)
		{
			//FIXME add javascript?
		}


		/**
		 * @see wicket.behavior.IBehaviorListener#onRequest()
		 */
		public void onRequest()
		{
		}
	}

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
			PackageResource.bind(application, ComponentInitializer.class,
					PackageResource.EXTENSION_JS);
			PackageResource.bind(application, ComponentInitializer.class, "progress-bar.gif");
			PackageResource.bind(application, ComponentInitializer.class, "progress-remainder.gif");

			// register the upload status resource
			Application.get().getSharedResources().add(RESOURCE_NAME, new UploadStatusResource());
		}
	}

}
