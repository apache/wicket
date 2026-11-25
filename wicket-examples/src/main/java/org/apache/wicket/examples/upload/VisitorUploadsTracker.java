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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable.IUploadsTracker;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable.TrackedUpload;

/**
 * Keeps track of the resumable uploads of each visitor, in memory, one for the whole application: an
 * application keeps them in its database, so that its users find them in a later session, on
 * another browser or after a restart. A file stored stays listed, though the server forgets its
 * upload, until the visitor takes it. The uploads of each context, each part of the application,
 * are listed apart.
 */
public class VisitorUploadsTracker implements IUploadsTracker
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Map<String, TrackedUpload>> uploadsByOwner = new ConcurrentHashMap<>();

	@Override
	public void onCreated(String context, TrackedUpload upload)
	{
		keep(upload);
	}

	@Override
	public void onProgress(String context, TrackedUpload upload)
	{
		keep(upload);
	}

	@Override
	public void onStored(String context, TrackedUpload upload)
	{
		keep(upload);
	}

	@Override
	public void onDeleted(String context, TrackedUpload upload)
	{
		// a file stored stays with the file manager, until it is taken
		if (!upload.isStored())
		{
			uploadsOf(upload.owner()).remove(upload.id());
		}
	}

	@Override
	public List<TrackedUpload> getUploads(String context, String owner)
	{
		return uploadsOf(owner).values()
			.stream()
			.filter(upload -> context == null || context.equals(upload.context()))
			.sorted(Comparator.comparing(TrackedUpload::created))
			.toList();
	}

	@Override
	public void delete(String context, String owner, String uploadId)
	{
		uploadsOf(owner).remove(uploadId);
	}

	@Override
	public void destroy()
	{
		// an application closes the connections to its database here
		uploadsByOwner.clear();
	}

	/**
	 * Takes the files stored for an upload field, which it heard of.
	 *
	 * @param owner
	 *            the owner of their uploads
	 * @param uploadFieldId
	 *            the id of the field
	 */
	public void takeAllOf(String owner, String uploadFieldId)
	{
		uploadsOf(owner).values()
			.removeIf(upload -> upload.isStored() && uploadFieldId.equals(upload.storedFieldId()));
	}

	private Map<String, TrackedUpload> uploadsOf(String owner)
	{
		return uploadsByOwner.computeIfAbsent(owner, key -> new ConcurrentHashMap<>());
	}

	private void keep(TrackedUpload upload)
	{
		uploadsOf(upload.owner()).put(upload.id(), upload);
	}
}
