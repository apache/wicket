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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.util.collections.IntHashMap;

/**
 * @author Matej Knopp
 */
public class PageWindowManager
{
	private static class PageWindowInternal
	{
		private int pageId;
		private short versionNumber;
		private short ajaxVersionNumber;
		private int filePartOffset;
		private int filePartSize;
	}

	private List /* <PageWindowInternal> */windows = new ArrayList();

	// map from page id to list of pagewindow indices (refering to the windows
	// list)
	private IntHashMap /* <int, List<Integer> */idToWindowIndices = null;

	private void putWindowIndex(int pageId, int windowIndex)
	{
		if (pageId != -1 && idToWindowIndices != null)
		{
			List indices = (List)idToWindowIndices.get(pageId);
			Integer index = new Integer(windowIndex);
			if (indices == null)
			{
				indices = new ArrayList();
				indices.add(index);
				idToWindowIndices.put(pageId, indices);
			}
			else if (indices.contains(index) == false)
			{
				indices.add(index);
			}
		}
	}

	private void removeWindowIndex(int pageId, int windowIndex)
	{
		if (pageId != -1 && idToWindowIndices != null)
		{
			List indices = (List)idToWindowIndices.get(pageId);
			if (indices != null)
			{
				indices.remove(new Integer(windowIndex));
			}
		}
	}
	
	private void rebuildIndices() {
		idToWindowIndices = new IntHashMap();
		for (int i = 0; i < windows.size(); ++i )
		{
			PageWindowInternal window = (PageWindowInternal) windows.get(i);
			putWindowIndex(window.pageId, i);
		}
	}

	private int getWindowIndex(int pageId, int versionNumber, int ajaxVersionNumber)
	{
		int index = -1;
		
		if (idToWindowIndices == null)
		{
			rebuildIndices();
		}
		
		List indices = (List)idToWindowIndices.get(pageId);
		if (indices != null)
		{
			for (Iterator i = indices.iterator(); i.hasNext();)
			{
				Integer currentIndex = (Integer)i.next();
				PageWindowInternal window = (PageWindowInternal)windows
						.get(currentIndex.intValue());
				if (window.pageId == pageId && window.versionNumber == versionNumber &&
						window.ajaxVersionNumber == ajaxVersionNumber)
				{
					index = currentIndex.intValue();
					break;
				}
			}
		}
		return index;
	}

	private int indexPointer = -1;

	private int incrementIndexPointer()
	{
		if (maxSize > 0 && totalSize >= maxSize && indexPointer == windows.size() - 1)
		{
			indexPointer = 0;
		}
		else
		{
			++indexPointer;
		}
		return indexPointer;
	}

	private int getWindowFileOffset(int index)
	{
		if (index > 0)
		{
			PageWindowInternal window = (PageWindowInternal)windows.get(index - 1);
			return window.filePartOffset + window.filePartSize;
		}
		else
		{
			return 0;
		}
	}

	private void splitWindow(int index, int size)
	{
		PageWindowInternal window = (PageWindowInternal)windows.get(index);
		int delta = window.filePartSize - size;

		if (index == windows.size() - 1)
		{
			// if this is last window
			totalSize -= delta;
			window.filePartSize = size;
		}
		else if (window.filePartSize != size)
		{
			PageWindowInternal newWindow = new PageWindowInternal();
			newWindow.pageId = -1;
			window.filePartSize = size;

			windows.add(index + 1, newWindow);

			newWindow.filePartOffset = getWindowFileOffset(index + 1);
			newWindow.filePartSize = delta;
		}
		
		idToWindowIndices = null;
	}

	private void mergeWindowWithNext(int index)
	{
		if (index < windows.size() - 1)
		{
			PageWindowInternal window = (PageWindowInternal)windows.get(index);
			PageWindowInternal next = (PageWindowInternal)windows.get(index + 1);
			window.filePartSize += next.filePartSize;
			
			windows.remove(index + 1);
			idToWindowIndices = null; // reset index
		}
	}

	private void adjustWindowSize(int index, int size)
	{
		PageWindowInternal window = (PageWindowInternal)windows.get(index);
		if (index == windows.size() - 1)
		{
			int delta = size - window.filePartSize;
			totalSize += delta;
			window.filePartSize = size;
		}
		else
		{
			while (window.filePartSize < size && index < windows.size() - 1)
			{
				mergeWindowWithNext(index);
			}
			// done merging - do we have anough room ?
			if (window.filePartSize < size)
			{
				// no, this is the last window
				int delta = size - window.filePartSize;
				totalSize += delta;
				window.filePartSize = size;
			}
			else
			{
				// yes, we might want to split the window, so that we don't lose
				// space when
				// the created window was too big
				splitWindow(index, size);
			}
		}

		window.pageId = -1;
	}

