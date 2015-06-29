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
package org.apache.wicket.util.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An IResourceStream that ZIPs a directory's contents on the fly
 * 
 * <p>
 * <b>NOTE 1.</b> As a future improvement, cache a map of generated ZIP files for every directory
 * and use a Watcher to detect modifications in this directory. Using ehcache would be good for
 * that, but it's not in Wicket dependencies yet. <b>No caching of the generated ZIP files is done
 * yet.</b>
 * </p>
 * 
 * <p>
 * <b>NOTE 2.</b> As a future improvement, implement getLastModified() and request
 * ResourceStreamRequestTarget to generate Last-Modified and Expires HTTP headers. <b>No HTTP cache
 * headers are provided yet</b>. See WICKET-385
 * </p>
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class ZipResourceStream extends AbstractResourceStream
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ZipResourceStream.class);

	private final transient ByteArrayOutputStream bytearray;

	/**
	 * Construct.
	 * 
	 * @param dir
	 *            The directory where to look for files. The directory itself will not be included
	 *            in the ZIP.
	 * @param recursive
	 *            If true, all subdirs will be zipped as well
	 */
	public ZipResourceStream(final File dir, final boolean recursive)
	{
		Args.notNull(dir, "dir");
		Args.isTrue(dir.isDirectory(), "Not a directory: '{}'", dir);

		bytearray = new ByteArrayOutputStream();
		try
		{
			ZipOutputStream out = new ZipOutputStream(bytearray);
			try
			{
				zipDir(dir, out, "", recursive);
			} finally {
				out.close();
			}
		}
		catch (RuntimeException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Construct. Until Wicket 1.4-RC3 recursive zip was not supported. In order not to change the
	 * behavior, using this constructor will default to recursive == false.
	 * 
	 * @param dir
	 *            The directory where to look for files. The directory itself will not be included
	 *            in the ZIP.
	 */
	public ZipResourceStream(final File dir)
	{
		this(dir, false);
	}

	/**
	 * Recursive method for zipping the contents of a directory including nested directories.
	 * 
	 * @param dir
	 *            dir to be zipped
	 * @param out
	 *            ZipOutputStream to write to
	 * @param path
	 *            Path to nested dirs (used in resursive calls)
	 * @param recursive
	 *            If true, all subdirs will be zipped as well
	 * @throws IOException
	 */
	private static void zipDir(final File dir, final ZipOutputStream out, final String path,
		final boolean recursive) throws IOException
	{
		Args.notNull(dir, "dir");
		Args.isTrue(dir.isDirectory(), "Not a directory: '{}'", dir);

		String[] files = dir.list();

		int BUFFER = 2048;
		BufferedInputStream origin;
		byte data[] = new byte[BUFFER];

		if (files != null)
		{
			for (String file : files)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Adding: '{}'", file);
				}
				File f = new File(dir, file);
				if (f.isDirectory())
				{
					if (recursive)
					{
						zipDir(f, out, path + f.getName() + "/", recursive);
					}
				} else
				{
					out.putNextEntry(new ZipEntry(path + f.getName()));

					FileInputStream fi = new FileInputStream(f);
					origin = new BufferedInputStream(fi, BUFFER);

					try
					{
						int count;
						while ((count = origin.read(data, 0, BUFFER)) != -1)
						{
							out.write(data, 0, count);
						}
					} finally
					{
						origin.close();
					}
				}
			}
		}

		if ("".equals(path))
		{
			out.close();
		}
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#close()
	 */
	@Override
	public void close() throws IOException
	{
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getContentType()
	 */
	@Override
	public String getContentType()
	{
		return null;
	}

	/**
	 * @see org.apache.wicket.util.resource.IResourceStream#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws ResourceStreamNotFoundException
	{
		return new ByteArrayInputStream(bytearray.toByteArray());
	}

	/**
	 * @see org.apache.wicket.util.resource.AbstractResourceStream#length()
	 */
	@Override
	public Bytes length()
	{
		return Bytes.bytes(bytearray.size());
	}

	/**
	 * @see org.apache.wicket.util.resource.AbstractResourceStream#lastModifiedTime()
	 */
	@Override
	public Time lastModifiedTime()
	{
		return null;
	}
}
