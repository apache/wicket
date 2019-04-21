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
package org.apache.wicket.markup.html.link;

import java.io.File;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;

/**
 * A link that streams a file to the client. When clicked this link will prompt the save as dialog
 * in the browser.
 * 
 * NOTICE that this link will lock the page. That means only one link from the page can be
 * downloaded at a time, and also while the download happens the page cannot be accessed by other
 * threads. If you need to stream multiple files concurrently without blocking then you should use
 * shared resources or a non-wicket servlet.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class DownloadLink extends Link<File>
{
	private static final long serialVersionUID = 1L;

	/**
	 * The file name that will be used in the response headers.<br/>
	 * Optional. If omitted the name of the provided file will be used.
	 */
	private IModel<String> fileNameModel;

	/**
	 * A flag indicating whether the file should be deleted after download.
	 */
	private boolean deleteAfter;

	/**
	 * The duration for which the file resource should be cached by the browser.
	 * <p>
	 * By default is {@code null} and
	 * {@link org.apache.wicket.settings.ResourceSettings#getDefaultCacheDuration()} is used.
	 * </p>
	 */
	private Duration cacheDuration;

	/**
	 * Constructor. File name used will be the result of <code>file.getName()</code>
	 * 
	 * @param id
	 *            component id
	 * @param file
	 *            file to stream to client
	 */
	public DownloadLink(String id, File file)
	{
		this(id, new Model<File>(Args.notNull(file, "file")));
	}

	/**
	 * Constructor. File name used will be the result of <code>file.getName()</code>
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model that contains the file object
	 */
	public DownloadLink(String id, IModel<File> model)
	{
		this(id, model, (IModel<String>)null);
	}

	/**
	 * Constructor. File name used will be the result of <code>file.getName()</code>
	 * 
	 * @param id
	 *            component id
	 * @param model
	 *            model that contains the file object
	 * @param fileName
	 *            name of the file
	 */
	public DownloadLink(String id, IModel<File> model, String fileName)
	{
		this(id, model, Model.of(fileName));
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            component id
	 * @param file
	 *            file to stream to client
	 * @param fileName
	 *            name of the file
	 */
	public DownloadLink(String id, File file, String fileName)
	{
		this(id, Model.of(Args.notNull(file, "file")), Model.of(fileName));
	}

	/**
	 * Constructor. File name used will be the result of <code>file.getName()</code>
	 * 
	 * @param id
	 *            component id
	 * @param fileModel
	 *            model that contains the file object
	 * @param fileNameModel
	 *            model that provides the file name to use in the response headers
	 */
	public DownloadLink(String id, IModel<File> fileModel, IModel<String> fileNameModel)
	{
		super(id, fileModel);
		this.fileNameModel = wrap(fileNameModel);
	}

	@Override
	public void detachModels()
	{
		super.detachModels();

		if (fileNameModel != null)
		{
			fileNameModel.detach();
		}
	}

	@Override
	public void onClick()
	{
		final File file = getModelObject();
		if (file == null)
		{
			throw new IllegalStateException(getClass().getName() +
				" failed to retrieve a File object from model");
		}

		String fileName = fileNameModel != null ? fileNameModel.getObject() : null;
		if (Strings.isEmpty(fileName))
		{
			fileName = file.getName();
		}

		IResourceStream resourceStream = new FileResourceStream(
			new org.apache.wicket.util.file.File(file));
		getRequestCycle().scheduleRequestHandlerAfterCurrent(
			new ResourceStreamRequestHandler(resourceStream)
			{
				@Override
				public void respond(IRequestCycle requestCycle)
				{
					super.respond(requestCycle);

					if (deleteAfter)
					{
						Files.remove(file);
					}
				}
			}.setFileName(fileName)
				.setContentDisposition(ContentDisposition.ATTACHMENT)
				.setCacheDuration(cacheDuration));
	}

	/**
	 * USE THIS METHOD WITH CAUTION!
	 * 
	 * If true, the file will be deleted! The recommended way to use this setting, is to set this
	 * DownloadLink object's model with a LoadableDetachableModel instance and the resulting file
	 * being generated in a temporary folder.
	 * 
	 * @param deleteAfter
	 *            true to delete file after download succeeds
	 * @return this component
	 */
	public final DownloadLink setDeleteAfterDownload(boolean deleteAfter)
	{
		this.deleteAfter = deleteAfter;

		return this;
	}

	/**
	 * Sets the duration for which the file resource should be cached by the client.
	 * 
	 * @param duration
	 *            the duration to cache
	 * @return this component.
	 */
	public DownloadLink setCacheDuration(final Duration duration)
	{
		cacheDuration = duration;
		return this;
	}


}
