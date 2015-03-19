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
package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.Session;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;


/**
 * Holds information about an upload, also has useful querying methods.
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class UploadInfo implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private transient long timeStarted;
	private transient long totalBytes;
	private transient long bytesUploaded;

	/**
	 * @param totalBytes
	 */
	public UploadInfo(final int totalBytes)
	{
		timeStarted = System.currentTimeMillis();
		this.totalBytes = totalBytes;
	}

	/**
	 * @return bytes uploaded so far
	 */
	public long getBytesUploaded()
	{
		return bytesUploaded;
	}

	/**
	 * Sets bytes uploaded so far
	 * 
	 * @param bytesUploaded
	 */
	public void setBytesUploaded(final long bytesUploaded)
	{
		this.bytesUploaded = bytesUploaded;
	}

	/**
	 * @return human readable string of bytes uploaded so far
	 */
	public String getBytesUploadedString()
	{
		return Bytes.bytes(bytesUploaded).toString(Session.get().getLocale());
	}

	/**
	 * @return human readable string of total number of bytes
	 */
	public String getTotalBytesString()
	{
		return Bytes.bytes(totalBytes).toString(Session.get().getLocale());
	}

	/**
	 * @return total bytes in the upload
	 */
	public long getTotalBytes()
	{
		return totalBytes;
	}

	/**
	 * @return milliseconds elapsed since upload started
	 */
	public long getElapsedMilliseconds()
	{
		return System.currentTimeMillis() - timeStarted;
	}

	/**
	 * @return seconds elapsed since upload started
	 */
	public long getElapsedSeconds()
	{
		return getElapsedMilliseconds() / 1000L;
	}


	/**
	 * @return transfer rate in bits per second
	 */
	public long getTransferRateBPS()
	{
		return bytesUploaded / Math.max(getElapsedSeconds(), 1);
	}

	/**
	 * @return transfer rate in a human readable string
	 */
	public String getTransferRateString()
	{
		return Bytes.bytes(getTransferRateBPS()).toString(Session.get().getLocale()) + "/s";
	}

	/**
	 * @return percent of the upload completed
	 */
	public int getPercentageComplete()
	{
		if (totalBytes == 0)
		{
			return 100;
		}
		return (int)(((double)bytesUploaded / (double)totalBytes) * 100);

	}

	/**
	 * @return estimate of the remaining number of milliseconds
	 */
	public long getRemainingMilliseconds()
	{
		int percentageComplete = getPercentageComplete();


		long totalTime = ((getElapsedSeconds() * 100) / Math.max(percentageComplete, 1));
		long remainingTime = (totalTime - getElapsedSeconds());

		return remainingTime * 1000; // convert seconds to milliseconds and return
	}

	/**
	 * @return estimate of the remaining time in a human readable string
	 */
	public String getRemainingTimeString()
	{
		return Duration.milliseconds(getRemainingMilliseconds())
			.toString(Session.get().getLocale());
	}


}
