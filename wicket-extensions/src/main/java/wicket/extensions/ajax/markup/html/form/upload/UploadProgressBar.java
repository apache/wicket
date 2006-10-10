/*
 * $Id$ $Revision$ $Date$
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
package wicket.extensions.ajax.markup.html.form.upload;

import wicket.Application;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.IInitializer;
import wicket.ResourceReference;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.markup.html.resources.JavaScriptReference;
import wicket.model.Model;

/**
 * ProgressbarPanel
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

	private static final ResourceReference JS_PROGRESSBAR = new ResourceReference(
			UploadProgressBar.class, "progressbar.js");

	private static final String RESOURCE_NAME = UploadProgressBar.class.getName();

	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 * @param form
	 */
	public UploadProgressBar(String id, final Form form)
	{
		super(id);
		setOutputMarkupId(true);
		form.setOutputMarkupId(true);
		setRenderBodyOnly(true);

		add(new JavaScriptReference("javascript", JS_PROGRESSBAR));

		final WebMarkupContainer barDiv = new WebMarkupContainer("bar");
		barDiv.setOutputMarkupId(true);
		add(barDiv);

		final WebMarkupContainer statusDiv = new WebMarkupContainer("status");
		statusDiv.setOutputMarkupId(true);
		add(statusDiv);

		form.add(new AttributeModifier("onsubmit", true, new Model()
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;


			public Object getObject(Component component)
			{
				ResourceReference ref = new ResourceReference(RESOURCE_NAME);

				return "var def=new wupb.Def('" + form.getMarkupId() + "', '"
						+ statusDiv.getMarkupId() + "', '" + barDiv.getMarkupId() + "', '"
						+ getPage().urlFor(ref) + "'); wupb.start(def); return false;";
			}
		}));
	}

}
