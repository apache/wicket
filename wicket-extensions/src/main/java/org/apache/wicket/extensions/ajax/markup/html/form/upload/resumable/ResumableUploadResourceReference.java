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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.wicket.Application;
import org.apache.wicket.IApplicationListener;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.resource.FolderUploadsFileManager;
import org.apache.wicket.markup.html.form.upload.resource.IUploadsFileManager;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The resource {@link ResumableUploadToResourceField} sends its files to, in chunks, over the
 * open <a href="https://tus.io/protocols/resumable-upload">tus protocol 1.0</a>: the browser
 * creates an upload ({@code POST}), appends chunks to it ({@code PATCH}), asks how many bytes the
 * server holds after a pause, a broken connection or a reload of the page ({@code HEAD}) and may
 * delete it ({@code DELETE}). Once the server holds the whole file, it goes to the
 * {@link IUploadsFileManager}, as the files sent to a
 * {@link org.apache.wicket.markup.html.form.upload.resource.FileUploadResourceReference} do, and
 * the field hears of it as of those.
 * <p>
 * The application mounts it once, in {@code init()}:
 *
 * <pre>
 * mountResource("/resumable-uploads",
 * 	new ResumableUploadResourceReference(new FolderUploadsFileManager(folder)));
 * </pre>
 *
 * Each upload gets a random name below that path. The bytes received go into files of their own in
 * a folder, {@link #getFolder()}, until the file is whole. What the server knows of the uploads is
 * kept in memory.
 * <p>
 * Every request carries the id of the upload field and the limits of its uploads, its grant,
 * signed by the field with the {@link #setGrantSigner(IUploadGrantSigner) signer of the grants},
 * so the browser cannot raise them. An upload belongs to an owner, {@link #getCurrentOwner()}: by
 * default the HTTP session that created it, so that the requests of another session are told there
 * is no such upload, and a request without a session creates none. The grant is made for the owner
 * of the page that rendered it, and refused to any other. An application whose users sign in can
 * make an upload belong to its user instead, and outlive the session: once the user is back, in a
 * new session, the field offers to go on with it. The signer's key decides whether a page rendered
 * in the earlier session can go on too: by default the key is one of the session, so the page has
 * to be loaded again; with a key of the application, {@link HmacGrantSigner}, the page goes on as
 * long as its grant has not {@link #setGrantExpiry(Duration) expired}. A request without the
 * header {@code Tus-Resumable} is refused, which a form of another site cannot send without asking
 * the browser first.
 * <p>
 * An {@link #setUploadsTracker(IUploadsTracker) uploads tracker} hears of each upload created, of
 * its bytes, of its file stored and of its end, and lists the uploads of an owner: a field offers
 * those to go on with in a later session, on a page of their own or in a step of a wizard, wherever
 * they started.
 * <p>
 * The server keeps no more than it is allowed to:
 * <ul>
 * <li>a file larger than the field allows, or larger than {@link #setMaxFileSize(Bytes)}, is
 * refused as the browser creates its upload, before a byte is sent;</li>
 * <li>an owner, a session by default, has at most {@link #setMaxUploadsPerSession(int)} uploads
 * running at once, at most {@link #setMaxBytesPerSession(Bytes)} together, all owners together at
 * most {@link #setMaxTotalBytes(Bytes)}, while at least {@link #setMinFreeSpace(Bytes)} stays free
 * on the disk;</li>
 * <li>a request carries at most {@link #setMaxChunkSize(Bytes)}, which has to be less than a proxy
 * in front of the server lets through;</li>
 * <li>an upload the browser sent nothing to for {@link #setExpiry(Duration)} is deleted, one that
 * belongs to an owner other than its session for {@link #setOwnerExpiry(Duration)}, and so are the
 * uploads a session owns when it ends, and all of them when the application stops; the requests
 * look for those at most every {@link #setExpiryCheckInterval(Duration)};</li>
 * <li>the header {@code Upload-Metadata} has at most {@link #setMaxMetadataLength(int)}
 * characters.</li>
 * </ul>
 * <p>
 * Wicket's filter answers {@code OPTIONS} itself, so the server does not announce its version and
 * extensions that way; tus clients do not need to ask. A proxy refusing {@code PATCH} is passed by
 * a {@code POST} with {@code X-HTTP-Method-Override: PATCH}.
 *
 * @see ResumableUploadToResourceField
 * @since 11.1.0
 */
public class ResumableUploadResourceReference extends ResourceReference
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(
		ResumableUploadResourceReference.class);

	/** The name the reference is registered under. */
	public static final String NAME = "resumable-uploads";

	/**
	 * The header of the answer that completes an upload, and of a {@code HEAD} after it: the name
	 * the file manager stored the file under.
	 */
	public static final String FILE_NAME_HEADER = "Wicket-Upload-File-Name";

	/** The header of a refusal: the resource key of its error. */
	public static final String ERROR_HEADER = "Wicket-Upload-Error";

	/** The size of a file at most, by default. */
	public static final Bytes DEFAULT_MAX_FILE_SIZE = Bytes.gigabytes(1);

	/** The size of a request at most, by default. */
	public static final Bytes DEFAULT_MAX_CHUNK_SIZE = Bytes.megabytes(16);

	/** How long an upload is kept without news of the browser, by default. */
	public static final Duration DEFAULT_EXPIRY = Duration.ofMinutes(30);

	/** The number of uploads a session has running at once at most, by default. */
	public static final int DEFAULT_MAX_UPLOADS_PER_SESSION = 10;

	/** The space left free on the disk, by default. */
	public static final Bytes DEFAULT_MIN_FREE_SPACE = Bytes.megabytes(512);

	/** How often, at most, the requests look for uploads that expired, by default. */
	public static final Duration DEFAULT_EXPIRY_CHECK_INTERVAL = Duration.ofMinutes(1);

	/** How long a request waits for another one writing the same upload, by default. */
	public static final Duration DEFAULT_LOCK_TIMEOUT = Duration.ofSeconds(3);

	/** The length of the header {@code Upload-Metadata} at most, by default. */
	public static final int DEFAULT_MAX_METADATA_LENGTH = 4096;

	/** The names of the uploads, and of their files: 32 hexadecimal digits. */
	private static final Pattern NAME_PATTERN = Pattern.compile("[0-9a-f]{32}");

	private static final SecureRandom RANDOM = new SecureRandom();

	private final IUploadsFileManager fileManager;

	private final File folder;

	private final boolean ownFolder;

	private final transient Map<String, ResumableUpload> uploads = new ConcurrentHashMap<>();

	private final AtomicBoolean listening = new AtomicBoolean();

	private Bytes maxFileSize = DEFAULT_MAX_FILE_SIZE;

	private Bytes maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

	private int maxUploadsPerSession = DEFAULT_MAX_UPLOADS_PER_SESSION;

	private Bytes maxBytesPerSession;

	private Bytes maxTotalBytes;

	private Bytes minFreeSpace = DEFAULT_MIN_FREE_SPACE;

	private Duration expiry = DEFAULT_EXPIRY;

	private Duration ownerExpiry;

	private Duration expiryCheckInterval = DEFAULT_EXPIRY_CHECK_INTERVAL;

	private Duration lockTimeout = DEFAULT_LOCK_TIMEOUT;

	private int maxMetadataLength = DEFAULT_MAX_METADATA_LENGTH;

	private IUploadGrantSigner grantSigner = new CryptFactoryGrantSigner();

	private Duration grantExpiry;

	private IUploadsTracker uploadsTracker = new HeldUploads();

	private Clock clock = Clock.systemUTC();

	private volatile Instant swept = Instant.EPOCH;

	/**
	 * Creates the reference, keeping the bytes of unfinished uploads in a new folder in the
	 * temporary folder of the JVM, which is deleted when the application stops.
	 *
	 * @param fileManager
	 *            where the files go once they are whole, a {@link FolderUploadsFileManager} for
	 *            example
	 */
	public ResumableUploadResourceReference(IUploadsFileManager fileManager)
	{
		this(fileManager, newTemporaryFolder(), true);
	}

	/**
	 * Creates the reference.
	 *
	 * @param fileManager
	 *            where the files go once they are whole, a {@link FolderUploadsFileManager} for
	 *            example
	 * @param folder
	 *            the folder of the bytes of unfinished uploads, which holds nothing else: the
	 *            files the uploads of an earlier run left there are deleted
	 */
	public ResumableUploadResourceReference(IUploadsFileManager fileManager, File folder)
	{
		this(fileManager, folder, false);
	}

	private ResumableUploadResourceReference(IUploadsFileManager fileManager, File folder,
		boolean ownFolder)
	{
		super(ResumableUploadResourceReference.class, NAME);
		this.fileManager = Args.notNull(fileManager, "fileManager");
		this.folder = Args.notNull(folder, "folder");
		this.ownFolder = ownFolder;
		deleteFiles();
	}

	private static File newTemporaryFolder()
	{
		try
		{
			return Files.createTempDirectory("wicket-resumable-uploads").toFile();
		}
		catch (IOException e)
		{
			throw new IllegalStateException("Could not create a folder for resumable uploads", e);
		}
	}

	/**
	 * @return the reference mounted in the application of the current thread
	 * @throws IllegalStateException
	 *             when the application mounted none
	 */
	public static ResumableUploadResourceReference getInstance()
	{
		ResourceReference reference = Application.get()
			.getResourceReferenceRegistry()
			.getResourceReference(ResumableUploadResourceReference.class, NAME, null, null, null,
				false, false);
		if (reference instanceof ResumableUploadResourceReference resumable)
		{
			return resumable;
		}
		throw new IllegalStateException("No " + ResumableUploadResourceReference.class.getName() +
			" is mounted: mount one in the init() of the application");
	}

	@Override
	public IResource getResource()
	{
		return new ResumableUploadResource(this);
	}

	/**
	 * Creates the query that a client other than {@link ResumableUploadToResourceField}, any tus
	 * client, adds to the URL of each of its requests: the id its files are stored under and their
	 * limits, signed with the {@link #setGrantSigner(IUploadGrantSigner) signer of the grants} for
	 * the {@link #getCurrentOwner() current owner}, so the client cannot change them, nor hand them
	 * to another owner, and valid for {@link #getGrantExpiry()}. Once a file is whole, the answer
	 * tells the name it was stored under in the header {@value #FILE_NAME_HEADER}, which the
	 * client hands back to the application, for it to find the file with
	 * {@code getFileManager().getFile(uploadFieldId, name)}. Create the query in a request of the
	 * owner, as for a page.
	 * <p>
	 * The id has to be unique in the application and must not come from the browser. The name the
	 * client hands back does: the application has to check that the id is one it gave the
	 * client's session.
	 *
	 * @param uploadFieldId
	 *            the id the files are stored under
	 * @param maxSize
	 *            the size of the files sent at once, and of each file, at most
	 * @param fileMaxSize
	 *            the size of each file at most, {@code null} for {@code maxSize}
	 * @param fileCountMax
	 *            the number of files sent at once at most, -1 for any
	 * @return the query, without its {@code ?}
	 */
	public String newQuery(String uploadFieldId, Bytes maxSize, Bytes fileMaxSize,
		long fileCountMax)
	{
		Args.notEmpty(uploadFieldId, "uploadFieldId");
		Args.notNull(maxSize, "maxSize");
		return newQuery(uploadFieldId, null, maxSize, fileMaxSize, fileCountMax);
	}

	/**
	 * Creates the query of {@link #newQuery(String, Bytes, Bytes, long)}, for the uploads of a
	 * context, see {@link ResumableUploadToResourceField#setUploadContext(String)}.
	 *
	 * @param uploadFieldId
	 *            the id the files are stored under
	 * @param context
	 *            the part of the application the uploads belong to, {@code null} for none
	 * @param maxSize
	 *            the size of the files sent at once, and of each file, at most
	 * @param fileMaxSize
	 *            the size of each file at most, {@code null} for {@code maxSize}
	 * @param fileCountMax
	 *            the number of files sent at once at most, -1 for any
	 * @return the query, without its {@code ?}
	 */
	public String newQuery(String uploadFieldId, String context, Bytes maxSize, Bytes fileMaxSize,
		long fileCountMax)
	{
		Args.notEmpty(uploadFieldId, "uploadFieldId");
		Args.notNull(maxSize, "maxSize");
		return newGrant(uploadFieldId, context, maxSize, fileMaxSize, fileCountMax).query(getGrantSigner(),
			getCurrentOwner());
	}

	/**
	 * @return where the files go once they are whole
	 */
	public IUploadsFileManager getFileManager()
	{
		return fileManager;
	}

	/**
	 * @return the folder of the bytes of unfinished uploads
	 */
	public File getFolder()
	{
		return folder;
	}

	/**
	 * @return the size of a file at most
	 */
	public Bytes getMaxFileSize()
	{
		return maxFileSize;
	}

	/**
	 * Sets the size of a file at most, for all fields: the limits of a field can only be lower.
	 *
	 * @param maxFileSize
	 *            the size, {@link #DEFAULT_MAX_FILE_SIZE} by default
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMaxFileSize(Bytes maxFileSize)
	{
		this.maxFileSize = Args.notNull(maxFileSize, "maxFileSize");
		return this;
	}

	/**
	 * @return the number of bytes a request carries at most
	 */
	public Bytes getMaxChunkSize()
	{
		return maxChunkSize;
	}

	/**
	 * @param maxChunkSize
	 *            the number of bytes a request carries at most, {@link #DEFAULT_MAX_CHUNK_SIZE} by
	 *            default
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMaxChunkSize(Bytes maxChunkSize)
	{
		Args.notNull(maxChunkSize, "maxChunkSize");
		Args.isTrue(maxChunkSize.bytes() > 0, "maxChunkSize must be positive");
		this.maxChunkSize = maxChunkSize;
		return this;
	}

	/**
	 * @return the number of uploads an owner, a session by default, has running at once at most
	 */
	public int getMaxUploadsPerSession()
	{
		return maxUploadsPerSession;
	}

	/**
	 * @param maxUploadsPerSession
	 *            the number of unfinished uploads an owner, a session by default, has at once at
	 *            most,
	 *            {@link #DEFAULT_MAX_UPLOADS_PER_SESSION} by default
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMaxUploadsPerSession(int maxUploadsPerSession)
	{
		Args.isTrue(maxUploadsPerSession > 0, "maxUploadsPerSession must be positive");
		this.maxUploadsPerSession = maxUploadsPerSession;
		return this;
	}

	/**
	 * @return the size of the unfinished uploads of an owner, a session by default, together at
	 *         most
	 */
	public Bytes getMaxBytesPerSession()
	{
		return maxBytesPerSession != null ? maxBytesPerSession
			: Bytes.bytes(Math.min(Long.MAX_VALUE / 4, maxFileSize.bytes()) * 4);
	}

	/**
	 * @param maxBytesPerSession
	 *            the size of the unfinished uploads of an owner, a session by default, together
	 *            at most; four times
	 *            {@link #getMaxFileSize()} by default, which {@code null} restores
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMaxBytesPerSession(Bytes maxBytesPerSession)
	{
		this.maxBytesPerSession = maxBytesPerSession;
		return this;
	}

	/**
	 * @return the size of the unfinished uploads of all owners together at most
	 */
	public Bytes getMaxTotalBytes()
	{
		return maxTotalBytes != null ? maxTotalBytes
			: Bytes.bytes(Math.min(Long.MAX_VALUE / 10, getMaxBytesPerSession().bytes()) * 10);
	}

	/**
	 * Sets the size of the unfinished uploads of all owners together at most, so that many
	 * sessions together cannot take all the disk, short of {@link #getMinFreeSpace()}, from the
	 * uploads of the others. {@link Bytes#MAX} sets no limit.
	 *
	 * @param maxTotalBytes
	 *            the size; ten times {@link #getMaxBytesPerSession()} by default, which
	 *            {@code null} restores
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMaxTotalBytes(Bytes maxTotalBytes)
	{
		this.maxTotalBytes = maxTotalBytes;
		return this;
	}

	/**
	 * @return the space the uploads leave free on the disk of the folder
	 */
	public Bytes getMinFreeSpace()
	{
		return minFreeSpace;
	}

	/**
	 * @param minFreeSpace
	 *            the space the uploads leave free on the disk of the folder,
	 *            {@link #DEFAULT_MIN_FREE_SPACE} by default
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMinFreeSpace(Bytes minFreeSpace)
	{
		this.minFreeSpace = Args.notNull(minFreeSpace, "minFreeSpace");
		return this;
	}

	/**
	 * @return how long an upload is kept without news of the browser
	 */
	public Duration getExpiry()
	{
		return expiry;
	}

	/**
	 * @param expiry
	 *            how long an upload is kept without news of the browser, {@link #DEFAULT_EXPIRY} by
	 *            default; the browser is told when its upload expires
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setExpiry(Duration expiry)
	{
		Args.notNull(expiry, "expiry");
		Args.isTrue(!expiry.isNegative() && !expiry.isZero(), "expiry must be positive");
		this.expiry = expiry;
		return this;
	}

	/**
	 * @return how long an upload that belongs to an owner other than its session is kept without
	 *         news of the browser
	 */
	public Duration getOwnerExpiry()
	{
		return ownerExpiry != null ? ownerExpiry : expiry;
	}

	/**
	 * Sets how long an upload that belongs to an owner other than its session, the signed-in user
	 * for example, see {@link #getCurrentOwner()}, is kept without news of the browser. Such an
	 * upload outlives its session, and the user may come back to it much later than to an upload of
	 * the session: a day rather than half an hour, for example.
	 *
	 * @param ownerExpiry
	 *            the expiry, {@code null} by default for {@link #getExpiry()}
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setOwnerExpiry(Duration ownerExpiry)
	{
		Args.isTrue(ownerExpiry == null || (!ownerExpiry.isNegative() && !ownerExpiry.isZero()),
			"ownerExpiry must be positive");
		this.ownerExpiry = ownerExpiry;
		return this;
	}

	/**
	 * @return what signs the grants of the fields and checks those the browser sends
	 */
	public IUploadGrantSigner getGrantSigner()
	{
		return grantSigner;
	}

	/**
	 * Sets what signs the grants of the fields, the id of a field and the limits of its uploads,
	 * and checks those the browser sends with each request. By default it is a
	 * {@link CryptFactoryGrantSigner}, the crypt of the application, whose key is one of the HTTP
	 * session unless the application configured another: a page can go on with its uploads as long
	 * as its session lives. An application whose uploads outlive the
	 * session, see {@link #getCurrentOwner()}, may sign with a key of its own, an
	 * {@link HmacGrantSigner}, so that a page rendered in an earlier session goes on with the
	 * uploads of its owner without being loaded again.
	 * <p>
	 * The uploads themselves are kept in memory and their bytes are deleted when the application
	 * stops: a key that outlives a restart does not let an upload outlive it.
	 *
	 * @param grantSigner
	 *            the signer
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setGrantSigner(IUploadGrantSigner grantSigner)
	{
		this.grantSigner = Args.notNull(grantSigner, "grantSigner");
		return this;
	}

	/**
	 * @return how long after a page rendered the grant of its field lets the browser start uploads
	 */
	public Duration getGrantExpiry()
	{
		return grantExpiry != null ? grantExpiry : getOwnerExpiry();
	}

	/**
	 * Sets how long after a page rendered the grant of its field lets the browser start uploads,
	 * and ask about those the server no longer has. An upload started goes on for as long as the
	 * server keeps it, see {@link #setExpiry(Duration)}, however long ago its page rendered: a
	 * long upload does not end with the grant. Once the grant has expired, the page has to be
	 * loaded again for new uploads. The expiry is sent along with the grant and signed with it.
	 *
	 * @param grantExpiry
	 *            the expiry, {@code null} by default for {@link #getOwnerExpiry()}
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setGrantExpiry(Duration grantExpiry)
	{
		Args.isTrue(grantExpiry == null || (!grantExpiry.isNegative() && !grantExpiry.isZero()),
			"grantExpiry must be positive");
		this.grantExpiry = grantExpiry;
		return this;
	}

	/**
	 * @return what keeps track of the uploads, and lists those of an owner
	 */
	public IUploadsTracker getUploadsTracker()
	{
		return uploadsTracker;
	}

	/**
	 * Sets what keeps track of the uploads, one for the whole application: it hears of each upload
	 * created, of the bytes of each request, of each file stored and of each upload the server no
	 * longer has, each with its context, and lists the uploads of an owner in a context, for a field
	 * to offer to go on with them, see {@link ResumableUploadToResourceField#getRecalledUploads()}.
	 * By default it keeps nothing and lists the uploads the reference holds,
	 * {@link #getUploads(String, String)}. One that keeps them, in a database for example, lets an
	 * application list them beyond the uploads, and the files they stored, for the user to choose
	 * one in a later session. Its {@link IUploadsTracker#destroy()} is called once the application
	 * stops.
	 *
	 * @param uploadsTracker
	 *            the tracker
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setUploadsTracker(IUploadsTracker uploadsTracker)
	{
		this.uploadsTracker = Args.notNull(uploadsTracker, "uploadsTracker");
		if (Application.exists())
		{
			// destroyed with the application, whether an upload came or not
			listen();
		}
		return this;
	}

	/**
	 * @param context
	 *            a context, see {@link ResumableUploadToResourceField#setUploadContext(String)},
	 *            {@code null} for every context
	 * @param owner
	 *            an owner, see {@link #getCurrentOwner()}
	 * @return the uploads of the owner in the context the reference holds, finished or not, the
	 *         oldest first
	 */
	public List<TrackedUpload> getUploads(String context, String owner)
	{
		Args.notNull(owner, "owner");
		return uploads.values()
			.stream()
			.filter(upload -> upload.getOwner().equals(owner) &&
				(context == null || context.equals(upload.getContext())))
			.sorted((one, other) -> one.getCreated().compareTo(other.getCreated()))
			.map(TrackedUpload::of)
			.toList();
	}

	/**
	 * Deletes an upload the reference holds, and its bytes, unless a request writes them: the user
	 * discarded it, or the application took the file it stored, which stays with the file manager.
	 * The tracker hears of it, {@link IUploadsTracker#onDeleted(String, TrackedUpload)}.
	 *
	 * @param owner
	 *            the owner of the upload
	 * @param uploadId
	 *            the id of the upload
	 * @return whether the reference deleted it: not when it holds no such upload of the owner, or
	 *         a request keeps writing it
	 */
	public boolean deleteUpload(String owner, String uploadId)
	{
		ResumableUpload upload = find(uploadId, owner);
		return upload != null && delete(upload);
	}

	/**
	 * An event of an upload, for the tracker.
	 */
	@FunctionalInterface
	interface UploadEvent
	{
		void tell(IUploadsTracker tracker, String context, TrackedUpload upload);
	}

	/**
	 * Tells the tracker of an event of an upload, logging what it throws.
	 */
	void track(ResumableUpload upload, UploadEvent event)
	{
		try
		{
			event.tell(uploadsTracker, upload.getContext(), TrackedUpload.of(upload));
		}
		catch (RuntimeException e)
		{
			LOG.warn("The tracker of the uploads failed on {}", upload, e);
		}
	}

	/**
	 * @return how often, at most, the requests look for uploads that expired
	 */
	public Duration getExpiryCheckInterval()
	{
		return expiryCheckInterval;
	}

	/**
	 * Sets how often, at most, the requests look for uploads that expired, and delete them: each
	 * look goes through all the uploads.
	 *
	 * @param expiryCheckInterval
	 *            the interval, {@link #DEFAULT_EXPIRY_CHECK_INTERVAL} by default;
	 *            {@link Duration#ZERO} to look at every request
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setExpiryCheckInterval(Duration expiryCheckInterval)
	{
		Args.notNull(expiryCheckInterval, "expiryCheckInterval");
		Args.isTrue(!expiryCheckInterval.isNegative(), "expiryCheckInterval must not be negative");
		this.expiryCheckInterval = expiryCheckInterval;
		return this;
	}

	/**
	 * @return how long a request waits for another one writing the same upload
	 */
	public Duration getLockTimeout()
	{
		return lockTimeout;
	}

	/**
	 * Sets how long a request waits for another one writing the same upload, a chunk sent again
	 * while the first one still arrives for example, before it is answered {@code 423 Locked}, which
	 * the browser tries again after a while.
	 *
	 * @param lockTimeout
	 *            the timeout, {@link #DEFAULT_LOCK_TIMEOUT} by default
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setLockTimeout(Duration lockTimeout)
	{
		Args.notNull(lockTimeout, "lockTimeout");
		Args.isTrue(!lockTimeout.isNegative(), "lockTimeout must not be negative");
		this.lockTimeout = lockTimeout;
		return this;
	}

	/**
	 * @return the length of the header {@code Upload-Metadata} at most
	 */
	public int getMaxMetadataLength()
	{
		return maxMetadataLength;
	}

	/**
	 * Sets the length of the header {@code Upload-Metadata} at most, in characters: the name and
	 * the type of the file, and what else a client of its own tells, in base64. A longer one is
	 * refused with {@code 400 Bad Request}.
	 *
	 * @param maxMetadataLength
	 *            the length, {@link #DEFAULT_MAX_METADATA_LENGTH} by default
	 * @return {@code this}
	 */
	public ResumableUploadResourceReference setMaxMetadataLength(int maxMetadataLength)
	{
		Args.isTrue(maxMetadataLength > 0, "maxMetadataLength must be positive");
		this.maxMetadataLength = maxMetadataLength;
		return this;
	}

	/**
	 * Lets the bytes of a chunk be read through another stream, one counting or scanning them for
	 * example. Returns the body itself by default.
	 *
	 * @param body
	 *            the body of the request
	 * @param upload
	 *            the upload the chunk belongs to
	 * @return the stream to read
	 */
	protected InputStream wrapBody(InputStream body, ResumableUpload upload)
	{
		return body;
	}

	ResumableUploadResourceReference setClock(Clock clock)
	{
		this.clock = Args.notNull(clock, "clock");
		return this;
	}

	Instant now()
	{
		return clock.instant();
	}

	/**
	 * Names who the uploads of the current request belong to. Only the requests of the same owner
	 * find an upload, and the limits per session count the uploads of an owner together.
	 * <p>
	 * By default the owner is the HTTP session: an upload ends with its session, and a request
	 * without a session creates none. An application whose users sign in may return the id of the
	 * signed-in user instead, and the session's id, {@code super.getCurrentOwner()}, for the users
	 * who did not. An upload of a user then outlives the session that created it: after the session
	 * expired, during a long upload or a connection broken for long, the user signs in again and
	 * the field, as long as it is {@link ResumableUploadToResourceField#setRemembering(boolean)
	 * remembering}, offers to go on with it once the user chooses the file again. It is deleted
	 * once the browser sent it nothing for {@link #setOwnerExpiry(Duration)}, which such an
	 * application may want to set to longer than {@link #setExpiry(Duration)}.
	 * <p>
	 * An upload that outlives its session is worth going on with only where its file still has a
	 * purpose: the field offers it in the same place of a later page, see
	 * {@link ResumableUploadToResourceField#getRememberedAs()}, which by default does not tell
	 * apart the records a page edits. Where the work of the page ends with the session, the unsaved
	 * steps of an Ajax wizard for example, the session is the right owner: its uploads end with it.
	 * <p>
	 * The owner must not come from the browser.
	 * <p>
	 * The grant of a field is made for the owner of the request that renders its page, and the
	 * server refuses it to any other: once the owner of the page changes, as the user signs in for
	 * example, the field has to be rendered again to upload.
	 *
	 * @return the owner, {@code null} when the current request may not upload
	 */
	protected String getCurrentOwner()
	{
		return currentSessionId();
	}

	/**
	 * @return the id of the HTTP session of the current request, {@code null} without one
	 */
	static String currentSessionId()
	{
		if (!Application.exists() || RequestCycle.get() == null)
		{
			return null;
		}
		Session session = Session.get();
		return session.isTemporary() ? null : session.getId();
	}

	/**
	 * @return the grant of a field, which expires after {@link #getGrantExpiry()}
	 */
	UploadGrant newGrant(String uploadFieldId, String context, Bytes maxSize, Bytes fileMaxSize,
		long fileCountMax)
	{
		Args.isTrue(UploadGrant.isLine(uploadFieldId), "uploadFieldId must be a single line");
		Args.isTrue(UploadGrant.isLine(context), "context must be a single line");
		return UploadGrant.of(uploadFieldId, context, maxSize, fileMaxSize, fileCountMax,
			now().plus(getGrantExpiry()));
	}

	/**
	 * @return the upload of a name if it belongs to the owner, {@code null} otherwise
	 */
	ResumableUpload find(String id, String owner)
	{
		if (id == null || owner == null || !NAME_PATTERN.matcher(id).matches())
		{
			return null;
		}
		ResumableUpload upload = uploads.get(id);
		return upload != null && upload.getOwner().equals(owner) ? upload : null;
	}

	/**
	 * @return the unfinished uploads of an owner
	 */
	private List<ResumableUpload> unfinishedUploadsOf(String owner)
	{
		return uploads.values()
			.stream()
			.filter(upload -> upload.getOwner().equals(owner) && !upload.isStored())
			.toList();
	}

	/**
	 * Creates an upload, unless it breaks a limit.
	 *
	 * @return the upload
	 * @throws Refusal
	 *             when it breaks a limit
	 */
	synchronized ResumableUpload create(String owner, UploadGrant grant, long length,
		Map<String, String> metadata) throws Refusal
	{
		listen();
		if (length > Math.min(maxFileSize.bytes(), grant.maxFileSize()))
		{
			throw new Refusal(413, ResumableUploadToResourceField.UPLOAD_TOO_LARGE_RESOURCE_KEY);
		}
		List<ResumableUpload> own = unfinishedUploadsOf(owner);
		if (own.size() >= maxUploadsPerSession)
		{
			throw new Refusal(403, ResumableUploadToResourceField.TOO_MANY_UPLOADS_RESOURCE_KEY);
		}
		long ownBytes = own.stream().mapToLong(ResumableUpload::getLength).sum();
		if (ownBytes + length > getMaxBytesPerSession().bytes())
		{
			throw new Refusal(413, ResumableUploadToResourceField.TOO_MUCH_RESOURCE_KEY);
		}
		long allBytes = uploads.values()
			.stream()
			.filter(upload -> !upload.isStored())
			.mapToLong(ResumableUpload::getLength)
			.sum();
		if (allBytes + length > getMaxTotalBytes().bytes() || !hasRoom(length))
		{
			throw new Refusal(507, ResumableUploadToResourceField.NO_ROOM_RESOURCE_KEY);
		}
		byte[] random = new byte[16];
		RANDOM.nextBytes(random);
		String id = HexFormat.of().formatHex(random);
		try
		{
			Path file = Files.createFile(folder.toPath().resolve(id));
			ResumableUpload upload = new ResumableUpload(id, owner, grant.context(),
				owner.equals(currentSessionId()), grant.uploadFieldId(), length, metadata, file,
				now());
			uploads.put(id, upload);
			return upload;
		}
		catch (IOException e)
		{
			LOG.warn("Could not create the file of an upload in {}", folder, e);
			throw new Refusal(507, ResumableUploadToResourceField.NO_ROOM_RESOURCE_KEY);
		}
	}

	/**
	 * @return whether the disk of the folder has room for more bytes, leaving free the space it
	 *         has to
	 */
	private boolean hasRoom(long length)
	{
		try
		{
			Files.createDirectories(folder.toPath());
			long usable = Files.getFileStore(folder.toPath()).getUsableSpace();
			long pending = uploads.values()
				.stream()
				.filter(upload -> !upload.isStored())
				.mapToLong(upload -> upload.getLength() - upload.getOffset())
				.sum();
			return usable - pending - length >= minFreeSpace.bytes();
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * Hands the whole file of an upload to the file manager, for an upload field.
	 *
	 * @param upload
	 *            the upload, whose lock the current thread holds
	 * @param uploadFieldId
	 *            the id of the field
	 */
	void store(ResumableUpload upload, String uploadFieldId)
	{
		FileUpload file = new FileUpload(new ReceivedFileItem(upload));
		String name = fileManager.store(file, uploadFieldId);
		upload.deleteFile();
		upload.stored(uploadFieldId, name, now());
		track(upload, IUploadsTracker::onStored);
	}

	/**
	 * Deletes an upload and its bytes, waiting a moment for a request writing them.
	 *
	 * @return whether it was deleted: not while a request keeps writing it
	 */
	boolean delete(ResumableUpload upload)
	{
		try
		{
			if (!upload.lock.tryLock(lockTimeout.toMillis(),
				TimeUnit.MILLISECONDS))
			{
				return false;
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return false;
		}
		try
		{
			forget(upload);
			return true;
		}
		finally
		{
			upload.lock.unlock();
		}
	}

	private void forget(ResumableUpload upload)
	{
		if (uploads.remove(upload.getId(), upload))
		{
			track(upload, IUploadsTracker::onDeleted);
		}
		upload.deleteFile();
	}

	/**
	 * Deletes the uploads the browser sent nothing to for longer than the expiry, at most once a
	 * minute unless forced, but not those a request is writing.
	 *
	 * @param force
	 *            whether to look now, however recently it looked
	 */
	void deleteExpired(boolean force)
	{
		Instant now = now();
		if (!force && swept.plus(expiryCheckInterval).isAfter(now))
		{
			return;
		}
		swept = now;
		for (ResumableUpload upload : new ArrayList<>(uploads.values()))
		{
			if (!expiresAt(upload).isAfter(now) && upload.lock.tryLock())
			{
				try
				{
					forget(upload);
				}
				finally
				{
					upload.lock.unlock();
				}
			}
		}
	}

	/**
	 * @return when an upload expires, unless the browser sends something
	 */
	Instant expiresAt(ResumableUpload upload)
	{
		return upload.getTouched().plus(upload.isEndingWithSession() ? expiry : getOwnerExpiry());
	}

	/**
	 * @return the number of uploads known, finished or not, of all owners
	 */
	int size()
	{
		return uploads.size();
	}

	/**
	 * Deletes the uploads the sessions that end own, and all of them when the application stops.
	 */
	private void listen()
	{
		if (!listening.compareAndSet(false, true))
		{
			return;
		}
		Application application = Application.get();
		application.getSessionStore().registerUnboundListener(sessionId -> {
			for (ResumableUpload upload : new ArrayList<>(uploads.values()))
			{
				if (upload.isEndingWithSession() && upload.getOwner().equals(sessionId))
				{
					forget(upload);
				}
			}
		});
		application.getApplicationListeners().add(new IApplicationListener()
		{
			@Override
			public void onBeforeDestroyed(Application destroyed)
			{
				new ArrayList<>(uploads.values()).forEach(upload -> forget(upload));
				deleteFiles();
				if (ownFolder && !folder.delete())
				{
					LOG.debug("Could not delete the folder of resumable uploads {}", folder);
				}
				try
				{
					uploadsTracker.destroy();
				}
				catch (RuntimeException e)
				{
					LOG.warn("The tracker of the uploads failed as the application stopped", e);
				}
			}
		});
	}

	/**
	 * Deletes the files of uploads in the folder.
	 */
	private void deleteFiles()
	{
		if (!folder.isDirectory())
		{
			return;
		}
		try (Stream<Path> files = Files.list(folder.toPath()))
		{
			for (Path file : files.filter(
				path -> NAME_PATTERN.matcher(path.getFileName().toString()).matches()).toList())
			{
				Files.deleteIfExists(file);
			}
		}
		catch (IOException e)
		{
			LOG.warn("Could not delete the files of resumable uploads in {}", folder, e);
		}
	}

	/**
	 * The tracker by default: it keeps nothing, and lists and deletes the uploads the reference
	 * holds.
	 */
	private final class HeldUploads implements IUploadsTracker
	{
		private static final long serialVersionUID = 1L;

		@Override
		public List<TrackedUpload> getUploads(String context, String owner)
		{
			return ResumableUploadResourceReference.this.getUploads(context, owner);
		}

		@Override
		public void delete(String context, String owner, String uploadId)
		{
			deleteUpload(owner, uploadId);
		}
	}

	/**
	 * A limit an upload breaks: the status the browser is told, and the resource key of the
	 * error its field reports.
	 */
	static final class Refusal extends Exception
	{
		private static final long serialVersionUID = 1L;

		final int status;

		final String key;

		Refusal(int status, String key)
		{
			super(key, null, false, false);
			this.status = status;
			this.key = key;
		}
	}
}