	private PageWindowInternal allocatePageWindow(int index, int size)
	{
		final PageWindowInternal window;
		if (index == windows.size())
		{
			// new page window
			window = new PageWindowInternal();
			window.filePartOffset = getWindowFileOffset(index);
			totalSize += size;
			window.filePartSize = size;
			windows.add(window);
		}
		else
		{
			// get the window
			window = (PageWindowInternal)windows.get(index);
			if (window.filePartSize != size)
			{
				adjustWindowSize(index, size);
			}
		}


		return window;
	}

	/**
	 * @author Matej Knopp
	 */
	public static class PageWindow
	{
		private final PageWindowInternal pageWindowInternal;

		private PageWindow(PageWindowInternal pageWindowInternal)
		{
			this.pageWindowInternal = pageWindowInternal;
		}

		/**
		 * @return
		 */
		public int getPageId()
		{
			return pageWindowInternal.pageId;
		}

		/**
		 * @return
		 */
		public int getVersionNumber()
		{
			return pageWindowInternal.versionNumber;
		}
		
		/**
		 * @return
		 */
		public int getAjaxVersionNumber() 
		{
			return pageWindowInternal.ajaxVersionNumber;
		}
		
		/**
		 * @return
		 */
		public int getFilePartOffset() 
		{
			return pageWindowInternal.filePartOffset;
		}
		
		/**
		 * @return
		 */
		public int getFilePartSize() 
		{
			return pageWindowInternal.filePartSize;
		}
	}

	/**
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 * @param size
	 * @return
	 */
	public PageWindow savePage(int pageId, int versionNumber, int ajaxVersionNumber,
			int size)
	{
		int index = getWindowIndex(pageId, versionNumber, ajaxVersionNumber);

		// if we found the page window, mark it as invalid
		if (index != -1)
		{
			removeWindowIndex(pageId, index);
			((PageWindowInternal)windows.get(index)).pageId = -1;
		}

		// if we are not going to reuse a page window (because it's not on
		// indexPointor position
		// or because we didn't find it), increment the indexPointer
		if (index == -1 || index != indexPointer)
		{
			index = incrementIndexPointer();
		}

		PageWindowInternal window = allocatePageWindow(index, size);
		window.pageId = pageId;
		window.versionNumber = (short)versionNumber;
		window.ajaxVersionNumber = (short)ajaxVersionNumber;

		putWindowIndex(pageId, index);
		return new PageWindow(window);
	}

	/**
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 * @return
	 */
	public PageWindow getPageWindow(int pageId, int versionNumber, int ajaxVersionNumber)
	{
		int index = getWindowIndex(pageId, versionNumber, ajaxVersionNumber);
		if (index != -1)
		{
			return new PageWindow((PageWindowInternal)windows.get(index));
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 */
	public void removePage(int pageId, int versionNumber, int ajaxVersionNumber)
	{
		int index = getWindowIndex(pageId, versionNumber, ajaxVersionNumber);
		if (index != -1)
		{
			PageWindowInternal window = (PageWindowInternal) windows.get(index);
			removeWindowIndex(pageId, index);
			if (index == windows.size() - 1)
			{
				windows.remove(index);
				totalSize -= window.filePartSize;
			}
			else
			{
				window.pageId = -1;
			}
		}
	}
	
	/**
	 * @param pageId
	 */
	public void removePage(int pageId)
	{
		if (idToWindowIndices == null)
		{
			rebuildIndices();
		}
		List indices = (List)idToWindowIndices.get(pageId);
		
		for (Iterator i = indices.iterator(); i.hasNext(); )
		{
			PageWindowInternal window = (PageWindowInternal) i.next();
			removePage(window.pageId, window.versionNumber, window.ajaxVersionNumber);
		}
	}
	
	/**
	 * Construct.
	 * @param maxSize
	 */
	public PageWindowManager(int maxSize)
	{
		this.maxSize = maxSize;
	}
	
	/**
	 * @return
	 */
	public int getTotalSize()
	{
		return totalSize;
	}

	private int totalSize = 0;

	private final int maxSize;
}
