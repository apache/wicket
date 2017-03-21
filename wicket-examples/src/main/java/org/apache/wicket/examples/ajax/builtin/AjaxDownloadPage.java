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

import java.util.concurrent.TimeUnit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.AjaxDownload;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.time.Duration;

/**
 * Ajax download.
 * 
 * @author svenmeier
 */
public class AjaxDownloadPage extends BasePage
{
	private WebMarkupContainer downloadingContainer;

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

		initDownloadInNewWindow();

		initDownloadInSameWindow();

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
		
		final AjaxDownload download = new AjaxDownload(resource) {
			
			@Override
			protected void onBeforeDownload(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(true);
				target.add(downloadingContainer);
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
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}
	
	private void initDownloadReference()
	{
		ResourceReference reference = new ResourceReference("referenceToResource") {
			@Override
			public IResource getResource()
			{
				return new StaticResource();
			}
		};
		
		final AjaxDownload download = new AjaxDownload(reference) {
			
			@Override
			protected void onBeforeDownload(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(true);
				target.add(downloadingContainer);
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

		final AjaxDownload download = new AjaxDownload(resource) {

			@Override
			protected void onBeforeDownload(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(true);
				target.add(downloadingContainer);
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
		download.setLocation(AjaxDownload.Location.NewWindow);
		add(download);

		add(new AjaxLink<Void>("downloadInNewWindow")
		{
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

		final AjaxDownload download = new AjaxDownload(resource) {

			@Override
			protected void onBeforeDownload(AjaxRequestTarget target)
			{
				downloadingContainer.setVisible(true);
				target.add(downloadingContainer);
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
		download.setLocation(AjaxDownload.Location.SameWindow);
		add(download);

		add(new AjaxLink<Void>("downloadInSameWindow")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		});
	}

	public static class StaticResource extends ResourceStreamResource {

		StaticResource() {
			setFileName("File-from-ResourceReference");
			setContentDisposition(ContentDisposition.ATTACHMENT);
			setCacheDuration(Duration.NONE);
		}
		
		@Override
		public void respond(Attributes attributes)
		{
			AjaxDownload.markCompleted(attributes);
			
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
	
	private class ExampleResource extends ResourceStreamResource {
		
		private String content;
		
		private int count = 0;

		public ExampleResource(String content)
		{
			this.content = content;

			setFileName("File-from-IResource.txt");
			setCacheDuration(Duration.NONE);
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

			return new StringResourceStream(content);
		};

	}
}