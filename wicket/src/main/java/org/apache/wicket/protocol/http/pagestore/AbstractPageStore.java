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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore.IPageStore;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.collections.IntHashMap;
import org.apache.wicket.util.lang.Objects;

/**
 * Abstract page store that implements the serialization logic so that the subclasses can
 * concentrate on actual storing of serialized page instances.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractPageStore implements IPageStore
{

	/**
	 * Immutable class that contains a serialized page instance.
	 * 
	 * @author Matej Knopp
	 */
	protected static class SerializedPage implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int pageId;
		private final String pageMapName;
		private final int versionNumber;
		private final int ajaxVersionNumber;
		private byte[] data;

		/**
		 * Construct.
		 * 
		 * @param pageId
		 * @param pageMapName
		 * @param versionNumber
		 * @param ajaxVersionNumber
		 * @param data
		 */
		public SerializedPage(int pageId, String pageMapName, int versionNumber,
			int ajaxVersionNumber, byte[] data)
		{
			this.pageId = pageId;
			this.pageMapName = pageMapName;
			this.versionNumber = versionNumber;
			this.ajaxVersionNumber = ajaxVersionNumber;
			this.data = data;
		}

		/**
		 * Construct.
		 * 
		 * @param page
		 */
		public SerializedPage(Page page)
		{
			pageId = page.getNumericId();
			pageMapName = page.getPageMapName();
			versionNumber = page.getCurrentVersionNumber();
			ajaxVersionNumber = page.getAjaxVersionNumber();
		}

		/**
		 * @return the page id
		 */
		public int getPageId()
		{
			return pageId;
		}

		/**
		 * @return the pagemap name
		 */
		public String getPageMapName()
		{
			return pageMapName;
		}

		/**
		 * @return the version number
		 */
		public int getVersionNumber()
		{
			return versionNumber;
		}

		/**
		 * @return the ajax version number
		 */
		public int getAjaxVersionNumber()
		{
			return ajaxVersionNumber;
		}

		/**
		 * @return the data
		 */
		public byte[] getData()
		{
			return data;
		}

		/**
		 * @param data
		 */
		public void setData(byte[] data)
		{
			this.data = data;
		}

		@Override
		public int hashCode()
		{
			return pageId * 1931 + versionNumber * 13 + ajaxVersionNumber * 301 +
				(pageMapName != null ? pageMapName.hashCode() : 0);
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;

			if (obj instanceof SerializedPage == false)
				return false;

			SerializedPage rhs = (SerializedPage)obj;

			return pageId == rhs.pageId &&
				(pageMapName == rhs.pageMapName || (pageMapName != null && pageMapName.equals(rhs.pageMapName))) &&
				versionNumber == rhs.versionNumber && ajaxVersionNumber == rhs.ajaxVersionNumber;
		}
	};

	/**
	 * Creates a list of {@link SerializedPage} instances obtained from serializing the provided
	 * page.
	 * <p>
	 * One page instance can be serialized to multiple {@link SerializedPage} instances, because
	 * each referenced page is serialized separately and should also be separately saved On
	 * deserialization wicket detects a page instance placeholder and loads the appropriate page.
	 * <p>
	 * As an example, when there is PageA that has a member variable of type PageB, serializing
	 * instanceof PageA will result in a list of two {@link SerializedPage} instances, one for PageA
	 * and another one for the referenced PageB.
	 * 
	 * @param page
	 *            page to be serialized
	 * @return list of {@link SerializedPage}s
	 */
	@SuppressWarnings("unchecked")
	protected List<SerializedPage> serializePage(Page page)
	{
		final List<SerializedPage> result = new ArrayList<SerializedPage>();

		SerializedPage initialPage = new SerializedPage(page);
		result.add(initialPage);

		PageSerializer serializer = new PageSerializer(initialPage)
		{
			@Override
			protected void onPageSerialized(SerializedPage page)
			{
				result.add(page);
			}
		};

		Page.serializer.set(serializer);

		try
		{
			initialPage.setData(Objects.objectToByteArray(page.getPageMapEntry()));
		}
		finally
		{
			Page.serializer.set(null);
		}

		return result;
	}

	/**
	 * Creates a page instance from given byte array. Optionally gets the specified version of the
	 * page.
	 * 
	 * @param data
	 *            Serialized page instance data as byte array
	 * @param versionNumber
	 *            Requested page version or -1 if original version (the one serialized) should be
	 *            kept
	 * @return page instance
	 */
	@SuppressWarnings("unchecked")
	protected Page deserializePage(byte[] data, int versionNumber)
	{
		boolean set = Page.serializer.get() == null;
		Page page = null;
		try
		{
			if (set)
			{
				Page.serializer.set(new PageSerializer(null));
			}
			IPageMapEntry entry = (IPageMapEntry)Objects.byteArrayToObject(data);
			if (entry != null)
			{
				page = entry.getPage();
				if (versionNumber != -1)
				{
					page = page.getVersion(versionNumber);
				}
			}
		}
		finally
		{
			if (set)
			{
				Page.serializer.set(null);
			}
		}
		return page;
	}

	/**
	 * Internal class for page serialization and deserialization
	 * 
	 * @author Matej Knopp
	 * @author Johan Compagner
	 */
	private static class PageSerializer implements Page.IPageSerializer
	{
		private SerializedPage current;

		private final List<SerializedPage> completed = new ArrayList<SerializedPage>();

		public Object getPageReplacementObject(Page callingPage)
		{
			SerializedPage calling = new SerializedPage(callingPage);

			// if current page writeObject is called we need to really serialize the page instance
			if (calling.equals(current))
			{
				completed.add(calling);
				return callingPage;
			}
			else
			// serializing page referenced from current page
			{
				// if the referenced page has not yet been serialized...
				if (completed.contains(calling) == false)
				{
					// ...get the bytearray representation of it
					SerializedPage prev = current;
					current = calling;
					current.data = Objects.objectToByteArray(callingPage);

					// invoke callback with the data
					onPageSerialized(current);
					current = prev;
				}

				// return page holder instance (object that will readResolve to
				// actual page instance
				return new PageHolder(callingPage);
			}
		}

		protected void onPageSerialized(SerializedPage page)
		{

		}

		/**
		 * Construct.
		 * 
		 * @param page
		 */
		public PageSerializer(SerializedPage page)
		{
			current = page;
		}

		/**
		 * @throws IOException
		 * @see org.apache.wicket.Page.IPageSerializer#serializePage(org.apache.wicket.Page,
		 *      java.io.ObjectOutputStream)
		 */

		public void serializePage(Page page, ObjectOutputStream stream) throws IOException
		{
			stream.defaultWriteObject();
		}

		public void deserializePage(int id, String pageMapName, Page page,
			ObjectInputStream stream) throws IOException, ClassNotFoundException
		{
			// get the page instance registry
			IntHashMap<Page> pages = SecondLevelCacheSessionStore.getUsedPages(pageMapName);
			// register the new page instance so that when the same page is being deserialized
			// (curricular page references) we can use existing page instance (otherwise deadlock
			// would happen)

			pages.put(id, page);

			stream.defaultReadObject();
		}
	}


	/**
	 * Class that resolves to page instance
	 */
	private static class PageHolder implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private final int pageid;
		private final String pagemap;

		PageHolder(Page page)
		{
			pageid = page.getNumericId();
			pagemap = page.getPageMapName();
		}

		protected Object readResolve() throws ObjectStreamException
		{
			return Session.get().getPage(pagemap, Integer.toString(pageid), -1);
		}
	}

}
