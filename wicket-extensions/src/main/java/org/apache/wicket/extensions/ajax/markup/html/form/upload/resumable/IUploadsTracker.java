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
package org.apache.wicket.extensions.ajax.markup.html.form.upload.resumable;

import java.util.List;

import org.apache.wicket.util.io.IClusterable;

/**
 * Keeps track of the resumable uploads, so that a later session can list those of its owner and go
 * on with them, or take the files they stored, wherever it is: on a page of its own, in a step of a
 * wizard, from another browser. The owner is the key: one that outlives the session, the
 * signed-in user for example, see {@link ResumableUploadResourceReference#getCurrentOwner()},
 * finds its uploads again in the next session.
 * <p>
 * One tracker serves the whole application:
 * {@link ResumableUploadResourceReference#setUploadsTracker(IUploadsTracker)} registers it. Each
 * call names the context of the upload, the part of the application it belongs to, which the field
 * sets, see {@link ResumableUploadToResourceField#setUploadContext(String)}: the attachments of a
 * process, videos, and so on, {@code null} for none. A tracker keeps the uploads of each context
 * apart, or not, as the application wants.
 * <p>
 * The reference tells the tracker of each upload created, of the bytes of each request that
 * arrived, of each file stored and of each upload the server no longer has; a
 * {@link ResumableUploadToResourceField} lists, from {@link #getUploads(String, String)}, those it
 * offers to go on with, see {@link ResumableUploadToResourceField#getRecalledUploads()}. The
 * application deletes an upload the user discards, or whose file it took, with
 * {@link #delete(String, String, String)}. The tracker the reference has by default keeps nothing
 * and lists the uploads the reference holds in memory. One that keeps them in a database lists them
 * after a restart too, though the server then no longer has their bytes: the field forgets those
 * once the server says so.
 * <p>
 * The requests of the uploads call it at the same time, from threads of their own: an
 * implementation is thread safe. The reference logs what it throws, and goes on. Once the
 * application stops, the reference tells the tracker of the uploads it deletes, and then
 * {@link #destroy()}s it.
 *
 * @see TrackedUpload
 * @since 11.1.0

 */
public interface IUploadsTracker extends IClusterable
{
	/**
	 * An upload was created.
	 *
	 * @param context
	 *            the context of the upload, {@code null} for none
	 * @param upload
	 *            the upload
	 */
	default void onCreated(String context, TrackedUpload upload)
	{
	}

	/**
	 * Bytes of an upload arrived: once for each request that sent some.
	 *
	 * @param context
	 *            the context of the upload, {@code null} for none
	 * @param upload
	 *            the upload, with the bytes the server holds now
	 */
	default void onProgress(String context, TrackedUpload upload)
	{
	}

	/**
	 * The whole file of an upload went to the file manager, under
	 * {@link TrackedUpload#storedName()} for {@link TrackedUpload#storedFieldId()}: until the
	 * application takes it, the file manager finds it with
	 * {@code getFile(storedFieldId, storedName)}.
	 *
	 * @param context
	 *            the context of the upload, {@code null} for none
	 * @param upload
	 *            the upload
	 */
	default void onStored(String context, TrackedUpload upload)
	{
	}

	/**
	 * The server no longer has an upload: it was canceled, it expired, it ended with its session,
	 * storing its file failed, the application deleted it or the application stopped. A file it
	 * stored stays with the file manager.
	 *
	 * @param context
	 *            the context of the upload, {@code null} for none
	 * @param upload
	 *            the upload
	 */
	default void onDeleted(String context, TrackedUpload upload)
	{
	}

	/**
	 * @param context
	 *            the context of the uploads, {@code null} for those of every context
	 * @param owner
	 *            the owner, never {@code null}
	 * @return the uploads of the owner in the context, finished or not, the oldest first
	 */
	List<TrackedUpload> getUploads(String context, String owner);

	/**
	 * Deletes what the tracker keeps of an upload: the user discarded it, or the application took
	 * the file it stored. The tracker the reference has by default deletes the upload the reference
	 * holds, {@link ResumableUploadResourceReference#deleteUpload(String, String)}.
	 *
	 * @param context
	 *            the context of the upload, {@code null} for none
	 * @param owner
	 *            the owner of the upload
	 * @param uploadId
	 *            the id of the upload, {@link TrackedUpload#id()}
	 */
	void delete(String context, String owner, String uploadId);

	/**
	 * The application stops: the reference told the tracker of the uploads it deleted, and the
	 * tracker lets go of what it holds, the connections to its database for example. The reference
	 * calls it from
	 * {@link org.apache.wicket.IApplicationListener#onBeforeDestroyed(org.apache.wicket.Application)}
	 * of the application it is mounted in, and does not call it for a tracker another one replaced.
	 */
	default void destroy()
	{
	}
}
