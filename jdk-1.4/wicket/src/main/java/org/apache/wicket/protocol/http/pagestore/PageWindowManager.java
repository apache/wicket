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
 * Manages positions and size of serialized pages in the pagemap file.
 * <p>
 * The pages are stored inside the file in a cyclic way. Newer pages are placed after older ones,
 * until the maximum file size is reached. After that, the next page is stored in the beginning of
 * the file.
 * 
 * @author Matej Knopp
 */
public class PageWindowManager
{
	/**
	 * Contains information about a page inside the file.
	 * 
	 * @author Matej Knopp
	 */
	private static class PageWindowInternal
	{
		// id of page or -1 if the window is empty
		private int pageId;

		private short versionNumber;
		private short ajaxVersionNumber;

		// offset in the file where the serialized page data begins
		private int filePartOffset;

		// size of serialized page data
		private int filePartSize;
	}

	// list of PageWindowInternal objects
	private final List /* <PageWindowInternal> */windows = new ArrayList();

	// map from page id to list of pagewindow indices (refering to the windows
	// list) - to improve searching speed
	// the index must be cleaned when the instances in the windows list
	// change their indexes (e.g. items are shifted on page window removal)
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

	private void rebuildIndices()
	{
		idToWindowIndices = new IntHashMap();
		for (int i = 0; i < windows.size(); ++i)
		{
			PageWindowInternal window = (PageWindowInternal)windows.get(i);
			putWindowIndex(window.pageId, i);
		}
	}

	private int getWindowIndex(List /* Integer */indices, int pageId, int versionNumber,
			int ajaxVersionNumber)
	{
		int result = -1;

		if (versionNumber != -1 && ajaxVersionNumber != -1)
		{
			// just find the exact page version
			for (Iterator i = indices.iterator(); i.hasNext();)
			{
				int currentIndex = ((Integer)i.next()).intValue();
				PageWindowInternal window = (PageWindowInternal)windows.get(currentIndex);

				if (window.pageId == pageId && window.versionNumber == versionNumber &&
						window.ajaxVersionNumber == ajaxVersionNumber)
				{
					result = currentIndex;
					break;
				}
			}
		}
		else if (versionNumber == -1)
		{
			// we need to find last recently stored page window - that is page
			// window with index closest to the left of the indexPointer or
			// farthest
			// to the right.
			for (Iterator i = indices.iterator(); i.hasNext();)
			{
				int currentIndex = ((Integer)i.next()).intValue();
				PageWindowInternal window = (PageWindowInternal)windows.get(currentIndex);

				if (window.pageId == pageId)
				{
					if ((result == -1) || /**/
					(currentIndex <= indexPointer && result > indexPointer) || /**/
					(currentIndex > result && currentIndex <= indexPointer) || /**/
					(currentIndex > result && result > indexPointer))
					{
						result = currentIndex;
					}
				}
			}
		}
		else if (ajaxVersionNumber == -1)
		{
			int lastAjaxVersion = -1;
			// we need to find index with highest ajax version
			for (Iterator i = indices.iterator(); i.hasNext();)
			{
				int currentIndex = ((Integer)i.next()).intValue();
				PageWindowInternal window = (PageWindowInternal)windows.get(currentIndex);

				if (window.pageId == pageId && window.versionNumber == versionNumber &&
						window.ajaxVersionNumber > lastAjaxVersion)
				{
					result = currentIndex;
					lastAjaxVersion = window.ajaxVersionNumber;
				}
			}
		}

		return result;
	}

