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
 * Thread safe implementation of {@link FileChannel} pool.
 * 
 * @author Matej Knopp
 */
class FileChannelPool
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


	private void reduceChannels()
	{
		int channelsToReduce = nameToChannel.size() - capacity + 1;

		while (channelsToReduce > 0 && idleChannels.isEmpty() == false)
		{
			FileChannel channel = (FileChannel)idleChannels.getFirst();
			String channelName = (String)channelToName.get(channel);
			idleChannels.removeFirst();
			nameToChannel.remove(channelName);
			channelToName.remove(channel);
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

		if (count.intValue() == 0)
		{
			channelToUseCount.remove(channel);
			if (channelsToDeleteOnReturn.contains(channel))
			{
				closeAndDelete(channel);
			}
			else
			{
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
		String name = (String) channelToName.get(channel);
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
	 * 
	 */
	public synchronized void destroy() 
	{
		log.debug("Destroying FileChannel pool");
		for (Iterator i = channelToName.keySet().iterator(); i.hasNext(); )
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