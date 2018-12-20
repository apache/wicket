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

import javax.servlet.http.HttpSession;

import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Classes;

/**
 * A store keeping a configurable maximum of pages in the session.
 * <p>
 * This store is used by {@link DefaultPageManagerProvider} as a cache in front 
 * of a persistent store.
 */
public class InSessionPageStore extends DelegatingPageStore
{

	private static final MetaDataKey<SessionData> KEY = new MetaDataKey<SessionData>()
	{
		private static final long serialVersionUID = 1L;
	};

	private final ISerializer serializer;

	private int maxPages;
	
	/**
	 * Use this constructor, if sessions are never persisted by the container.
	 * 
	 * @param delegate
	 *            store to delegate to
	 * @param maxPages
	 *            maximum pages to keep in session
	 */
	public InSessionPageStore(IPageStore delegate, int maxPages)
	{
		this(delegate, maxPages,  new ISerializer()
		{
			@Override
			public byte[] serialize(Object object)
			{
				throw new WicketRuntimeException("InSessionPageStore not configured for serialization");
			}
			
			@Override
			public Object deserialize(byte[] data)
			{
				throw new WicketRuntimeException("InSessionPageStore not configured for serialization");
			}
		});
	}
	
	/**
	 * @param delegate
	 *            store to delegate to
	 * @param maxPages
	 *            maximum pages to keep in session
	 * @param serializer
	 *            for serialization of pages if session gets persisted
	 */
	public InSessionPageStore(IPageStore delegate, int maxPages, ISerializer serializer)
	{
		super(delegate);

		this.serializer = Args.notNull(serializer, "serializer");

		this.maxPages = maxPages;
	}

	@Override
	public IManageablePage getPage(IPageContext context, int id)
	{
		SessionData data = getSessionData(context, false);
		if (data != null) {
			IManageablePage page = data.get(id);
			if (page != null)
			{
				return page;
			}
		}

		return getDelegate().getPage(context, id);
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		SessionData data = getSessionData(context, true);

		data.add(context, page, maxPages);
		
		getDelegate().addPage(context, page);
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		SessionData data = getSessionData(context, false);
		if (data != null) {
			data.remove(page);
		}

		getDelegate().removePage(context, page);
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		SessionData data = getSessionData(context, false);
		if (data != null) {
			data.removeAll();
		}

		getDelegate().removeAllPages(context);
	}

	private SessionData getSessionData(IPageContext context, boolean create)
	{
		SessionData data = context.getSessionData(KEY);
		if (data == null && create)
		{
			data = context.setSessionData(KEY, new SessionData());
		}

		if (data != null) {
			// data might be deserialized so initialize again
			data.init(serializer);
		}

		return data;
	}

	/**
	 * Data kept in the {@link Session}, might get serialized along with its containing
	 * {@link HttpSession}.
	 */
	static class SessionData implements Serializable
	{

		transient ISerializer serializer;

		/**
		 * Pages, may partly be serialized.
		 * <p>
		 * Kept in list instead of map, since life pages might change their id during a request.
		 */
		private List<IManageablePage> pages = new LinkedList<>();

		/**
		 * This method <em>must</em> be called each time it is retrieved from the session: <br/>
		 * After deserializing from persisted session the serializer is no longer referenced and all
		 * contained pages are in a serialized state.
		 */
		public void init(ISerializer serializer)
		{
			this.serializer = Args.notNull(serializer, "serializer");
		}

		public synchronized void add(IPageContext context, IManageablePage page, int maxPages)
		{
			// move to end
			remove(page);
			pages.add(page);

			while (pages.size() > maxPages)
			{
				pages.remove(0);
			}
		}

		public synchronized void remove(IManageablePage page)
		{
			Iterator<IManageablePage> iterator = pages.iterator();
			while (iterator.hasNext()) {
				if (iterator.next().getPageId() == page.getPageId()) {
					iterator.remove();
					break;
				}
			}
		}

		public synchronized void removeAll()
		{
			pages.clear();
		}

		public synchronized IManageablePage get(int id)
		{
			IManageablePage page = null;
			
			for (int p = 0; p < pages.size(); p++)
			{
				IManageablePage candidate = pages.get(p);

				if (candidate.getPageId() == id) {
					if (candidate instanceof SerializedPage)
					{
						if (serializer == null)
						{
							throw new IllegalStateException("SessionData#init() was not called");
						}
						candidate = (IManageablePage)serializer.deserialize(((SerializedPage)candidate).getData());
		
						pages.set(p, candidate);
					}
					
					page = candidate;
					break;
				}
			}

			return page;
		}

		/**
		 * Serialize pages before writing to output.
		 */
		private void writeObject(final ObjectOutputStream output) throws IOException
		{
			// serialize pages if not already
			for (int p = 0; p < pages.size(); p++)
			{
				IManageablePage page = pages.get(p);
				
				if ((page instanceof SerializedPage) == false)
				{
					if (serializer == null)
					{
						throw new IllegalStateException("SessionData#init() was not called");
					}
					pages.set(p,  new SerializedPage(page.getPageId(), Classes.name(page.getClass()), serializer.serialize(page)));
				}
			}

			output.defaultWriteObject();
		}
	}
}