	/**
	 * Returns the index of the given page in the {@link #windows} list.
	 * 
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 * @return
	 */
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
			index = getWindowIndex(indices, pageId, versionNumber, ajaxVersionNumber);
		}
		return index;
	}

	// index of last added page
	private int indexPointer = -1;

	/**
	 * Increments the {@link #indexPointer}. If the maximum file size has ben reeched, the
	 * {@link #indexPointer} is set to 0.
	 * 
	 * @return
	 */
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

	/**
	 * Returns the offset in file of the window on given index. The offset is counted by getting the
	 * previous page offset and adding the previous page size to it.
	 * 
	 * @param index
	 * @return
	 */
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

	/**
	 * Splits the window with given index to two windows. First of those will have size specified by
	 * the argument, the other one will fill up the rest of the original window.
	 * 
	 * @param index
	 * @param size
	 */
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

	/**
	 * Merges the window with given index with the next window. The resulting window will have size
	 * of the two windows summed together.
	 * 
	 * @param index
	 */
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

	/**
	 * Adjusts the window on given index to the specified size. If the new size is smaller than the
	 * window size, the window will be splitted. Otherwise the window will be merged with as many
	 * subsequent window as necessary. In case the window is last window in the file, the size will
	 * be adjusted without splitting or merging.
	 * 
	 * @param index
	 * @param size
	 */
	private void adjustWindowSize(int index, int size)
	{
		PageWindowInternal window = (PageWindowInternal)windows.get(index);

		// last window, just adjust size
		if (index == windows.size() - 1)
		{
			int delta = size - window.filePartSize;
			totalSize += delta;
			window.filePartSize = size;
		}
		else
		{
			// merge as many times as necessary
			while (window.filePartSize < size && index < windows.size() - 1)
			{
				mergeWindowWithNext(index);
			}
			// done merging - do we have enough room ?
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

	/**
	 * Allocates window on given index with to size. If the index is pointing to existing window,
	 * the window size will be adjusted. Otherwise a new window with appropriated size will be
	 * created.
	 * 
	 * @param index
	 * @param size
	 * @return
	 */
	private PageWindowInternal allocatePageWindow(int index, int size)
	{
		final PageWindowInternal window;

		// new indow
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

			// adjust if necessary
			if (window.filePartSize != size)
			{
				adjustWindowSize(index, size);
			}
		}


		return window;
	}

	/**
	 * Public (read only) version of page window.
	 * 
	 * @author Matej Knopp
	 */
	public static class PageWindow
	{
		private final PageWindowInternal pageWindowInternal;

		/**
		 * Construct.
		 * 
		 * @param pageWindowInternal
		 */
		private PageWindow(PageWindowInternal pageWindowInternal)
		{
			this.pageWindowInternal = pageWindowInternal;
		}

		/**
		 * @return page Id
		 */
		public int getPageId()
		{
			return pageWindowInternal.pageId;
		}

		/**
		 * @return page version number
		 */
		public int getVersionNumber()
		{
			return pageWindowInternal.versionNumber;
		}

		/**
		 * @return page ajax version number
		 */
		public int getAjaxVersionNumber()
		{
			return pageWindowInternal.ajaxVersionNumber;
		}

		/**
		 * @return offset in the pagemap file where the serialized page data starts
		 */
		public int getFilePartOffset()
		{
			return pageWindowInternal.filePartOffset;
		}

		/**
		 * @return size of the serialized page data
		 */
		public int getFilePartSize()
		{
			return pageWindowInternal.filePartSize;
		}
	}

	/**
	 * Creates and returns a new page window for given page.
	 * 
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 * @param size
	 * @return
	 */
	public PageWindow createPageWindow(int pageId, int versionNumber, int ajaxVersionNumber,
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
		// indexPointor position or because we didn't find it), increment the
		// indexPointer
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
	 * Returns the page window for given page or null if no window was found.
	 * 
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 * @return page window or null
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
	 * Removes the page window for given page.
	 * 
	 * @param pageId
	 * @param versionNumber
	 * @param ajaxVersionNumber
	 */
	public void removePage(int pageId, int versionNumber, int ajaxVersionNumber)
	{
		int index = getWindowIndex(pageId, versionNumber, ajaxVersionNumber);
		if (index != -1)
		{
			PageWindowInternal window = (PageWindowInternal)windows.get(index);
			removeWindowIndex(pageId, index);
			if (index == windows.size() - 1)
			{
				windows.remove(index);
				totalSize -= window.filePartSize;
				if (indexPointer == index)
				{
					--indexPointer;
				}
			}
			else
			{
				window.pageId = -1;
			}
		}
	}

	/**
	 * Removes all page windows for given page. Removes all page versions.
	 * 
	 * @param pageId
	 */
	public void removePage(int pageId)
	{
		if (idToWindowIndices == null)
		{
			rebuildIndices();
		}
		List indicesList = (List)idToWindowIndices.get(pageId);

		if (indicesList != null)
		{
			// we need to make a copy, as the removePage removes index from indicesList
			Object[] indices = indicesList.toArray();
			for (int i = 0; i < indices.length; i++)
			{
				int index = ((Integer)indices[i]).intValue();
				PageWindowInternal window = (PageWindowInternal)windows.get(index);
				if (window.pageId == pageId)
				{
					removePage(window.pageId, window.versionNumber, window.ajaxVersionNumber);
				}
			}
		}
	}

	/**
	 * Returns last n saved page windows.
	 * 
	 * @param count
	 * @return
	 */
	public synchronized List /* <PageWindow> */getLastPageWindows(int count)
	{
		List /* <PageWindow */result = new ArrayList();

		int currentIndex = indexPointer;

		do
		{
			if (currentIndex == -1)
			{
				break;
			}

			PageWindowInternal window = (PageWindowInternal)windows.get(currentIndex);
			if (window.pageId != -1)
			{
				result.add(new PageWindow(window));
			}

			--currentIndex;

			if (currentIndex == -1)
			{
				currentIndex = result.size() - 1;
			}

		}
		while (result.size() < count && currentIndex != indexPointer);

		return result;
	}

	/**
	 * Creates a new PageWindowManager.
	 * 
	 * @param maxSize
	 *            maximum page size. After this size is exceeded, the pages will be saved starting
	 *            at the beginning of file
	 */
	public PageWindowManager(int maxSize)
	{
		this.maxSize = maxSize;
	}

	/**
	 * Returns the size of all saved pages
	 * 
	 * @return
	 */
	public int getTotalSize()
	{
		return totalSize;
	}

	private int totalSize = 0;

	private final int maxSize;
}
