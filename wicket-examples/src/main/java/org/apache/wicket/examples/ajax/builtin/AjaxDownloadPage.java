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
package org.apache.wicket.examples.ajax.builtin;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior.Location;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

/**
 * Ajax download.
 *
 * @author svenmeier
 */
public class AjaxDownloadPage extends BasePage
{
	private static final long serialVersionUID = 1L;
	private final WebMarkupContainer downloadingContainer;

	private IModel<String> text;

	/**
	 * Constructor
	 */
	public AjaxDownloadPage()
	{
		downloadingContainer = new WebMarkupContainer("downloading");
		downloadingContainer.setOutputMarkupPlaceholderTag(true);
		downloadingContainer.setVisible(false);
		add(downloadingContainer);

		initDownload();

		initDownloadInIframe();

		initDownloadInNewWindow();

		initDownloadInSameWindow();

		initDynamicDownload();

		initDownloadReference();
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();

		// download cannot continue on page refresh
		downloadingContainer.setVisible(false);
	}

	private void initDownload()
	{
		IResource resource = new ExampleResource("downloaded via ajax")
			.setContentDisposition(ContentDisposition.ATTACHMENT);

		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(resource) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}
		};
		add(download);

		add(new AjaxLink<Void>("download")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}

	private void initDownloadInIframe()
	{
		IResource resource = new ExampleResource("downloaded via ajax in iframe")
			.setContentDisposition(ContentDisposition.ATTACHMENT);

		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(resource)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}
		};
		download.setLocation(Location.IFrame);
		add(download);

		add(new AjaxLink<Void>("downloadIframe")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}

	private void initDownloadReference()
	{
		ResourceReference reference = new ResourceReference("referenceToResource")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public IResource getResource()
			{
				return new StaticResource();
			}
		};

		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(reference)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}
		};
		add(download);

		add(new AjaxLink<Void>("downloadReference")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}

	private void initDownloadInNewWindow()
	{
		IResource resource = new ExampleResource("downloaded via ajax in a new browser window")
			.setContentDisposition(ContentDisposition.INLINE);

		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(resource)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}

			@Override
			protected void onDownloadCompleted(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}
		};
		download.setLocation(AjaxDownloadBehavior.Location.NewWindow);
		add(download);

		add(new AjaxLink<Void>("downloadInNewWindow")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}

	private void initDownloadInSameWindow()
	{
		IResource resource = new ExampleResource("downloaded via ajax in same browser window")
			.setContentDisposition(ContentDisposition.ATTACHMENT);

		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(resource)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}

			@Override
			protected void onDownloadCompleted(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}
		};
		download.setLocation(AjaxDownloadBehavior.Location.SameWindow);
		add(download);

		add(new AjaxLink<Void>("downloadInSameWindow")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}

	private void initDynamicDownload()
	{
		final AjaxDownloadBehavior download = new AjaxDownloadBehavior(DynamicTextFileResource.instance)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onBeforeDownload(IPartialPageRequestHandler handler)
			{
				downloadingContainer.setVisible(true);
				handler.add(downloadingContainer);
			}

			@Override
			protected void onDownloadSuccess(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}

			@Override
			protected void onDownloadFailed(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);

				target.appendJavaScript("alert('Download failed');");
			}

			@Override
			protected void onDownloadCompleted(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(false);
				target.add(downloadingContainer);
			}
		};
		add(download);
		download.setLocation(Location.Blob);
		text = Model.of("");
		Form<Void> form = new Form<>("form");
		add(form);
		final TextArea<String> stringTextArea =new TextArea<>("text", text);
		stringTextArea.setOutputMarkupId(true);
		form.add(stringTextArea);
		form.add(new AjaxSubmitLink("downloadDynamicContents")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				download.initiate(target, DynamicTextFileResource.encodeText(text.getObject()));
				text.setObject("");
				target.add(stringTextArea);
			}
		});
	}

	public static class StaticResource extends ResourceStreamResource
	{
		private static final long serialVersionUID = 1L;

		StaticResource() {
			setFileName("File-from-ResourceReference");
			setContentDisposition(ContentDisposition.ATTACHMENT);
			setCacheDuration(Duration.ZERO);
		}

		@Override
		public void respond(Attributes attributes)
		{
			AjaxDownloadBehavior.markCompleted(attributes);

			super.respond(attributes);
		}

		@Override
		protected IResourceStream getResourceStream(Attributes attributes)
		{
			// simulate delay
			try
			{
				TimeUnit.MILLISECONDS.sleep(5000);
			}
			catch (InterruptedException e)
			{
			}

			return new StringResourceStream("downloaded via ajax with resource reference");
		}
	}

	private static class ExampleResource extends ResourceStreamResource
	{
		private static final long serialVersionUID = 1L;

		private String content;

		private int count = 0;

		public ExampleResource()
		{
			setFileName("Dynamic-File-from-IResource.txt");
			setCacheDuration(Duration.ZERO);
		}

		public ExampleResource(String content)
		{
			this.content = content;

			setFileName("File-from-IResource.txt");
			setCacheDuration(Duration.ZERO);
		}

		@Override
		protected IResourceStream getResourceStream(Attributes attributes) {
			// simulate delay
			try
			{
				TimeUnit.MILLISECONDS.sleep(3000);
			}
			catch (InterruptedException e)
			{
			}

			count++;
			if (count == 3) {
				count = 0;
				throw new AbortWithHttpErrorCodeException(400);
			}

			return new StringResourceStream(getContent(attributes));
		}

		protected String getContent(Attributes attributes) {
			return content;
		}

	}

	public static class DynamicTextFileResource extends ResourceReference {

		static final String FILE_CONTENTS = "fileContents";

		public static DynamicTextFileResource instance = new DynamicTextFileResource();

		public DynamicTextFileResource() {
			super(AjaxDownloadPage.class, "DynamicTextFileResource");
		}

		public static PageParameters encodeText(String  text) {
			PageParameters parameters = new PageParameters();
			parameters.add(FILE_CONTENTS, text);
			return parameters;
		}

		@Override
		public IResource getResource() {
			return new ExampleResource() {
				@Override
				protected String getContent(Attributes attributes) {
					String licence = "/*\n" +
							" * Licensed to the Apache Software Foundation (ASF) under one or more\n" +
							" * contributor license agreements.  See the NOTICE file distributed with\n" +
							" * this work for additional information regarding copyright ownership.\n" +
							" * The ASF licenses this file to You under the Apache License, Version 2.0\n" +
							" * (the \"License\"); you may not use this file except in compliance with\n" +
							" * the License.  You may obtain a copy of the License at\n" +
							" *\n" +
							" *      http://www.apache.org/licenses/LICENSE-2.0\n" +
							" *\n" +
							" * Unless required by applicable law or agreed to in writing, software\n" +
							" * distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
							" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
							" * See the License for the specific language governing permissions and\n" +
							" * limitations under the License.\n" +
							" */ \n\n\n";
					return licence + attributes.getParameters().get(FILE_CONTENTS).toString("");
				}
			};
		}
	}
}
