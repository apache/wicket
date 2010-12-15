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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.WicketAjaxReference;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WicketEventReference;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
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
public class UploadProgressBar extends Panel implements IHeaderContributor
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

	private static final ResourceReference JS = new ResourceReference(UploadProgressBar.class,
		"progressbar.js");

	private static final ResourceReference CSS = new ResourceReference(UploadProgressBar.class,
		"UploadProgressBar.css");

	private static final String RESOURCE_NAME = UploadProgressBar.class.getName();

	private static final long serialVersionUID = 1L;

	private final Form<?> form;

	private final WebMarkupContainer statusDiv;

	private final WebMarkupContainer barDiv;

	private final FileUploadField uploadField;


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
		uploadField = fileUploadField;
		this.form = form;
		form.setOutputMarkupId(true);
		setRenderBodyOnly(true);

		add(JavascriptPackageResource.getHeaderContribution(WicketEventReference.INSTANCE));
		add(JavascriptPackageResource.getHeaderContribution(WicketAjaxReference.INSTANCE));
		add(JavascriptPackageResource.getHeaderContribution(JS));
		ResourceReference css = getCss();
		if (css != null)
		{
			add(CSSPackageResource.getHeaderContribution(css));
		}

		barDiv = new WebMarkupContainer("bar");
		barDiv.setOutputMarkupId(true);
		add(barDiv);

		statusDiv = new WebMarkupContainer("status");
		statusDiv.setOutputMarkupId(true);
		add(statusDiv);

		if (!(RequestCycle.get().getRequest() instanceof UploadWebRequest) &&
			!(RequestCycle.get().getRequest() instanceof MultipartRequest))
		{
			log.warn("UploadProgressBar will not work without an UploadWebRequest. See the javadoc for details.");
		}
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		form.getRootForm().setOutputMarkupId(true);
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

	/**
	 * 
	 */
	public void renderHead(IHeaderResponse response)
	{
		ResourceReference ref = new ResourceReference(RESOURCE_NAME);

		final String uploadFieldId = (uploadField == null) ? "" : uploadField.getMarkupId();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream js = new PrintStream(out);

		js.printf("var formElement = Wicket.$('%s');", form.getRootForm().getMarkupId());
		js.append("var originalCallback = formElement.onsubmit;");

		js.append("var submitCallback = function() {");
		js.printf("  if (!Wicket.$('%s')) return;", statusDiv.getMarkupId());
		js.printf("  var def=new Wicket.WUPB.Def('%s', '%s', '%s', '%s','%s');", getMarkupId(),
			statusDiv.getMarkupId(), barDiv.getMarkupId(), urlFor(ref), uploadFieldId);

		js.append("  new Wicket.WUPB(def).start();");

		js.append("  if(originalCallback)return originalCallback(); else return true;");
		js.append("};");

		js.append("formElement.onsubmit = submitCallback;");
		js.close();

		response.renderOnDomReadyJavascript(new String(out.toByteArray()));

	}
}
