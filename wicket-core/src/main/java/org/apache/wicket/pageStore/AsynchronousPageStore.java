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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for {@link IPageStore} moving {@link #addPage(IPageContext, IManageablePage)} to a worker thread.
 * <p>
 * Creates an {@link PendingAdd} for {@link #addPage(IPageContext, IManageablePage)} and puts ito a {@link #queue}.
 * Later {@link PageAddingRunnable} reads in blocking manner from {@link #queue} and performs the add.
 * <p>
 * It starts only one instance of {@link PageAddingRunnable} because all we need is to make the page
 * storing asynchronous. We don't want to write concurrently in the wrapped {@link IPageStore},
 * though it may happen in the extreme case when the queue is full. These cases should be avoided.
 * 
 * @author Matej Knopp
 * @author manuelbarzi
 */
public class AsynchronousPageStore extends DelegatingPageStore
{

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(AsynchronousPageStore.class);

	/**
	 * The time to wait when adding an {@link PendingAdd} into the entries. In millis.
	 */
	private static final long OFFER_WAIT = 30L;

	/**
	 * The time to wait for an entry to save with the wrapped {@link IPageStore} . In millis.
	 */
	private static final long POLL_WAIT = 1000L;

	/**
	 * The page saving thread.
	 */
	private final Thread pageSavingThread;

	/**
	 * The queue where the entries which have to be saved are temporary stored
	 */
	private final BlockingQueue<PendingAdd> queue;

	/**
	 * A map 'sessionId:::pageId' -> {@link PendingAdd}. Used for fast retrieval of {@link PendingAdd}s which
	 * are not yet stored by the wrapped {@link IPageStore}
	 */
	private final ConcurrentMap<String, PendingAdd> queueMap;

	/**
	 * Construct.
	 * 
	 * @param delegate
	 *            the wrapped {@link IPageStore} that actually saved the page
	 * @param capacity
	 *            the capacity of the queue that delays the saving
	 */
	public AsynchronousPageStore(final IPageStore delegate, final int capacity)
	{
		super(delegate);
		
		queue = new LinkedBlockingQueue<>(capacity);
		queueMap = new ConcurrentHashMap<>();

		PageAddingRunnable savingRunnable = new PageAddingRunnable(delegate, queue, queueMap);
		pageSavingThread = new Thread(savingRunnable, "Wicket-AsyncPageStore-PageSavingThread");
		pageSavingThread.setDaemon(true);
		pageSavingThread.start();
	}

	/**
	 * 
	 * @param sessionId
	 * @param pageId
	 * @return generated key
	 */
	private static String getKey(final String sessionId, final int pageId)
	{
		return pageId + ":::" + sessionId;
	}

	/**
	 * An add of a page that is pending its asynchronous execution.
	 * <p>
	 * Used as an isolating {@link IPageContext} for the delegation to 
	 * {@link IPageStore#addPage(IPageContext, IManageablePage)}.
	 */
	private static class PendingAdd implements IPageContext
	{
		private final IPageContext context;
		
		private final IManageablePage page;

		private final String sessionId;

		/**
		 * Is this context passed to an asynchronously called {@link IPageStore#addPage(IPageContext, IManageablePage)}.
		 */
		private boolean asynchronous = false;

		/**
		 * Cache of session attributes which may filled in {@link IPageStore#canBeAsynchronous(IPageContext)},
		 * so these are available asynchronously later on.
		 */
		private final Map<String, Serializable> attributeCache = new HashMap<>();

		public PendingAdd(final IPageContext context, final IManageablePage page)
		{
			this.context = Args.notNull(context, "context");
			this.page = Args.notNull(page, "page");
			
			this.sessionId = context.getSessionId(true);
		}

		/**
		 * @return generated key
		 */
		private String getKey()
		{
			return AsynchronousPageStore.getKey(sessionId, page.getPageId());
		}

		@Override
		public String toString()
		{
			return "PendingAdd [sessionId=" + sessionId + ", pageId=" + page.getPageId() + "]";
		}

		/**
		 * Prevents access to request when called asynchronously.
		 */
		@Override
		public <T> T getRequestData(MetaDataKey<T> key, Supplier<T> value)
		{
			if (asynchronous)
			{
				throw new WicketRuntimeException("request data not available asynchronuously");
			}
			
			return context.getRequestData(key, value);
		}

		/**
		 * Prevents changing of session attributes when called asynchronously.
		 * <p>
		 * All values accessed from {@link IPageStore#canBeAsynchronous(IPageContext)} are still
		 * available.
		 */
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Serializable> T getSessionAttribute(String key, Supplier<T> defaultValue)
		{
			T value;
			
			if (asynchronous)
			{
				value = (T)attributeCache.get(key);
				if (value == null && defaultValue.get() != null)
				{
						throw new WicketRuntimeException("session attribute can not be changed asynchronuously");
				}
			} else {
				value = context.getSessionAttribute(key, defaultValue);
				if (value != null)
				{
					attributeCache.put(key, value);
				}
			}
			
			return value;
		}
		
		/**
		 * Prevents changing of session data when called asynchronously.
		 */
		@Override
		public <T extends Serializable> T getSessionData(MetaDataKey<T> key, Supplier<T> defaultValue)
		{
			T value;
			
			if (asynchronous)
			{
				value = context.getSessionData(key, () -> null);
				if (value == null && defaultValue.get() != null)
				{
						throw new WicketRuntimeException("session data can not be changed asynchronuously");
				}
			}
			else
			{
				value = context.getSessionData(key, defaultValue);
			}
			
			return value;
		}

		/**
		 * Returns id of session.
		 */
		@Override
		public String getSessionId(boolean bind)
		{
			return sessionId;
		}
	}

	/**
	 * The consumer of {@link PendingAdd}s.
	 */
	private static class PageAddingRunnable implements Runnable
	{
		private static final Logger log = LoggerFactory.getLogger(PageAddingRunnable.class);

		private final BlockingQueue<PendingAdd> queue;

		private final ConcurrentMap<String, PendingAdd> map;

		private final IPageStore delegate;

		private PageAddingRunnable(IPageStore delegate, BlockingQueue<PendingAdd> queue,
		                           ConcurrentMap<String, PendingAdd> map)
		{
			this.delegate = delegate;
			this.queue = queue;
			this.map = map;
		}

		@Override
		public void run()
		{
			while (!Thread.interrupted())
			{
				PendingAdd add = null;
				try
				{
					add = queue.poll(POLL_WAIT, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}

				if (add != null)
				{
					log.debug("Saving asynchronously: {}...", add);
					add.asynchronous = true;					
					delegate.addPage(add, add.page);
					map.remove(add.getKey());
				}
			}
		}
	}

	@Override
	public void destroy()
	{
		if (pageSavingThread.isAlive())
		{
			pageSavingThread.interrupt();
			try
			{
				pageSavingThread.join();
			}
			catch (InterruptedException e)
			{
				log.error(e.getMessage(), e);
			}
		}

		super.destroy();
	}

	@Override
	public IManageablePage getPage(IPageContext context, int pageId)
	{
		String sessionId = context.getSessionId(false);
		if (sessionId == null) {
			return null;
		}
		
		PendingAdd entry = queueMap.get(getKey(sessionId, pageId));
		if (entry != null)
		{
			log.debug("Returning the page of a non-stored entry with page id '{}'", pageId);
			return entry.page;
		}
		IManageablePage page = getDelegate().getPage(context, pageId);

		log.debug("Returning the page of a stored entry with page id '{}'", pageId);

		return page;
	}

	@Override
	public void removePage(IPageContext context, IManageablePage page)
	{
		String sessionId = context.getSessionId(false);
		if (sessionId == null) {
			return;
		}

		String key = getKey(sessionId, page.getPageId());
		PendingAdd entry = queueMap.remove(key);
		if (entry != null)
		{
			queue.remove(entry);
		}

		getDelegate().removePage(context, page);
	}

	@Override
	public void addPage(IPageContext context, IManageablePage page)
	{
		PendingAdd add = new PendingAdd(context, page);
		if (getDelegate().canBeAsynchronous(add))
		{
			String key = add.getKey();
			queueMap.put(key, add);
			try
			{
				if (queue.offer(add, OFFER_WAIT, TimeUnit.MILLISECONDS))
				{
					log.debug("Offered for storing asynchronously page with id '{}'", page.getPageId());
					return;
				}
				else
				{
					log.debug("Storing synchronously page with id '{}'", page.getPageId());
					queueMap.remove(key);
				}
			}
			catch (InterruptedException e)
			{
				log.error(e.getMessage(), e);
				queueMap.remove(key);
			}
		}
		else
		{
			log.warn("Delegated page store '{}' can not be asynchronous", getDelegate().getClass().getName());
		}
		
		getDelegate().addPage(context, page);
	}

	@Override
	public void removeAllPages(IPageContext context)
	{
		String sessionId = context.getSessionId(false);
		if (sessionId == null) {
			return;
		}

		queue.removeIf(add -> {
			if (add.sessionId.equals(sessionId)) {
				queueMap.remove(add.getKey());
				return true;
			}
			
			return false;
		});
		
		getDelegate().removeAllPages(context);
	}
}
