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
package org.apache.wicket.examples.upload;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.resource.IUploadsFileManager;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;

/**
 * Upload example.
 *
 * @author reiern70
 */
public class UploadToResourcePage extends WicketExamplePage
{
	private static class UpdateInEachAjaxRequestBehavior extends Behavior
	{

		@Override
		public void bind(Component component)
		{
			component.setOutputMarkupPlaceholderTag(true);
		}

		@Override
		public void onEvent(Component component, IEvent<?> event)
		{
			if (event.getPayload() instanceof AjaxRequestTarget)
			{
				((AjaxRequestTarget)event.getPayload()).add(component);
			}
		}
	}

	private final FeedbackPanel uploadFeedback;

	private final FileUploadToResourceField fileUploadToResourceField;

	private boolean uploading;

	private int counter;

	private final IModel<Boolean> allowToLeavePageWhileUploading = Model.of(true);

	/**
	 * Constructor.
	 *
	 * @param parameters
	 *            Page parameters
	 */
	public UploadToResourcePage(final PageParameters parameters)
	{
		fileUploadToResourceField = new FileUploadToResourceField("fileInput")
		{

			private void updateFeedback(AjaxRequestTarget target)
			{
				target.add(uploadFeedback);
				uploading = false;
			}

			@Override
			protected void onUploadSuccess(AjaxRequestTarget target, List<UploadInfo> infos)
			{
				for (UploadInfo uploadInfo : infos) {
					info("File " + uploadInfo.getClientFileName() + " successfully uploaded. Size: " + uploadInfo.getSize() + "bytes. ContentType: "
							+ uploadInfo.getContentType() +". Stored at " +  uploadInfo.getFile().getAbsolutePath());
				}
				updateFeedback(target);
			}

			@Override
			protected void onUploadFailure(AjaxRequestTarget target, String errorInfo)
			{
				updateFeedback(target);
			}

			@Override
			public void startUpload(IPartialPageRequestHandler target) {
				if (allowToLeavePageWhileUploading.getObject())
				{
					target.prependJavaScript("Wicket.CurrentUpload = {};");
				}
				super.startUpload(target);
			}

			@Override
			protected void onUploadCanceled(AjaxRequestTarget target)
			{
				info("You have canceled the upload!");
				updateFeedback(target);
			}

			@Override
			protected CharSequence getClientSideSuccessCallBack() {
				return "function() {Wicket.CurrentUpload = null}";
			}

			@Override
			protected CharSequence getClientSideUploadErrorCallBack()
			{
				return "function() {Wicket.CurrentUpload = null}";
			}

			@Override
			protected String getClientSideCancelCallBack()
			{
				return "function() {Wicket.CurrentUpload = null}";
			}

			@Override
			protected IUploadsFileManager fileManager()
			{
				return getUploadsFileManager();
			}
		};
		fileUploadToResourceField.setMaxSize(Bytes.kilobytes(100));
		fileUploadToResourceField.setFileMaxSize(Bytes.kilobytes(90));
		fileUploadToResourceField.setFileCountMax(5L);
		add(fileUploadToResourceField);

		Form<Void> form = new Form<>("form");
		add(form);
		form.add(new AjaxCheckBox("allowToLeavePageWhileUploading", allowToLeavePageWhileUploading) {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {

			}
		});
		final UploadProgressBar uploadProgressBar = new UploadProgressBar("progress", fileUploadToResourceField)
		{

			@Override
			protected String getOnProgressUpdatedCallBack()
			{
				return "function(percent) { console.log(percent);}";
			}
		};
		add(uploadProgressBar);
		add(new AjaxLink<Void>("upload")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				uploading = true;
				fileUploadToResourceField.startUpload(target);
				uploadProgressBar.start(target);
				target.add(uploadFeedback);
			}

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(!uploading);
			}
		}.add(new UpdateInEachAjaxRequestBehavior()));

		add(new AjaxLink<Void>("cancelUpload")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				fileUploadToResourceField.cancelUpload(target);
				target.add(uploadFeedback);
			}

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(uploading);
			}
		}.add(new UpdateInEachAjaxRequestBehavior()));


		add(new WebMarkupContainer("cancelUploadClientSide")
		{

			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				response.render(OnEventHeaderItem.forComponent(this, "click", fileUploadToResourceField.getTriggerCancelUploadScript()));
			}

			@Override
			protected void onConfigure()
			{
				super.onConfigure();
				setVisible(uploading);
			}

		}.add(new UpdateInEachAjaxRequestBehavior()));

		add(new AjaxLink<Void>("counter")
		{

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				counter ++;
				target.add(this);
			}

		}.setBody(()-> "Click me: upload does not blocks normal wicket AJAX. I was clicked " + counter).setOutputMarkupId(true));
		// Create feedback panels
		uploadFeedback = new FeedbackPanel("uploadFeedback");
		uploadFeedback.setOutputMarkupId(true);

		// Add uploadFeedback to the page itself
		add(uploadFeedback);

	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(
				OnDomReadyHeaderItem.forScript("$(window).bind('beforeunload', function(){ if (Wicket.CurrentUpload != null) { return 'leave?'; }});"));
	}

	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		uploading = false;
	}

	private IUploadsFileManager getUploadsFileManager()
	{
		return UploadApplication.getInstance().getUploadsFileManager();
	}
}
