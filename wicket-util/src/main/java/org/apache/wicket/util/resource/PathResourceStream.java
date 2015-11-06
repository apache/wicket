package org.apache.wicket.util.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;

/**
 * A PathResourceStream is an IResourceStream implementation for Java NIO paths.
 * 
 * @see org.apache.wicket.util.resource.IResourceStream
 * @see org.apache.wicket.util.watch.IModifiable
 * @author Tobias Soloschenko
 */
public class PathResourceStream extends AbstractResourceStream
	implements
		IFixedLocationResourceStream
{
	private static final long serialVersionUID = 1L;

	/** Any associated path */
	private final Path path;

	/** Resource stream */
	private transient InputStream inputStream;

	/**
	 * Constructor.
	 * 
	 * @param path
	 *            {@link Path} containing resource
	 */
	public PathResourceStream(final Path path)
	{
		Args.notNull(path, "path");
		this.path = path;
	}

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            {@link java.io.File} containing resource
	 */
	public PathResourceStream(final java.io.File file)
	{
		Args.notNull(file, "file");
		this.path = file.toPath();
	}

	/**
	 * Constructor.
	 * 
	 * @param file
	 *            {@link File} containing resource
	 */
	public PathResourceStream(final File file)
	{
		Args.notNull(file, "file");
		this.path = file.toPath();
	}

	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		if (inputStream == null)
		{
			try
			{
				inputStream = Files.newInputStream(path);
			}
			catch (IOException e)
			{
				throw new ResourceStreamNotFoundException("Input stream of path " + path +
					" could not be acquired", e);
			}
		}
		return inputStream;
	}

	@Override
	public void close() throws IOException
	{
		if (inputStream != null)
		{
			inputStream.close();
			inputStream = null;
		}
	}

	@Override
	public String getContentType()
	{
		try
		{
			String contentType = Files.probeContentType(path);
			if (contentType == null)
			{
				contentType = URLConnection.getFileNameMap().getContentTypeFor(
					path.getFileName().toString());
			}
			return contentType;
		}
		catch (IOException e)
		{
			throw new RuntimeException("Content type of path " + path + " could not be acquired", e);
		}
	}

	/**
	 * @return The path this resource resides in, if any.
	 */
	public final Path getPath()
	{
		return path;
	}

	@Override
	public Time lastModifiedTime()
	{
		try
		{
			BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
			FileTime lastModifiedTime = attributes.lastModifiedTime();
			long millis = lastModifiedTime.toMillis();
			return Time.millis(millis);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Modification time of path " + path +
				" could not be acquired", e);
		}
	}

	@Override
	public Bytes length()
	{
		try
		{
			BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
			long size = attributes.size();
			return Bytes.bytes(size);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Length of path " + path + " could not be acquired", e);
		}
	}

	@Override
	public String locationAsString()
	{
		return path.toString();
	}

	@Override
	public String toString()
	{
		return locationAsString();
	}
}
