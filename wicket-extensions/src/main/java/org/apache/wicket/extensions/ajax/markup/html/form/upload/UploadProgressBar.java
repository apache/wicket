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
import org.apache.wicket.Component;
import org.apache.wicket.IInitializer;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A panel to show the progress of an HTTP upload.
 * <p>
 * NB: For this to work, you *must* use an {@link UploadWebRequest}. See the javadoc in that class
 * for details.
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
		@Override
		public String toString()
		{
			return "Ajax UploadProgressBar initializer";
		}
	}

	private static final ResourceReference JS = new PackageResourceReference(
		UploadProgressBar.class, "progressbar.js");

	private static final ResourceReference CSS = new PackageResourceReference(
		UploadProgressBar.class, "UploadProgressBar.css");

	private static final String RESOURCE_NAME = UploadProgressBar.class.getName();

	private static final long serialVersionUID = 1L;

	private final Form<?> form;

	/**
	 * Constructor that will display the upload progress bar for every submit of the given form.
	 * 
	 * @param id
	 *            component id (not null)
	 * @param form
	 *            form that will be submitted (not null)
	 */
	public UploadProgressBar(String id, final Form<?> form)
	{
		this(id, form, null);
	}

	/**
	 * Constructor that will display the upload progress bar for submissions of the given form, that
	 * include a file upload in the given file upload field; i.e. if the user did not select a file
	 * in the given file upload field, the progess bar is not displayed.
	 * 
	 * @param id
	 *            component id (not null)
	 * @param form
	 *            form that is submitted (not null)
	 * @param fileUploadField
	 *            the file upload field to check for a file upload, or null to display the upload
	 *            field for every submit of the given form
	 */
	public UploadProgressBar(String id, final Form<?> form, FileUploadField fileUploadField)
	{
		super(id);
		this.form = form;
		form.setOutputMarkupId(true);
		if (fileUploadField != null)
		{
			fileUploadField.setOutputMarkupId(true);
		}
		setRenderBodyOnly(true);

		final WebMarkupContainer barDiv = new WebMarkupContainer("bar");
		barDiv.setOutputMarkupId(true);
		add(barDiv);

		final WebMarkupContainer statusDiv = new WebMarkupContainer("status");
		statusDiv.setOutputMarkupId(true);
		add(statusDiv);

		if (!(RequestCycle.get().getRequest() instanceof UploadWebRequest) &&
			!(RequestCycle.get().getRequest() instanceof MultipartRequest))
		{
			log.warn("UploadProgressBar will not work without an UploadWebRequest. See the javadoc for details.");
		}

		form.add(new FormEnabler(this, statusDiv, barDiv, fileUploadField));
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

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.renderJavascriptReference(JS);
		ResourceReference css = getCss();
		if (css != null)
		{
			response.renderCSSReference(css);
		}
	}

	/** {@inheritDoc} */
	@Override
	protected void onRemove()
	{
		// remove formenabler we added to the form
		for (IBehavior behavior : form.getBehaviors())
		{
			if (behavior instanceof FormEnabler)
			{
				if (((FormEnabler)behavior).getUploadProgressBar() == this)
				{
					form.remove(behavior);
					break;
				}
			}
		}
		super.onRemove();
	}

	/**
	 * Hooks into form onsubmit and triggers the progress bar updates
	 * 
	 * @author igor.vaynberg
	 */
	private static class FormEnabler extends AbstractBehavior
	{
		private static final long serialVersionUID = 1L;

		private final Component status, bar, uploadField;
		private final UploadProgressBar pbar;

		public FormEnabler(UploadProgressBar pbar, Component status, Component bar,
			Component uploadField)
		{
			this.pbar = pbar;
			this.bar = bar;
			this.status = status;
			this.uploadField = uploadField;
		}

		@Override
		public void onComponentTag(Component component, ComponentTag tag)
		{
			ResourceReference ref = new SharedResourceReference(RESOURCE_NAME);
			final String uploadFieldId = (uploadField == null) ? "" : uploadField.getMarkupId();
			tag.put("onsubmit", "var def=new Wicket.WUPB.Def('" + component.getMarkupId() + "', '" +
				status.getMarkupId() + "', '" + bar.getMarkupId() + "', '" +
				component.getPage().urlFor(ref) + "','" + uploadFieldId +
				"'); Wicket.WUPB.start(def);");
		}

		public UploadProgressBar getUploadProgressBar()
		{
			return pbar;
		}


	}
}
