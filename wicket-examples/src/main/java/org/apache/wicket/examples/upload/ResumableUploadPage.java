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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable.ResumableUploadToResourceField;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable.TrackedUpload;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnEventHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.resource.FileDropZoneBehavior;
import org.apache.wicket.markup.html.form.upload.resource.FileListBehavior;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.lang.Bytes;

/**
 * Uploads files in chunks that can be paused, resumed and canceled, and that go on after a broken
 * connection or a reload of the page.
 */
public class ResumableUploadPage extends WicketExamplePage
{
	private static final long serialVersionUID = 1L;

	/** The part of the application the uploads of the page belong to. */
	static final String CONTEXT = "resumable-upload-example";

	private final FeedbackPanel feedback;

	private final List<String> received = new ArrayList<>();

	private final WebMarkupContainer receivedList;

	private final WebMarkupContainer trackedList;

	/**
	 * Constructor.
	 */
	public ResumableUploadPage()
	{
		feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		receivedList = new WebMarkupContainer("received");
		receivedList.setOutputMarkupId(true);
		add(receivedList);
		receivedList.add(new ListView<>("files", received)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> item)
			{
				item.add(new Label("file", item.getModel()));
			}
		});

		trackedList = new WebMarkupContainer("tracked");
		trackedList.setOutputMarkupId(true);
		add(trackedList);
		trackedList.add(new ListView<>("uploads",
			LoadableDetachableModel.of(ResumableUploadPage::uploadsOfVisitor))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<TrackedUpload> item)
			{
				TrackedUpload upload = item.getModelObject();
				item.add(new Label("name", upload.clientFileName()));
				item.add(new Label("state", upload.isStored() ? "stored, not taken yet"
					: Bytes.bytes(upload.offset()) + " of " + Bytes.bytes(upload.length()) +
						": choose the file again to go on"));
				item.add(new AjaxLink<Void>("take")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target)
					{
						// a step of a wizard would take the file this way, from the files stored
						received.add(0, upload.clientFileName() + ": taken from your uploads, at " +
							UploadApplication.getInstance()
								.getUploadsFileManager()
								.getFile(upload.storedFieldId(), upload.storedName())
								.getAbsolutePath());
						tracker().delete(upload.context(), upload.owner(), upload.id());
						target.add(receivedList, trackedList);
					}
				}.setVisible(upload.isStored()));
			}
		});

		ResumableUploadToResourceField field = new ResumableUploadToResourceField("files")
		{
			private static final long serialVersionUID = 1L;


			@Override
			protected void onUploadSuccess(AjaxRequestTarget target, List<UploadInfo> infos)
			{
				for (UploadInfo info : infos)
				{
					received.add(0, info.getClientFileName() + ": " + Bytes.bytes(info.getSize()) +
						", " + info.getContentType() + ". Stored at " +
						info.getFile().getAbsolutePath());
				}
				String owner = UploadVisitor.owner();
				if (owner != null)
				{
					tracker().takeAllOf(owner, getMarkupId());
				}
				target.add(receivedList, trackedList, feedback);
			}

			@Override
			protected void onUploadFailure(AjaxRequestTarget target, String errorInfo)
			{
				target.add(trackedList, feedback);
			}

			@Override
			protected void onUploadCanceled(AjaxRequestTarget target)
			{
				info("You canceled an upload.");
				target.add(trackedList, feedback);
			}

			@Override
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				// the files go as soon as they are chosen, or dropped
				response.render(OnEventHeaderItem.forComponent(this, "change",
					getTriggerUploadScript()));
			}
		};
		// offers every upload of the visitor in the context left unfinished, wherever it started
		field.setUploadContext(CONTEXT);
		field.setFileMaxSize(Bytes.megabytes(200));
		field.setChunkSize(Bytes.megabytes(1));

		WebMarkupContainer dropZone = new WebMarkupContainer("dropZone");
		dropZone.add(new FileDropZoneBehavior(field));
		add(dropZone);
		dropZone.add(field);
		dropZone.add(new WebMarkupContainer("fileList").add(new FileListBehavior(field)));

		add(new AjaxLink<Void>("pause")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				field.pauseUpload(target);
			}
		});
		add(new AjaxLink<Void>("resume")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				field.resumeUpload(target);
			}
		});
		add(new AjaxLink<Void>("cancel")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				field.cancelUpload(target);
			}
		});
	}

	/**
	 * @return the uploads of the visitor, as the server keeps track of them
	 */
	private static List<TrackedUpload> uploadsOfVisitor()
	{
		String owner = UploadVisitor.owner();
		return owner != null ? tracker().getUploads(CONTEXT, owner) : List.of();
	}

	private static VisitorUploadsTracker tracker()
	{
		return UploadApplication.getInstance().getUploadsTracker();
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(
			new CssResourceReference(ResumableUploadPage.class, "ResumableUploadPage.css")));
	}
}
