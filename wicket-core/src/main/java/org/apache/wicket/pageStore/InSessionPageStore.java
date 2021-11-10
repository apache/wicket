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
package org.apache.wicket.pageStore;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpSession;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Classes;

/**
 * A store keeping a configurable maximum of pages in the session.
 * <p>
 * Note: see {@link #getKey()} for using more than once instance in an application
 */
public class InSessionPageStore implements IPageStore
{

	private static final MetaDataKey<SessionData> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final ISerializer serializer;

	private final Supplier<SessionData> dataCreator;

	/**
	 * Keep {@code maxPages} persistent in each session.
	 * <p>
	 * Any page added to this store <em>not</em> being a {@code SerializedPage} will be dropped
	 * on serialization of the session.
	 * 
	 * @param maxPages
	 *            maximum pages to keep in session
	 */
	public InSessionPageStore(int maxPages)
	{
		this(null, () -> new CountLimitedData(maxPages));
	}

	/**
	 * Keep page up to {@code maxBytes} persistent in each session.
	 * <p>
	 * All pages added to this store <em>must</em> be {@code SerializedPage}s. You can achieve this
	 * by letting a {@link SerializingPageStore} delegate to this store.
	 * 
	 * @param maxBytes
	 *            maximum bytes to keep in session
	 */
	public InSessionPageStore(Bytes maxBytes)
	{
		this(null, () -> new SizeLimitedData(maxBytes));
	}

	/**
	 * Keep a cache of {@code maxPages} in each session.
	 * <p>
	 * If the container serializes sessions to disk, any non-{@code SerializedPage} added to this
	 * store will be automatically serialized.
	 * 
	 * @param maxPages
	 *            maximum pages to keep in session
	 * @param serializer
	 *            optional serializer used only in case session serialization
	 */
	public InSessionPageStore(int maxPages, ISerializer serializer)
	{
		this(serializer, () -> new CountLimitedData(maxPages));
	}

	private InSessionPageStore(ISerializer serializer, Supplier<SessionData> dataCreator)
	{
		this.serializer = serializer;

		this.dataCreator = dataCreator;
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		SessionData data = getSessionData(context, false);
		if (data != null)
		{
			IManageablePage page = data.get(id);
			if (page != null)
			{
				return page;
			}
		}

		return null;
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		SessionData data = getSessionData(context, true);

		data.add(page);
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		SessionData data = getSessionData(context, false);
		if (data != null)
		{
			data.remove(page.getPageId());
		}
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		SessionData data = getSessionData(context, false);
		if (data != null)
		{
			data.removeAll();
		}
	}

	private SessionData getSessionData(IPageContext context, boolean create)
	{
		SessionData data = context.getSessionData(getKey(), () -> {
			if (create)
			{
				return dataCreator.get();
			}
			else
			{
				return null;
			}
		});

		if (data != null && serializer != null)
		{
			// data might be deserialized so initialize again
			data.supportSessionSerialization(serializer);
		}

		return data;
	}

	/**
	 * Session data is stored under a {@link MetaDataKey}.
	 * <p>
	 * In the unlikely case that an application utilizes more than one instance of this store,
	 * this method has to be overridden to provide a separate key for each instance.
	 */
	protected MetaDataKey<SessionData> getKey()
	{
		return KEY;
	}

	/**
	 * Data kept in the {@link Session}, might get serialized along with its containing
	 * {@link HttpSession}.
	 */
	protected abstract static class SessionData implements Serializable
	{

		transient ISerializer serializer;

		/**
		 * Pages, may partly be serialized.
		 * <p>
		 * Kept in list instead of map, since non-serialized pages might change their id during a
		 * request.
		 */
		List<IManageablePage> pages = new LinkedList<>();

		/**
		 * Call this method if session serialization should be supported, i.e. all pages get
		 * serialized along with the session.
		 */
		public void supportSessionSerialization(ISerializer serializer)
		{
			this.serializer = Args.notNull(serializer, "serializer");
		}

		public synchronized void add(IManageablePage page)
		{
			// move to end
			remove(page.getPageId());

			pages.add(page);
		}

		protected synchronized void removeOldest()
		{
			IManageablePage page = pages.get(0);

			remove(page.getPageId());
		}

		public synchronized IManageablePage remove(int pageId)
		{
			Iterator<IManageablePage> iterator = pages.iterator();
			while (iterator.hasNext())
			{
				IManageablePage page = iterator.next();

				if (page.getPageId() == pageId)
				{
					iterator.remove();
					return page;
				}
			}
			return null;
		}

		public synchronized void removeAll()
		{
			pages.clear();
		}

		public synchronized IManageablePage get(int id)
		{
			for (int p = 0; p < pages.size(); p++)
			{
				IManageablePage candidate = pages.get(p);

				if (candidate.getPageId() == id)
				{
					if (candidate instanceof SerializedPage && serializer != null)
					{
						candidate = (IManageablePage)serializer
							.deserialize(((SerializedPage)candidate).getData());

						pages.set(p, candidate);
					}

					return candidate;
				}
			}

			return null;
		}

		/**
		 * Serialize pages before writing to output.
		 */
		private void writeObject(final ObjectOutputStream output) throws IOException
		{
			// handle non-serialized pages
			for (int p = 0; p < pages.size(); p++)
			{
				IManageablePage page = pages.get(p);

				if ((page instanceof SerializedPage) == false)
				{
					// remove if not already serialized
					pages.remove(p);
					
					if (serializer == null)
					{
						// cannot be serialized, thus skip
						p--;
					}
					else
					{
						// serialize first
						byte[] bytes = serializer.serialize(page);
						SerializedPage serializedPage = new SerializedPage(page.getPageId(), Classes.name(page.getClass()), bytes);

						// and then re-add (to prevent a serialization loop,
						// in case the page holds a reference to the session)  
						pages.add(p, serializedPage);
					}
				}
			}

			output.defaultWriteObject();
		}
	}

	/**
	 * Limit pages by count.
	 */
	static class CountLimitedData extends SessionData
	{
		private final int maxPages;

		public CountLimitedData(int maxPages)
		{
			this.maxPages = Args.withinRange(1, Integer.MAX_VALUE, maxPages, "maxPages");
		}

		@Override
		public synchronized void add(IManageablePage page)
		{
			super.add(page);

			while (pages.size() > maxPages)
			{
				removeOldest();
			}
		}
	}

	/**
	 * Limit pages by size.
	 */
	static class SizeLimitedData extends SessionData
	{
		private final Bytes maxBytes;

		private long size;

		public SizeLimitedData(Bytes maxBytes)
		{
			Args.notNull(maxBytes, "maxBytes");

			this.maxBytes = Args.withinRange(Bytes.bytes(1), Bytes.MAX, maxBytes, "maxBytes");
		}

		@Override
		public synchronized void add(IManageablePage page)
		{
			if (page instanceof SerializedPage == false)
			{
				throw new WicketRuntimeException(
					"InSessionPageStore limited by size works with serialized pages only");
			}

			super.add(page);

			size += ((SerializedPage)page).getData().length;

			while (size > maxBytes.bytes())
			{
				removeOldest();
			}
		}

		@Override
		public synchronized IManageablePage remove(int pageId)
		{
			SerializedPage page = (SerializedPage)super.remove(pageId);
			if (page != null)
			{
				size -= page.getData().length;
			}

			return page;
		}

		@Override
		public synchronized void removeAll()
		{
			super.removeAll();

			size = 0;
		}
	}

	@Override
	public boolean supportsVersioning()
	{
		return false;
	}
}
