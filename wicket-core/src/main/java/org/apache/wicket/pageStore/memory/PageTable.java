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
package org.apache.wicket.pageStore.memory;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.util.io.IClusterable;

/**
 * A structure that holds page id => pageAsBytes.
 * 
 * <p>
 * Additionally it has an index of the least recently used pages
 */
public class PageTable implements IClusterable
{
	private static final long serialVersionUID = 1L;


	/**
	 * Holds the index of last/least recently used page ids. The most recently used page id is in
	 * the tail, the least recently used is in the head.
	 */
	/*
	 * Can be replaced later with PriorityQueue to deal with lightweight (Ajax) and heavyweight
	 * pages
	 */
	private final Queue<Integer> index;

	/**
	 * The actual container for the pages.
	 * 
	 * <p>
	 * page id => page as bytes
	 */
	private final ConcurrentMap<Integer, byte[]> pages;

	public PageTable()
	{
		pages = new ConcurrentHashMap<>();
		index = new ConcurrentLinkedQueue<>();
	}

	public void storePage(Integer pageId, byte[] pageAsBytes)
	{
		synchronized (index)
		{
			pages.put(pageId, pageAsBytes);

			updateIndex(pageId);
		}
	}

	public byte[] getPage(final Integer pageId)
	{
		synchronized (index)
		{
			updateIndex(pageId);

			return pages.get(pageId);
		}
	}

	public byte[] removePage(Integer pageId)
	{
		synchronized (index)
		{
			index.remove(pageId);

			return pages.remove(pageId);
		}
	}

	public void clear()
	{
		synchronized (index)
		{
			index.clear();
			pages.clear();
		}
	}

	public int size()
	{
		return pages.size();
	}

	public Integer getOldest()
	{
		return index.peek();
	}

	/**
	 * Updates the index of last/least recently used pages by removing the page id from the index
	 * (in case it is already in) and (re-)adding it at the head
	 * 
	 * @param pageId
	 *            the id of a recently used page
	 */
	private void updateIndex(Integer pageId)
	{
		index.remove(pageId);
		index.offer(pageId);
	}

}
