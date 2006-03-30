/*
 * $Id: Slider.java 5132 2006-03-26 02:13:41 -0800 (Sun, 26 Mar 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-03-26 02:13:41 -0800 (Sun, 26 Mar
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.yui.slider;

import java.util.Map;
import java.util.regex.Pattern;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.IInitializer;
import wicket.behavior.HeaderContributor;
import wicket.extensions.markup.html.yui.AbstractYuiPanel;
import wicket.extensions.util.resource.TextTemplateHeaderContributor;
import wicket.markup.html.PackageResource;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.image.Image;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.IModel;
import wicket.model.PropertyModel;
import wicket.util.collections.MiniMap;

/**
 * Slider component based on the Slider of Yahoo UI Library.
 * 
 * @author Eelco Hillenius
 */
public class Slider extends AbstractYuiPanel
{
	private static final long serialVersionUID = 1L;

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
			// register all javascript files
			PackageResource.bind(application, Slider.class, Pattern.compile(".*\\.js"));
			// images
			PackageResource.bind(application, Slider.class, Pattern.compile(".*\\.gif|.*\\.png"),
					true);
			// and a css
			PackageResource.bind(application, Slider.class, "css/screen.css");
		}
	}

	/**
	 * The id of the background element.
	 */
	private String backgroundElementId;

	/**
	 * The id of the image element.
	 */
	private String imageElementId;

	/**
	 * The JavaScript variable name of the calendar component.
	 */
	private String javaScriptId;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param model
	 *            the model for this component
	 */
	public Slider(String id, IModel model)
	{
		super(id, model);

		add(HeaderContributor.forJavaScript(Slider.class, "slider.js"));
		add(HeaderContributor.forCss(Slider.class, "css/screen.css"));

		IModel variablesModel = new AbstractReadOnlyModel()
		{
			private static final long serialVersionUID = 1L;

			/** cached variables; we only need to fill this once. */
			private Map variables;

			/**
			 * @see wicket.model.AbstractReadOnlyModel#getObject(wicket.Component)
			 */
			public Object getObject(Component component)
			{
				if (variables == null)
				{
					this.variables = new MiniMap(3);
					variables.put("javaScriptId", javaScriptId);
					variables.put("backGroundElementId", backgroundElementId);
					variables.put("imageElementId", imageElementId);
				}
				return variables;
			}
		};

		add(TextTemplateHeaderContributor.forJavaScript(Slider.class, "init.js", variablesModel));

		WebMarkupContainer backgroundElement = new WebMarkupContainer("backgroundElement");
		backgroundElement.add(new AttributeModifier("id", true, new PropertyModel(this,
				"backgroundElementId")));
		add(backgroundElement);

		WebMarkupContainer imageElement = new WebMarkupContainer("imageElement");
		imageElement.add(new AttributeModifier("id", true,
				new PropertyModel(this, "imageElementId")));
		backgroundElement.add(imageElement);

		/* add the thumb img resoruce */
		imageElement.add(new Image("thumb", PackageResource
				.get(Slider.class, "img/horizSlider.png")));

	}

	/**
	 * @see wicket.Component#renderHead(wicket.markup.html.internal.HtmlHeaderContainer)
	 */
	public void renderHead(HtmlHeaderContainer container)
	{
		((WebPage)getPage()).getBodyContainer().addOnLoadModifier("init" + javaScriptId + "();");
		super.renderHead(container);
	}

	/**
	 * Gets backgroundElementId.
	 * 
	 * @return backgroundElementId
	 */
	public final String getBackgroundElementId()
	{
		return backgroundElementId;
	}

	/**
	 * Gets imageElementId.
	 * 
	 * @return imageElementId
	 */
	public final String getImageElementId()
	{
		return imageElementId;
	}

	/**
	 * TODO implement
	 */
	public void updateModel()
	{
	}

	/**
	 * @see wicket.Component#onAttach()
	 */
	protected void onAttach()
	{
		super.onAttach();

		// initialize lazily
		if (backgroundElementId == null)
		{
			// assign the markup id
			String id = getMarkupId();
			backgroundElementId = id + "Bg";
			imageElementId = id + "Img";
			javaScriptId = backgroundElementId + "JS";
		}
	}
}
