/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.extensions.ajax.markup.html.form.upload;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IInitializer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel to show the progress of an HTTP upload.
 * <p>
 * NB: For this to work, you *must* use an {@link UploadWebRequest}. See the
 * javadoc in that class for details.
 * 
 * @author Andrew Lombardi
 */
public class UploadProgressBar extends Panel
{
	private static final Logger log = LoggerFactory.getLogger(UploadProgressBar.class);

	/**
	 * Initializer for this component; binds static resources.
	 */
	public final static class ComponentInitializer implements IInitializer
	{
		/**
		 * @see org.apache.wicket.IInitializer#init(org.apache.wicket.Application)
		 */
		public void init(Application application)
		{
			// register the upload status resource
			Application.get().getSharedResources().add(RESOURCE_NAME, new UploadStatusResource());
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "Ajax UploadProgressBar initializer";
		}
	}

	private static final ResourceReference JS = new ResourceReference(
			UploadProgressBar.class, "progressbar.js");

	private static final ResourceReference CSS = new ResourceReference(
			UploadProgressBar.class, "UploadProgressBar.css");

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

		add(HeaderContributor.forJavaScript(JS));
		ResourceReference css = getCss();
		if (css != null)
		{
			add(HeaderContributor.forCss(css));
		}

		final WebMarkupContainer barDiv = new WebMarkupContainer("bar");
		barDiv.setOutputMarkupId(true);
		add(barDiv);

		final WebMarkupContainer statusDiv = new WebMarkupContainer("status");
		statusDiv.setOutputMarkupId(true);
		add(statusDiv);

		if (!(RequestCycle.get().getRequest() instanceof UploadWebRequest))
		{
			log.warn("UploadProgressBar will not work without an UploadWebRequest. See the javadoc for details.");
		}
		
		form.add(new AttributeModifier("onsubmit", true, new Model()
		{

			private static final long serialVersionUID = 1L;


			public Object getObject()
			{
				ResourceReference ref = new ResourceReference(RESOURCE_NAME);

				return "var def=new wupb.Def('" + form.getMarkupId() + "', '"
						+ statusDiv.getMarkupId() + "', '" + barDiv.getMarkupId() + "', '"
						+ getPage().urlFor(ref) + "'); wupb.start(def); return false;";
			}
		}));
	}
	
	/**
	 * Override this to provide your own CSS, or return null to avoid including the default.
	 * 
	 * @return ResourceReference for your CSS.
	 */
	protected ResourceReference getCss()
	{
		return CSS;
	}

}
