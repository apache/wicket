package org.apache.wicket.pageStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomUtils;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.DeflatedJavaSerializer;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Bytes;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;


/**
 * AsynchronousPageStoreTest
 * 
 * @author manuelbarzi
 *
 */
public class AsynchronousPageStoreTest
{

	/** Log for reporting. */
	private static final Logger log = LoggerFactory.getLogger(AsynchronousPageStoreTest.class);

	@SuppressWarnings("serial")
	private static class DummyPage implements IManageablePage
	{

		private int pageId;
		private long writeMillis;
		private long readMillis;
		private String sessionId;

		private DummyPage(int pageId, long writeMillis, long readMillis, String sessionId)
		{
			this.pageId = pageId;
			this.writeMillis = writeMillis;
			this.readMillis = readMillis;
			this.sessionId = sessionId;
		}

		@Override
		public boolean isPageStateless()
		{
			return false;
		}

		@Override
		public int getPageId()
		{
			return pageId;
		}

		@Override
		public void detach()
		{
		}

		@Override
		public boolean setFreezePageId(boolean freeze)
		{
			return false;
		}

		/**
		 * @param s
		 * @throws IOException
		 */
		private void writeObject(java.io.ObjectOutputStream s) throws IOException
		{
			log.debug("serializing page {} for {}ms (session {})", getPageId(), writeMillis,
				sessionId);
			try
			{
				Thread.sleep(writeMillis);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}

			s.writeInt(pageId);
			s.writeLong(writeMillis);
			s.writeLong(readMillis);
			s.writeObject(sessionId);
		}

		private void readObject(java.io.ObjectInputStream s)
			throws IOException, ClassNotFoundException
		{
			log.debug("deserializing page {} for {}ms (session {})", getPageId(), writeMillis,
				sessionId);
			try
			{
				Thread.sleep(readMillis);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}

			pageId = s.readInt();
			writeMillis = s.readLong();
			readMillis = s.readLong();
			sessionId = (String)s.readObject();
		}

		public String toString()
		{
			return "DummyPage[pageId = " + pageId + ", writeMillis = " + writeMillis +
				", readMillis = " + readMillis + ", sessionId = " + sessionId + ", hashCode = " +
				hashCode() + "]";
		}
	}

