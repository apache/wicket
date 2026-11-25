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

import java.time.Duration;

import org.apache.wicket.Page;
import org.apache.wicket.examples.WicketExampleApplication;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable.HmacGrantSigner;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable.ResumableUploadResourceReference;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadResourceReference;
import org.apache.wicket.markup.html.form.upload.resource.FolderUploadsFileManager;
import org.apache.wicket.markup.html.form.upload.resource.IUploadsFileManager;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;


/**
 * Application class for org.apache.wicket.examples.upload example.
 * 
 * @author Eelco Hillenius
 */
public class UploadApplication extends WicketExampleApplication
{
	private Folder uploadFolder = null;

	private IUploadsFileManager uploadsFileManager;

	private final VisitorUploadsTracker uploadsTracker = new VisitorUploadsTracker();

	@Override
	public Class<? extends Page> getHomePage()
	{
		return UploadPage.class;
	}

	/**
	 * @return the folder for uploads
	 */
	public Folder getUploadFolder()
	{
		return uploadFolder;
	}

	@Override
	protected void init()
	{
		super.init();

		getResourceSettings().setThrowExceptionOnMissingResource(false);

		uploadFolder = new Folder(System.getProperty("java.io.tmpdir"), "wicket-uploads");
		// Ensure folder exists
		uploadFolder.mkdirs();

		uploadsFileManager = new FolderUploadsFileManager(uploadFolder);

		mountResource("/uploads", FileUploadResourceReference.createNewInstance(uploadsFileManager));
		mountResource("/resumable-uploads",
			new ResumableUploadResourceReference(uploadsFileManager)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected String getCurrentOwner()
				{
					// the visitor, known by a cookie: the uploads outlive the session
					String visitor = UploadVisitor.owner();
					return visitor != null ? visitor : super.getCurrentOwner();
				}
			}
				.setMaxFileSize(Bytes.megabytes(200))
				.setMaxChunkSize(Bytes.megabytes(1))
				// a key of the application rather than one of each session: a page goes on with
				// the uploads of its owner after the session that rendered it ended
				.setGrantSigner(HmacGrantSigner.random())
				// kept a day for the visitor, and listed on the page
				.setOwnerExpiry(Duration.ofDays(1))
				.setUploadsTracker(uploadsTracker));

		mountPage("/multi", MultiUploadPage.class);
		mountPage("/single", UploadPage.class);
		mountPage("/uploadToResource", UploadToResourcePage.class);
		mountPage("/resumableUpload", ResumableUploadPage.class);

		getApplicationSettings().setUploadProgressUpdatesEnabled(true);
	}

	public IUploadsFileManager getUploadsFileManager() {
		return uploadsFileManager;
	}

	/**
	 * @return what keeps track of the resumable uploads of the visitors
	 */
	public VisitorUploadsTracker getUploadsTracker()
	{
		return uploadsTracker;
	}

	public static UploadApplication getInstance() {
		return (UploadApplication)get();
	}
}
