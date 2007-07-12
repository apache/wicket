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
package org.apache.wicket.protocol.http.pagestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread safe pool of {@link FileChannel} objects.
 * <p>
 * Opening and closing file is an expensive operation and under certain
 * circumstances this can singificantly harm performances, because on every
 * close the filesystem cache might be flushed.
 * <p>
 * To minimize the negative impact opened files can be pooled, which is a
 * responsibility of {@link FileChannelPool} class.
 * <p>
 * {@link FileChannelPool} allows to specify maximum number of opened
 * {@link FileChannel}s.
 * <p>
 * Note that under certain circumtances (when there are no empty slots in pool)
 * the initial capacity can be exceeded (more files are opened then the
 * specified capacity is). If this happens, a warning is written to log, as this
 * probably means that there is a problem with page store.
 * 
 * @author Matej Knopp
 */
public class FileChannelPool
{

	private final Map /* <String, FileChannel> */nameToChannel = new HashMap();
	private final Map /* <FileChannel, String> */channelToName = new HashMap();
	private final Map /* <FileChannel, Integer> */channelToUseCount = new HashMap();
	private final LinkedList /* <FileChannel> */idleChannels = new LinkedList();
	private final Set /* <FileChannel> */channelsToDeleteOnReturn = new HashSet();

	private final int capacity;

	/**
	 * Construct.
	 * 
	 * @param capacity
	 *            Maximum number of opened file channels.
	 */
	public FileChannelPool(int capacity)
	{
		this.capacity = capacity;

		if (capacity < 1)
		{
			throw new IllegalArgumentException("Capacity must be at least one.");
		}

		log.debug("Starting file channel pool with capacity of " + capacity + " channels");
	};

	/**
	 * Creates a new file channel with specified file name.
	 * 
	 * @param fileName
	 * @param createIfDoesNotExist
	 *            in case the file does not exist this parameter determines if
	 *            the file should be created
	 * @return
	 */
	private FileChannel newFileChannel(String fileName, boolean createIfDoesNotExist)
	{
		File file = new File(fileName);
		if (file.exists() == false && createIfDoesNotExist == false)
		{
			return null;
		}

		try
		{
			FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
			return channel;
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}


	/**
	 * Tries to reduce (close) enouch channels to have at least one channel free
	 * (so that there are maximum capacity - 1 opened channel).
	 */
	private void reduceChannels()
	{
		// how much channels we need to close?
		int channelsToReduce = nameToChannel.size() - capacity + 1;

		// while there are still channels to close and we have still idle
		// channels left
		while (channelsToReduce > 0 && idleChannels.isEmpty() == false)
		{
			FileChannel channel = (FileChannel)idleChannels.getFirst();
			String channelName = (String)channelToName.get(channel);

			// remove oldest idle channel
			idleChannels.removeFirst();
			nameToChannel.remove(channelName);
			channelToName.remove(channel);

			// this shouldn't really happen
			if (channelToUseCount.get(channel) != null)
			{
				log.warn("Channel " + channelName + " is both idle and in use at the same time!");
				channelToUseCount.remove(channel);
			}

			try
			{
				channel.close();
			}
			catch (IOException e)
			{
				log.error("Error closing file channel", e);
			}
			--channelsToReduce;
		}

		if (channelsToReduce > 0)
		{
			log.warn("Unable to reduce enough channels, no idle channels left to remove.");
		}
	}

	/**
	 * Returns a channel for given file. If the file doesn't exist, the
	 * createIfDoesNotExit attribute specifies if the file should be crated.
	 * 
	 * Do NOT call close on the returned chanel. Instead call
	 * {@link #returnFileChannel(FileChannel)}
	 * 
	 * @param fileName
	 * @param createIfDoesNotExist
	 * @return
	 */
	public synchronized FileChannel getFileChannel(String fileName, boolean createIfDoesNotExist)
	{
		FileChannel channel = (FileChannel)nameToChannel.get(fileName);

		if (channel == null)
		{
			channel = newFileChannel(fileName, createIfDoesNotExist);

			if (channel != null)
			{
				// we need to create new channel
				// first, check how many channels we have already opened
				if (nameToChannel.size() >= capacity)
				{
					reduceChannels();
				}

				nameToChannel.put(fileName, channel);
				channelToName.put(channel, fileName);
			}
		}

		if (channel != null)
		{
			// increase the usage count for this channel

			Integer count = (Integer)channelToUseCount.get(channel);
			if (count == null || count.intValue() == 0)
			{
				channelToUseCount.put(channel, new Integer(1));
				idleChannels.remove(channel);
			}
			else
			{
				count = new Integer(count.intValue() + 1);
				channelToUseCount.put(channel, count);
			}
		}

		return channel;
	}

	/**
	 * Returns the channel to the pool. It is necessary to call this for every
	 * channel obtained by calling {@link #getFileChannel(String, boolean)}.
	 * 
	 * @param channel
	 */
	public synchronized void returnFileChannel(FileChannel channel)
	{
		Integer count = (Integer)channelToUseCount.get(channel);

		if (count == null || count.intValue() == 0)
		{
			throw new IllegalArgumentException("Trying to return unused channel");
		}

		count = new Integer(count.intValue() - 1);

		// decrease the usage count
		if (count.intValue() == 0)
		{
			channelToUseCount.remove(channel);
			if (channelsToDeleteOnReturn.contains(channel))
			{
				closeAndDelete(channel);
			}
			else
			{
				// this was the last usage, add chanel to idle channels
				idleChannels.addLast(channel);
			}
		}
		else
		{
			channelToUseCount.put(channel, count);
		}
	}

	private void closeAndDelete(FileChannel channel)
	{
		channelsToDeleteOnReturn.remove(channel);
		String name = (String)channelToName.get(channel);
		channelToName.remove(channel);

		channelToUseCount.remove(channel);
		idleChannels.remove(channel);

		try
		{
			channel.close();
		}
		catch (IOException e)
		{
			log.error("Error closing file channel", e);
		}

		File file = new File(name);
		file.delete();
	}

	/**
	 * Closes the file channel with given name and removes it from pool. Also
	 * removes the file from file system. If the channel is in use, the pool
	 * first waits until the chanel is returned to the pool and then closes it.
	 * 
	 * @param name
	 */
	public synchronized void closeAndDeleteFileChannel(String name)
	{
		FileChannel channel = (FileChannel)nameToChannel.get(name);
		if (channel != null)
		{
			nameToChannel.remove(name);

			Integer count = (Integer)channelToUseCount.get(channel);
			if (count != null && count.intValue() > 0)
			{
				channelsToDeleteOnReturn.add(channel);
			}
			else
			{
				closeAndDelete(channel);
			}
		}
		else
		{
			File file = new File(name);
			file.delete();
		}
	}

	/**
	 * Destroys the {@link FileChannel} pool and closes all opened channels.
	 */
	public synchronized void destroy()
	{
		log.debug("Destroying FileChannel pool");
		for (Iterator i = channelToName.keySet().iterator(); i.hasNext();)
		{
			FileChannel channel = (FileChannel)i.next();
			try
			{
				channel.close();
			}
			catch (IOException e)
			{
				log.error("Error closing file channel", e);
			}
		}
	}

	private static final Logger log = LoggerFactory.getLogger(FileChannelPool.class);
}