	/**
	 * Store returns the same page instance from queue when there is a close request for it back
	 * again.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void storeReturnsSameInstanceOnClosePageRequest() throws InterruptedException
	{

		ISerializer serializer = new DeflatedJavaSerializer("applicationKey");
		// ISerializer serializer = new DummySerializer();

		IDataStore dataStore = new DiskDataStore("applicationName", new File("./target"),
			Bytes.bytes(10000l));

		// IPageStore pageStore = new DummyPageStore(new File("target/store"));
		IPageStore pageStore = spy(new DefaultPageStore(serializer, dataStore, 0));

		IPageStore asyncPageStore = new AsynchronousPageStore(pageStore, 100);

		int pageId = 0;
		String sessionId = "sessionId";

		DummyPage page = new DummyPage(pageId, 1000, 1000, sessionId);
		asyncPageStore.storePage(sessionId, page);

		Thread.sleep(500);

		IManageablePage pageBack = asyncPageStore.getPage(sessionId, pageId);

		verify(pageStore, never()).getPage(sessionId, pageId);

		assertEquals(page, pageBack);
	}

	/**
	 * Store returns the restored page instance from wrapped store when there is a distant request
	 * for it back again.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void storeReturnsRestoredInstanceOnDistantPageRequest() throws InterruptedException
	{

		ISerializer serializer = new DeflatedJavaSerializer("applicationKey");
		// ISerializer serializer = new DummySerializer();

		IDataStore dataStore = new DiskDataStore("applicationName", new File("./target"),
			Bytes.bytes(10000l));

		// IPageStore pageStore = new DummyPageStore(new File("target/store"));
		IPageStore pageStore = spy(new DefaultPageStore(serializer, dataStore, 0));

		IPageStore asyncPageStore = new AsynchronousPageStore(pageStore, 100);

		int pageId = 0;
		String sessionId = "sessionId";

		DummyPage page = new DummyPage(pageId, 1000, 1000, sessionId);
		asyncPageStore.storePage(sessionId, page);

		Thread.sleep(1500);

		IManageablePage pageBack = asyncPageStore.getPage(sessionId, pageId);

		verify(pageStore, times(1)).getPage(sessionId, pageId);

		assertNotEquals(page, pageBack);
	}

	/**
	 * Store works fully asynchronous when number of pages handled never exceeds the
	 * asynchronous-storage capacity.
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void storeBehavesAsyncWhenNotExceedingStoreCapacity() throws InterruptedException
	{
		int sessions = 2;
		int pages = 5;
		long writeMillis = 2000;
		long readMillis = 1500;
		int asyncPageStoreCapacity = pages * sessions;

		List<Metrics> results = runTest(sessions, pages, writeMillis, readMillis,
			asyncPageStoreCapacity);

		for (Metrics metrics : results)
			System.out.println(metrics);

		for (Metrics metrics : results)
		{
			assertEquals(metrics.storedPage, metrics.restoredPage);
			assertTrue(metrics.storingMillis < writeMillis);
			assertTrue(metrics.restoringMillis < readMillis);
		}
	}

	/**
	 * Store behaves sync from when number of pages handled exceeds the given asynchronous-storage
	 * capacity. It works asynchronous until the number of pages reaches the limit (capacity).
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void storeBehavesSyncFromWhenExceedingStoreCapacity() throws InterruptedException
	{
		int sessions = 2;
		int pages = 5;
		long writeMillis = 2000;
		long readMillis = 1500;
		int asyncPageStoreCapacity = pages; // only up to the first round of
											// pages

		List<Metrics> results = runTest(sessions, pages, writeMillis, readMillis,
			asyncPageStoreCapacity);

		for (Metrics metrics : results)
			System.out.println(metrics);

		int sync = 0;

		for (int i = 0; i < results.size(); i++)
		{
			Metrics metrics = results.get(i);

			assertEquals(metrics.storedPage.sessionId, metrics.restoredPage.sessionId);
			assertEquals(metrics.storedPage.getPageId(), metrics.restoredPage.getPageId());

			if (!metrics.storedPage.equals(metrics.restoredPage))
			{
				assertTrue(metrics.storingMillis >= metrics.storedPage.writeMillis);
				sync++;
			}
		}

		assertTrue(sync > 0);
	}

	// test run

	private class Metrics
	{
		private DummyPage storedPage;
		private long storingMillis;
		private DummyPage restoredPage;
		private long restoringMillis;

		public String toString()
		{
			return "Metrics[storedPage = " + storedPage + ", storingMillis = " + storingMillis +
				", restoredPage = " + restoredPage + ", restoringMillis = " + restoringMillis + "]";
		}
	}

	private List<Metrics> runTest(int sessions, int pages, long writeMillis, long readMillis,
		int asyncPageStoreCapacity) throws InterruptedException
	{

		List<Metrics> results = new ArrayList<>();

		final CountDownLatch lock = new CountDownLatch(pages * sessions);

		// ISerializer serializer = new DummySerializer();
		ISerializer serializer = new DeflatedJavaSerializer("applicationKey");

		IDataStore dataStore = new DiskDataStore("applicationName", new File("./target"),
			Bytes.bytes(10000l));

		// IPageStore pageStore = new DummyPageStore(new File("target/store")) {
		IPageStore pageStore = new DefaultPageStore(serializer, dataStore, 0)
		{

			@Override
			public void storePage(String sessionId, IManageablePage page)
			{

				super.storePage(sessionId, page);

				lock.countDown();
			}
		};

		IPageStore asyncPageStore = new AsynchronousPageStore(pageStore, asyncPageStoreCapacity);

		Stopwatch stopwatch = Stopwatch.createUnstarted();

		for (int pageId = 1; pageId <= pages; pageId++)
		{
			for (int i = 1; i <= sessions; i++)
			{
				String sessionId = String.valueOf(i);
				Metrics metrics = new Metrics();

				stopwatch.reset();
				DummyPage page = new DummyPage(pageId, around(writeMillis), around(readMillis),
					sessionId);
				stopwatch.start();
				asyncPageStore.storePage(sessionId, page);
				metrics.storedPage = page;
				metrics.storingMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

				stopwatch.reset();
				stopwatch.start();
				metrics.restoredPage = DummyPage.class
					.cast(asyncPageStore.getPage(sessionId, pageId));
				metrics.restoringMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

				results.add(metrics);
			}
		}

		lock.await(pages * sessions * (writeMillis + readMillis), TimeUnit.MILLISECONDS);

		return results;
	}

	private long around(long target)
	{
		return RandomUtils.nextLong((long)(target * .9), (long)(target * 1.1));
	}

	// other aux dummy impls for testing

	private class DummySerializer implements ISerializer
	{

		@Override
		public byte[] serialize(Object obj)
		{
			ByteArrayOutputStream bos = null;
			ObjectOutput out = null;
			try
			{
				bos = new ByteArrayOutputStream();
				out = new ObjectOutputStream(bos);
				out.writeObject(obj);
				return bos.toByteArray();
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				try
				{
					if (bos != null)
						bos.close();
					if (out != null)
						out.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public Object deserialize(byte[] bytes)
		{
			ByteArrayInputStream bis = null;
			ObjectInput in = null;
			try
			{
				bis = new ByteArrayInputStream(bytes);
				in = new ObjectInputStream(bis);
				return in.readObject();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				try
				{
					if (bis != null)
						bis.close();
					if (in != null)
						in.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

	}

	private class DummyPageStore implements IPageStore
	{

		private File folder;

		private DummyPageStore(File folder)
		{
			folder.mkdirs();
			this.folder = folder;
		}

		private File getPageFile(String sessionId, int pageId)
		{
			return new File(folder.getAbsolutePath() + "/" + sessionId + "-" + pageId + ".page");
		}

		private void toFile(Object obj, File file)
		{
			FileOutputStream fos = null;
			ObjectOutput oo = null;
			try
			{
				fos = new FileOutputStream(file);
				oo = new ObjectOutputStream(fos);
				oo.writeObject(obj);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				try
				{
					if (fos != null)
						fos.close();
					if (oo != null)
						oo.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		private Object fromFile(File file)
		{
			FileInputStream fis = null;
			ObjectInput oi = null;
			try
			{
				fis = new FileInputStream(file);
				oi = new ObjectInputStream(fis);
				return oi.readObject();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			catch (ClassNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				try
				{
					if (fis != null)
						fis.close();
					if (oi != null)
						oi.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		public void destroy()
		{
		}

		@Override
		public IManageablePage getPage(String sessionId, int pageId)
		{
			return (IManageablePage)fromFile(getPageFile(sessionId, pageId));
		}

		@Override
		public void removePage(String sessionId, int pageId)
		{
		}

		@Override
		public void storePage(String sessionId, IManageablePage page)
		{
			toFile(page, getPageFile(sessionId, page.getPageId()));
		}

		@Override
		public void unbind(String sessionId)
		{
		}

		@Override
		public Serializable prepareForSerialization(String sessionId, Serializable page)
		{
			return null;
		}

		@Override
		public Object restoreAfterSerialization(Serializable serializable)
		{
			return null;
		}

		@Override
		public IManageablePage convertToPage(Object page)
		{
			return null;
		}

		@Override
		public boolean canBeAsynchronous()
		{
			return false;
		}
	}

}