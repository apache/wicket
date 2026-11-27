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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.upload.resource.FileUploadToResourceField;
import org.apache.wicket.markup.html.form.upload.resource.IUploadsFileManager;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * A {@link FileUploadToResourceField} whose files go to a {@link ResumableUploadResourceReference}
 * in chunks, each file an upload of its own that can be paused, resumed and canceled.
 * <p>
 * After the connection broke off, an upload goes on by itself from where the server got, once the
 * server answers again within {@link #setRetryTimeout(Duration) the retry timeout}, 40 seconds by
 * default. Once the connection is lost for longer, a VPN that went down for example, the field
 * keeps checking whether the server answers again, and then asks the user whether to resume the
 * uploads or cancel them, unless it is not {@link #setAskingToResume(boolean) asking}: then they
 * go on by themselves.
 * <p>
 * When the field is {@link #setRemembering(boolean) remembering}, the uploads also outlive the
 * page: after a reload, a restart of the browser, or a new session when the uploads belong to the
 * user, see {@link ResumableUploadResourceReference#getCurrentOwner()}, the field lists the uploads
 * that did not end and asks whether to go on with them, which it does once the user chooses their
 * files again. Whether a page rendered before the session ended can go on with them too depends on
 * the key that signs the grant of the field, see
 * {@link ResumableUploadResourceReference#setGrantSigner(IUploadGrantSigner)}: when the server
 * refuses the grant, the field tells the user to load the page again, and the page loaded offers to
 * go on with the uploads.
 * <p>
 * The field asks in its lists, above the uploads.
 * {@link #getClientSideResumeQuestionCallBack()} lets the page ask in a way of its own instead.
 * <p>
 * Everything else is as for its parent: {@link #startUpload(IPartialPageRequestHandler)} sends
 * the files chosen, a {@link org.apache.wicket.markup.html.form.upload.resource.FileDropZoneBehavior}
 * lets them be dropped and a {@link org.apache.wicket.markup.html.form.upload.resource.FileListBehavior}
 * lists them, there with their progress and buttons to pause, resume and cancel each upload. As
 * each file arrives whole it goes to the {@link IUploadsFileManager} of the reference, and
 * {@link #onUploadSuccess(AjaxRequestTarget, List)} is called with it. A file refused or lost is
 * reported to {@link #onUploadFailure(AjaxRequestTarget, String)}, and a canceled one to
 * {@link #onUploadCanceled(AjaxRequestTarget)}. The functions of the parent's client side
 * callbacks are called as for the parent, per file, except for
 * {@link #getClientBeforeSendCallBack()}: it is called once as the upload starts, with
 * {@code null} instead of a request, since every file makes several.
 * <p>
 * The limits of the field are those of its parent: {@link #setMaxSize(Bytes)} for the files
 * chosen at once and for each file, {@link #setFileMaxSize(Bytes)} for each file and
 * {@link #setFileCountMax(long)} for the number of files chosen at once. Those of the reference
 * apply too, and the lower one wins. Every file is an upload of its own, so the server holds each
 * request to the size of each file only, {@link #getFileMaxSize()}, which the grant of the field
 * signs: the size of the files chosen at once and their number are checked in the browser only.
 *
 * @since 12.0.0
 */
public abstract class ResumableUploadToResourceField extends FileUploadToResourceField
{
	private static final long serialVersionUID = 1L;

	/** The script of the parent, which the script of this field builds on. */
	private static final JavaScriptResourceReference PARENT_JS = new JavaScriptResourceReference(
		FileUploadToResourceField.class, "FileUploadToResourceField.js");

	/** The script of the field. */
	public static final JavaScriptResourceReference JS = new JavaScriptResourceReference(
		ResumableUploadToResourceField.class, "ResumableUploadToResourceField.js")
	{
		private static final long serialVersionUID = 1L;

		@Override
		public List<HeaderItem> getDependencies()
		{
			List<HeaderItem> dependencies = super.getDependencies();
			dependencies.add(JavaScriptHeaderItem.forReference(PARENT_JS));
			return dependencies;
		}
	};

	/** The size of a chunk by default. */
	public static final Bytes DEFAULT_CHUNK_SIZE = Bytes.megabytes(8);

	/** The number of files sent at the same time by default. */
	public static final int DEFAULT_PARALLEL_UPLOADS = 2;

	/**
	 * How long an upload keeps trying again by itself by default, before it takes the connection
	 * for lost.
	 */
	public static final Duration DEFAULT_RETRY_TIMEOUT = Duration.ofSeconds(40);

	/**
	 * How often at most an upload that lost the connection looks whether the server answers again,
	 * by default.
	 */
	public static final Duration DEFAULT_RECONNECT_CHECK_INTERVAL = Duration.ofSeconds(30);

	/** How long a request may send or get nothing before it is given up, by default. */
	public static final Duration DEFAULT_STALL_TIMEOUT = Duration.ofSeconds(30);

	/**
	 * How long after an upload last sent something another page takes it for in use, by default.
	 */
	public static final Duration DEFAULT_IN_USE_TIMEOUT = Duration.ofSeconds(20);

	/** The first waits before looking whether the server answers again, in milliseconds. */
	private static final long[] RECONNECT_CHECK_STEPS = { 5000, 10000, 20000 };

	/** The waits before the tries again, in milliseconds, the last one again and again. */
	private static final long[] RETRY_STEPS = { 0, 1000, 3000, 5000, 10000, 20000, 30000 };

	/** The resource key of the text of a file larger than the limits allow. */
	public static final String UPLOAD_TOO_LARGE_RESOURCE_KEY = "resumableUploadTooLarge";

	/** The resource key of the text of an owner that has too many uploads running at once. */
	public static final String TOO_MANY_UPLOADS_RESOURCE_KEY = "resumableUploadTooMany";

	/** The resource key of the text of an owner whose uploads together are too large. */
	public static final String TOO_MUCH_RESOURCE_KEY = "resumableUploadTooMuch";

	/** The resource key of the text of a server that has no room for a file now. */
	public static final String NO_ROOM_RESOURCE_KEY = "resumableUploadNoRoom";

	/** The resource key of the text of an upload the server no longer has. */
	public static final String UPLOAD_LOST_RESOURCE_KEY = "resumableUploadLost";

	/**
	 * The resource key of the text of a grant the server refuses: one signed with a key it no
	 * longer has, that of an ended session for example, expired, or made for another owner. The
	 * page has to be loaded again.
	 */
	public static final String GRANT_REFUSED_RESOURCE_KEY = "resumableUploadGrantRefused";

	private static final Set<String> RESUMABLE_ERROR_KEYS = Set.of(UPLOAD_TOO_LARGE_RESOURCE_KEY,
		TOO_MANY_UPLOADS_RESOURCE_KEY, TOO_MUCH_RESOURCE_KEY, NO_ROOM_RESOURCE_KEY,
		UPLOAD_LOST_RESOURCE_KEY, GRANT_REFUSED_RESOURCE_KEY);

	/** The keys of the texts of the script, without their prefix {@code resumableUpload.}. */
	private static final List<String> TEXTS = List.of("waiting", "paused", "retrying", "offline",
		"interrupted", "stopped", "failed", "pause", "resume", "cancel", "reconnected", "recalled",
		"continue", "discard");

	private Bytes chunkSize = DEFAULT_CHUNK_SIZE;

	private int parallelUploads = DEFAULT_PARALLEL_UPLOADS;

	private boolean remembering = true;

	private boolean askingToResume = true;

	private Duration retryTimeout = DEFAULT_RETRY_TIMEOUT;

	private Duration reconnectCheckInterval = DEFAULT_RECONNECT_CHECK_INTERVAL;

	private Duration stallTimeout = DEFAULT_STALL_TIMEOUT;

	private boolean recalling = true;

	private String uploadContext;

	private Duration inUseTimeout = DEFAULT_IN_USE_TIMEOUT;

	private boolean overridingMethod;

	/**
	 * @param id
	 *            the component id
	 */
	public ResumableUploadToResourceField(String id)
	{
		super(id);
	}

	/**
	 * @return the reference the files go to, by default the one mounted in the application, see
	 *         {@link ResumableUploadResourceReference#getInstance()}
	 */
	protected ResumableUploadResourceReference getResumableUploadResourceReference()
	{
		return ResumableUploadResourceReference.getInstance();
	}

	/**
	 * @return the file manager of the reference
	 */
	@Override
	protected IUploadsFileManager fileManager()
	{
		return getResumableUploadResourceReference().getFileManager();
	}

	/**
	 * @return the size of each file at most: the lowest of {@link #setFileMaxSize(Bytes)},
	 *         {@link #setMaxSize(Bytes)} and the {@link ResumableUploadResourceReference#getMaxFileSize()
	 *         limit of the reference}
	 */
	@Override
	public Bytes getFileMaxSize()
	{
		long most = Math.min(getMaxSize().bytes(),
			getResumableUploadResourceReference().getMaxFileSize().bytes());
		Bytes fileMaxSize = super.getFileMaxSize();
		return Bytes.bytes(fileMaxSize != null ? Math.min(most, fileMaxSize.bytes()) : most);
	}

	/**
	 * @return the size of a chunk
	 */
	public Bytes getChunkSize()
	{
		return chunkSize;
	}

	/**
	 * Sets the size of the chunks the files are sent in: the smaller they are, the less is sent
	 * again after the connection broke off, and the more requests are made. The chunks are never
	 * larger than the reference takes, see
	 * {@link ResumableUploadResourceReference#setMaxChunkSize(Bytes)}.
	 *
	 * @param chunkSize
	 *            the size, {@link #DEFAULT_CHUNK_SIZE} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setChunkSize(Bytes chunkSize)
	{
		Args.notNull(chunkSize, "chunkSize");
		Args.isTrue(chunkSize.bytes() > 0, "chunkSize must be positive");
		this.chunkSize = chunkSize;
		return this;
	}

	/**
	 * @return the number of files sent at the same time
	 */
	public int getParallelUploads()
	{
		return parallelUploads;
	}

	/**
	 * @param parallelUploads
	 *            the number of files sent at the same time, {@link #DEFAULT_PARALLEL_UPLOADS} by
	 *            default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setParallelUploads(int parallelUploads)
	{
		Args.isTrue(parallelUploads > 0, "parallelUploads must be positive");
		this.parallelUploads = parallelUploads;
		return this;
	}

	/**
	 * @return whether the browser remembers the uploads that did not end
	 */
	public boolean isRemembering()
	{
		return remembering;
	}

	/**
	 * Sets whether the browser remembers the URLs of the uploads that did not end, in its local
	 * storage, with the place of the field, {@link #getRememberedAs()}, and the name, size and
	 * date of their files: after a reload
	 * of the page or a restart of the browser, the field lists those the server still has and asks
	 * whether to go on with them, and an upload goes on once the user chooses the same file again.
	 * The server keeps an upload as long as its owner lasts, the session by default, see
	 * {@link ResumableUploadResourceReference#getCurrentOwner()}, and the upload does not expire.
	 * The browser remembers no content of the files.
	 *
	 * @param remembering
	 *            whether to remember, {@code true} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setRemembering(boolean remembering)
	{
		this.remembering = remembering;
		return this;
	}

	/**
	 * Names the place the uploads of the field are remembered for: the field in the same place of a
	 * later page offers to go on with them. By default it is the class of the page and the path of
	 * the field in it, since the id of the field is a new one on every page.
	 * <p>
	 * A page that edits one of several records, a document for example, adds the record, as
	 * {@code "document:" + id}, so that an upload started for one document is not offered on the
	 * page of another, where its file would go to the wrong record. Where what the file was for does
	 * not come back, the unsaved steps of an Ajax wizard after its session ended for example, the
	 * uploads should belong to the session, see
	 * {@link ResumableUploadResourceReference#getCurrentOwner()}, or not be
	 * {@link #setRecalling(boolean) recalled}.
	 *
	 * @return the place of the field
	 */
	protected String getRememberedAs()
	{
		return getPage().getClass().getName() + ':' + getPageRelativePath();
	}

	/**
	 * @return whether the field asks the user before it resumes the uploads, once the server
	 *         answers again after the connection was lost for long
	 */
	public boolean isAskingToResume()
	{
		return askingToResume;
	}

	/**
	 * Sets whether the field asks the user before it resumes the uploads once the server answers
	 * again, after the connection was lost for longer than {@link #setRetryTimeout(Duration) the
	 * retry timeout}: the user may no longer want them, or want to look at what was sent first.
	 * Else the uploads go on by themselves. Either way an upload goes on by itself after a shorter
	 * loss.
	 *
	 * @param askingToResume
	 *            whether to ask, {@code true} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setAskingToResume(boolean askingToResume)
	{
		this.askingToResume = askingToResume;
		return this;
	}

	/**
	 * @return how long an upload keeps trying again by itself before it takes the connection for
	 *         lost
	 */
	public Duration getRetryTimeout()
	{
		return retryTimeout;
	}

	/**
	 * Sets how long an upload keeps trying again by itself after a request got no answer, or a
	 * busy one (5xx, 409, 423 or 429), waiting longer before each try: at once, then after 1, 3,
	 * 5, 10, 20 and then 30 seconds. Also how long it waits for the browser to be online again.
	 * After that, an upload without answers takes the connection for lost: it waits for the server
	 * to answer again and the field asks whether to resume it, see
	 * {@link #setAskingToResume(boolean)}; one with busy answers fails.
	 * <p>
	 * A short timeout asks soon, which suits a VPN that is down until the user does something; a
	 * long one rides out a longer outage silently.
	 *
	 * @param retryTimeout
	 *            the timeout, {@link #DEFAULT_RETRY_TIMEOUT} by default; {@link Duration#ZERO} to
	 *            take the connection for lost at the first request without an answer
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setRetryTimeout(Duration retryTimeout)
	{
		Args.notNull(retryTimeout, "retryTimeout");
		Args.isTrue(!retryTimeout.isNegative(), "retryTimeout must not be negative");
		this.retryTimeout = retryTimeout;
		return this;
	}

	/**
	 * @return how often at most an upload that lost the connection looks whether the server
	 *         answers again
	 */
	public Duration getReconnectCheckInterval()
	{
		return reconnectCheckInterval;
	}

	/**
	 * Sets how often at most the field looks whether the server answers again, once the connection
	 * of its uploads is lost: after 5, 10 and 20 seconds, as long as they are shorter, and then at
	 * this interval. It also looks whenever the browser is online again or the page gets the focus.
	 *
	 * @param reconnectCheckInterval
	 *            the interval, {@link #DEFAULT_RECONNECT_CHECK_INTERVAL} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setReconnectCheckInterval(Duration reconnectCheckInterval)
	{
		Args.notNull(reconnectCheckInterval, "reconnectCheckInterval");
		Args.isTrue(!reconnectCheckInterval.isNegative() && !reconnectCheckInterval.isZero(),
			"reconnectCheckInterval must be positive");
		this.reconnectCheckInterval = reconnectCheckInterval;
		return this;
	}

	/**
	 * @return the waits before each look whether the server answers again in milliseconds, the
	 *         last one again and again
	 */
	static List<Long> reconnectCheckDelaysOf(Duration interval)
	{
		List<Long> delays = new ArrayList<>();
		for (long step : RECONNECT_CHECK_STEPS)
		{
			if (step < interval.toMillis())
			{
				delays.add(step);
			}
		}
		delays.add(interval.toMillis());
		return delays;
	}

	/**
	 * @return how long a request may send or get nothing before it is given up
	 */
	public Duration getStallTimeout()
	{
		return stallTimeout;
	}

	/**
	 * Sets how long a request may send nothing, or get no answer, before it is given up and tried
	 * again, as after a broken connection: a connection that went away without a word answers
	 * never. A chunk whose bytes all left the browser waits for its answer as long as the server
	 * answers a {@code HEAD} every so often.
	 *
	 * @param stallTimeout
	 *            the timeout, {@link #DEFAULT_STALL_TIMEOUT} by default; {@link Duration#ZERO} never
	 *            to give a request up
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setStallTimeout(Duration stallTimeout)
	{
		Args.notNull(stallTimeout, "stallTimeout");
		Args.isTrue(!stallTimeout.isNegative(), "stallTimeout must not be negative");
		this.stallTimeout = stallTimeout;
		return this;
	}

	/**
	 * @return whether the field lists, as the page loads, the uploads an earlier page left
	 *         unfinished, and asks whether to go on with them
	 */
	public boolean isRecalling()
	{
		return recalling;
	}

	/**
	 * Sets whether the field lists, as the page loads, the uploads an earlier page of the same place
	 * left unfinished, and asks whether to go on with them. Without, an upload still goes on once
	 * the user happens to choose the same file again, as long as the field is
	 * {@link #setRemembering(boolean) remembering}.
	 *
	 * @param recalling
	 *            whether to list and ask, {@code true} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setRecalling(boolean recalling)
	{
		this.recalling = recalling;
		return this;
	}

	/**
	 * Lists the uploads the server keeps that the field offers to go on with as the page loads,
	 * besides those the browser remembers: with the uploads belonging to the user, see
	 * {@link ResumableUploadResourceReference#getCurrentOwner()}, those started in another browser,
	 * or, overridden, those started anywhere else, on a page listing the unfinished uploads of the
	 * user or in a step of a wizard for example. The user goes on with one by choosing its file again,
	 * which has to have the same name, size and date. The field asks only while it is
	 * {@link #setRemembering(boolean) remembering} and {@link #setRecalling(boolean) recalling}.
	 * <p>
	 * By default these are the unfinished uploads of the current owner in the
	 * {@link #setUploadContext(String) context} of the field, wherever they started, or, without a
	 * context, those a field in the same place started, {@link #getRememberedAs()}, as the
	 * {@link ResumableUploadResourceReference#getUploadsTracker() tracker of the uploads} lists them,
	 * whose files are no larger than the field takes.
	 *
	 * @return the uploads to offer
	 */
	protected List<TrackedUpload> getRecalledUploads()
	{
		ResumableUploadResourceReference reference = getResumableUploadResourceReference();
		String owner = reference.getCurrentOwner();
		if (owner == null)
		{
			return List.of();
		}
		String place = getRememberedAs();
		long fileMaxSize = getFileMaxSize().bytes();
		return reference.getUploadsTracker()
			.getUploads(uploadContext, owner)
			.stream()
			.filter(upload -> !upload.isStored() && !upload.isComplete() &&
				upload.length() <= fileMaxSize &&
				(uploadContext != null || place.equals(upload.place())))
			.toList();
	}

	/**
	 * @return the part of the application the uploads of the field belong to, {@code null} for
	 *         none
	 */
	public String getUploadContext()
	{
		return uploadContext;
	}

	/**
	 * Sets the part of the application the uploads of the field belong to: the attachments of a
	 * process, videos, and so on. The grant of the field signs it, and the
	 * {@link ResumableUploadResourceReference#getUploadsTracker() tracker of the uploads} hears of
	 * each upload with it, so that the application keeps the uploads of each part apart, or not. A
	 * field with a context offers to go on with the unfinished uploads of its owner in the same
	 * context, wherever they started, see {@link #getRecalledUploads()}.
	 *
	 * @param uploadContext
	 *            the context, a single line, {@code null} by default for none
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setUploadContext(String uploadContext)
	{
		Args.isTrue(UploadGrant.isLine(uploadContext), "uploadContext must be a single line");
		this.uploadContext = uploadContext;
		return this;
	}

	/**
	 * @return the uploads to offer, as the script of the field takes them
	 */
	private JSONArray recalledSettings()
	{
		JSONArray recalled = new JSONArray();
		if (!remembering || !recalling)
		{
			return recalled;
		}
		for (TrackedUpload upload : getRecalledUploads())
		{
			if (upload.isStored() || upload.isComplete())
			{
				continue;
			}
			JSONObject each = new JSONObject();
			each.put("id", upload.id());
			each.put("name", upload.clientFileName());
			each.put("size", upload.length());
			each.put("lastModified", upload.lastModified());
			each.put("type", upload.contentType());
			each.put("sent", upload.offset());
			recalled.put(each);
		}
		return recalled;
	}

	/**
	 * @return how long after an upload last sent something another page takes it for in use
	 */
	public Duration getInUseTimeout()
	{
		return inUseTimeout;
	}

	/**
	 * Sets how long after an upload last sent something another page of the field, in another tab
	 * for example, takes it for in use: it leaves it alone rather than offering to go on with it. A
	 * page sending an upload tells so in the local storage four times as often. A page that is
	 * left tells at once that it no longer sends.
	 *
	 * @param inUseTimeout
	 *            the timeout, {@link #DEFAULT_IN_USE_TIMEOUT} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setInUseTimeout(Duration inUseTimeout)
	{
		Args.notNull(inUseTimeout, "inUseTimeout");
		Args.isTrue(!inUseTimeout.isNegative() && !inUseTimeout.isZero(),
			"inUseTimeout must be positive");
		this.inUseTimeout = inUseTimeout;
		return this;
	}

	/**
	 * @return whether the script sends {@code PATCH} and {@code DELETE} as {@code POST}, naming the
	 *         method in the header {@code X-HTTP-Method-Override}
	 */
	public boolean isOverridingMethod()
	{
		return overridingMethod;
	}

	/**
	 * Sets whether the script sends its chunks, {@code PATCH}, and the deletion of an upload,
	 * {@code DELETE}, as {@code POST}, naming the method in the header
	 * {@code X-HTTP-Method-Override}, for a proxy in front of the server that refuses those
	 * methods.
	 *
	 * @param overridingMethod
	 *            whether to override the method, {@code false} by default
	 * @return {@code this}
	 */
	public ResumableUploadToResourceField setOverridingMethod(boolean overridingMethod)
	{
		this.overridingMethod = overridingMethod;
		return this;
	}

	/**
	 * @return the waits before the tries again in milliseconds, which together take a timeout
	 */
	static List<Long> retryDelaysOf(Duration timeout)
	{
		List<Long> delays = new ArrayList<>();
		long left = timeout.toMillis();
		for (int step = 0; left > 0; step++)
		{
			long delay = Math.min(RETRY_STEPS[Math.min(step, RETRY_STEPS.length - 1)], left);
			delays.add(delay);
			left -= delay;
		}
		return delays;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		CoreLibrariesContributor.contributeAjax(getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(OnDomReadyHeaderItem.forScript("Wicket.Timer." + getMarkupId() +
			" = new Wicket.ResumableUploadToResourceField(" + newClientSettings() + "," +
			getClientBeforeSendCallBack() + "," + getClientSideSuccessCallBack() + "," +
			getClientSideCancelCallBack() + "," + getClientSideUploadErrorCallBack() + "," +
			getClientSideResumeQuestionCallBack() + ");"));
	}

	/**
	 * Lets the page ask the user whether to go on with uploads, in a dialog of its own for example,
	 * instead of the field asking in its lists. The function is called with a question:
	 * <ul>
	 * <li>{@code reason}: {@code 'reconnected'} when the server answers again after the connection
	 * was lost for long, {@code 'recalled'} when the page found uploads of an earlier visit that
	 * did not end;</li>
	 * <li>{@code files}: the files, each with its {@code name}, its {@code size} and the bytes the
	 * server holds, {@code sent};</li>
	 * <li>{@code resume()}: goes on with the uploads, for {@code 'recalled'} by opening the
	 * browser's dialog to choose their files again, which only works while the user clicks;</li>
	 * <li>{@code cancel()}: cancels the uploads, deleting them on the server.</li>
	 * </ul>
	 * It returns {@code true} when it asked the user, else the field asks in its lists. By default
	 * it returns {@code false}.
	 *
	 * @return the JavaScript function
	 */
	protected CharSequence getClientSideResumeQuestionCallBack()
	{
		return "function (question) { return false; }";
	}

	/**
	 * Creates the settings of the script of the field: those of its parent, with the URL of the
	 * reference as {@code resourceUrl}, the grant of the field signed by the reference's
	 * {@link ResumableUploadResourceReference#getGrantSigner() signer} for the
	 * {@link ResumableUploadResourceReference#getCurrentOwner() current owner} as
	 * {@code uploadToken}, and {@code grantExpires}, when the grant expires, in milliseconds since
	 * the epoch, -1 for never, and {@code chunkSize},
	 * {@code parallelUploads}, {@code remembering}, {@code rememberedAs}, {@code recalling},
	 * {@code recalled}, the uploads of {@link #getRecalledUploads()}, {@code uploadContext}, the
	 * {@link #setUploadContext(String) context} of the field when it has one,
	 * {@code askingToResume}, {@code retryDelays}, the waits before the tries again in
	 * milliseconds, {@code probeDelays}, the waits before each look whether the server answers again
	 * once the connection is lost, the last one again and again, {@code stallMillis},
	 * {@code inUseMillis}, {@code overridingMethod} and {@code texts}. The script sends the id of
	 * the field, {@code fileMaxSize}, {@code uploadToken}, {@code grantExpires} and
	 * {@code uploadContext} along with each of its requests.
	 *
	 * @return the settings
	 */
	protected JSONObject newClientSettings()
	{
		ResumableUploadResourceReference reference = getResumableUploadResourceReference();
		UploadGrant grant = reference.newGrant(getMarkupId(), uploadContext, getFileMaxSize());
		JSONObject settings = new JSONObject();
		settings.put("inputName", getMarkupId());
		settings.put("resourceUrl", Strings.stripEnding(
			urlFor(reference, new PageParameters()).toString(), "/"));
		settings.put("ajaxCallBackUrl", getUploadCallbackUrl());
		// as large as a script holds exactly
		settings.put("maxSize", Math.min(getMaxSize().bytes(), UploadGrant.MAX_SAFE_INTEGER));
		settings.put("fileMaxSize", grant.fileMaxSize());
		settings.put("fileCountMax", getFileCountMax());
		settings.put("uploadToken",
			grant.token(reference.getGrantSigner(), reference.getCurrentOwner()));
		settings.put("grantExpires", grant.expires());
		settings.put("chunkSize",
			Math.min(chunkSize.bytes(), reference.getMaxChunkSize().bytes()));
		settings.put("parallelUploads", parallelUploads);
		settings.put("remembering", remembering);
		settings.put("rememberedAs", getRememberedAs());
		settings.put("askingToResume", askingToResume);
		settings.put("retryDelays", new JSONArray(retryDelaysOf(retryTimeout)));
		settings.put("probeDelays",
			new JSONArray(reconnectCheckDelaysOf(reconnectCheckInterval)));
		settings.put("stallMillis", stallTimeout.toMillis());
		settings.put("recalling", recalling);
		settings.put("recalled", recalledSettings());
		if (uploadContext != null)
		{
			settings.put("uploadContext", uploadContext);
		}
		settings.put("inUseMillis", inUseTimeout.toMillis());
		settings.put("overridingMethod", overridingMethod);
		JSONObject texts = new JSONObject();
		for (String key : TEXTS)
		{
			texts.put(key, getString("resumableUpload." + key));
		}
		settings.put("texts", texts);
		settings.put("errorKeys", new JSONArray(RESUMABLE_ERROR_KEYS));
		return settings;
	}

	/**
	 * Words the errors of the uploads of this field, and leaves the others to its parent.
	 */
	@Override
	protected String getUploadErrorMessage(String errorKey)
	{
		if (errorKey == null || !RESUMABLE_ERROR_KEYS.contains(errorKey))
		{
			return super.getUploadErrorMessage(errorKey);
		}
		ResumableUploadResourceReference reference = getResumableUploadResourceReference();
		Map<String, Object> model = new HashMap<>();
		model.put("fileMaxSize", getFileMaxSize());
		model.put("maxUploadsPerOwner", reference.getMaxUploadsPerOwner());
		model.put("maxBytesPerOwner", reference.getMaxBytesPerOwner());
		return getString(errorKey, Model.ofMap(model));
	}

	/**
	 * @return the JavaScript expression pausing the uploads of the field
	 */
	public String getTriggerPauseUploadScript()
	{
		return "Wicket.Timer." + getMarkupId() + ".pause();";
	}

	/**
	 * Pauses the uploads of the field via an Ajax request.
	 *
	 * @param target
	 *            the {@link AjaxRequestTarget}
	 */
	public void pauseUpload(IPartialPageRequestHandler target)
	{
		target.appendJavaScript(getTriggerPauseUploadScript());
	}

	/**
	 * @return the JavaScript expression resuming the paused uploads of the field
	 */
	public String getTriggerResumeUploadScript()
	{
		return "Wicket.Timer." + getMarkupId() + ".resume();";
	}

	/**
	 * Resumes the paused uploads of the field via an Ajax request.
	 *
	 * @param target
	 *            the {@link AjaxRequestTarget}
	 */
	public void resumeUpload(IPartialPageRequestHandler target)
	{
		target.appendJavaScript(getTriggerResumeUploadScript());
	}
}
