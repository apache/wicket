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
package org.apache.wicket.util.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * <p>
 * An output stream which will retain data in memory until a specified threshold is reached, and
 * only then commit it to disk. If the stream is closed before the threshold is reached, the data
 * will not be written to disk at all.
 * </p>
 * <p>
 * This class originated in FileUpload processing. In this use case, you do not know in advance the
 * size of the file being uploaded. If the file is small you want to store it in memory (for speed),
 * but if the file is large you want to store it to file (to avoid memory issues).
 * </p>
 * 
 * @author <a href="mailto:martinc@apache.org">Martin Cooper</a>
 */
public class DeferredFileOutputStream extends ThresholdingOutputStream
{

	// ----------------------------------------------------------- Data members


	/**
	 * The output stream to which data will be written at any given time. This will always be one of
	 * <code>memoryOutputStream</code> or <code>diskOutputStream</code>.
	 */
	private OutputStream currentOutputStream;


	/**
	 * The output stream to which data will be written prior to the threshold being reached.
	 */
	private ByteArrayOutputStream memoryOutputStream;


	/**
	 * The file to which output will be directed if the threshold is exceeded.
	 */
	private File outputFile;


	private final FileFactory fileFactory;


	// ----------------------------------------------------------- Constructors


	/**
	 * Constructs an instance of this class which will trigger an event at the specified threshold,
	 * and save data to a file beyond that point.
	 * 
	 * @param threshold
	 *            The number of bytes at which to trigger an event.
	 * @param outputFile
	 *            The file to which data is saved beyond the threshold.
	 */
	public DeferredFileOutputStream(int threshold, File outputFile)
	{
		super(threshold);
		if (outputFile == null)
			throw new IllegalArgumentException("output file must be specified");
		this.outputFile = outputFile;
		fileFactory = null;

		memoryOutputStream = new ByteArrayOutputStream();
		currentOutputStream = memoryOutputStream;
	}

	/**
	 * Constructs an instance of this class which will trigger an event at the specified threshold,
	 * and save data to a file beyond that point.
	 * 
	 * @param threshold
	 *            The number of bytes at which to trigger an event.
	 * @param fileFactory
	 *            The FileFactory to create the file.
	 */
	public DeferredFileOutputStream(int threshold, FileFactory fileFactory)
	{
		super(threshold);
		if (fileFactory == null)
			throw new IllegalArgumentException("FileFactory must be specified");
		this.fileFactory = fileFactory;

		memoryOutputStream = new ByteArrayOutputStream();
		currentOutputStream = memoryOutputStream;
	}


	// --------------------------------------- ThresholdingOutputStream methods


	/**
	 * Returns the data for this output stream as an array of bytes, assuming that the data has been
	 * retained in memory. If the data was written to disk, this method returns <code>null</code>.
	 * 
	 * @return The data for this output stream, or <code>null</code> if no such data is available.
	 */
	public byte[] getData()
	{
		if (memoryOutputStream != null)
		{
			return memoryOutputStream.toByteArray();
		}
		return null;
	}


	/**
	 * Returns the data for this output stream as a <code>File</code>, assuming that the data was
	 * written to disk. If the data was retained in memory, this method returns <code>null</code>.
	 * 
	 * @return The file for this output stream, or <code>null</code> if no such file exists.
	 */
	public File getFile()
	{
		return outputFile;
	}


	// --------------------------------------------------------- Public methods


	/**
	 * Determines whether or not the data for this output stream has been retained in memory.
	 * 
	 * @return <code>true</code> if the data is available in memory; <code>false</code> otherwise.
	 */
	public boolean isInMemory()
	{
		return (!isThresholdExceeded());
	}


	/**
	 * Returns the current output stream. This may be memory based or disk based, depending on the
	 * current state with respect to the threshold.
	 * 
	 * @return The underlying output stream.
	 * @exception IOException
	 *                if an error occurs.
	 */
	@Override
	protected OutputStream getStream() throws IOException
	{
		return currentOutputStream;
	}


	/**
	 * Switches the underlying output stream from a memory based stream to one that is backed by
	 * disk. This is the point at which we realize that too much data is being written to keep in
	 * memory, so we elect to switch to disk-based storage.
	 * 
	 * @exception IOException
	 *                if an error occurs.
	 */
	@Override
	protected void thresholdReached() throws IOException
	{
		byte[] data = memoryOutputStream.toByteArray();
		if (outputFile == null)
		{
			outputFile = fileFactory.createFile();
		}
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(data);
		currentOutputStream = fos;
		memoryOutputStream = null;
	}

	/**
	 * The file factory for this deferred file output stream.
	 * 
	 * @author jcompagner
	 */
	public interface FileFactory
	{
		/**
		 * @return the file to use for disk cache
		 */
		File createFile();
	}
}
