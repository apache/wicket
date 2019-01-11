package org.apache.wicket.page;

import org.apache.wicket.pageStore.AsynchronousPageStore;
import org.apache.wicket.pageStore.DefaultPageStore;
import org.apache.wicket.pageStore.DiskDataStore;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.memory.DummyPageManagerContext;
import org.apache.wicket.serialize.ISerializer;
import org.apache.wicket.serialize.java.DeflatedJavaSerializer;
import org.apache.wicket.util.WicketTestTag;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.lang.Bytes;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.FieldSetter;

/**
 * https://issues.apache.org/jira/browse/WICKET-6629
 */
@Tag(WicketTestTag.SLOW)
class AsyncPageStoreManagerTest
{
	private static final String APP_NAME = "test_app";

	/*
	 * Symptoms:
	 * 1) Out of memory on DiskDataStore.sessionEntryMap, map contains thousands of
	 * DiskDataStore$SessionEntry's; The vm only contains a few hundred PageStoreManager$SessionEntry's
	 * (as expected for the currently active sessions)
	 * 2) Slowly growing disk usage, the Wicket filestore contains thousands of directories, up to the point
	 * where ls becomes unusable.
	 * 
	 * Problem (as far as I can tell):
	 * The PageSavingRunnable of the AsynchronousPageStore saves entries to the DiskDataStore after the
	 * corresponding session has been invalidated and its DiskDataStore has been cleaned. Because the session is
	 * destroyed according to the container and Wicket session handling there is nothing triggering a cleanup for
	 * this session anymore.
	 */
	@Test
	void invalidateSessionBeforeSave() throws InterruptedException
	{
		ISerializer serializer = new DeflatedJavaSerializer("applicationKey");
		IDataStore dataStore = new DiskDataStore("applicationName", new File("target"),
			Bytes.bytes(10000l));
		IPageStore pageStore = new DefaultPageStore(serializer, dataStore, 0);
		IPageStore asyncPageStore = new AsynchronousPageStore(pageStore, 100);
		IPageManagerContext pageManagerContext = new DummyPageManagerContext();
		IPageManager newPageManager = new PageStoreManager(APP_NAME, asyncPageStore,
			pageManagerContext);

		// Commit a page to the pagemanager
		TestPage toSerializePage = new TestPage(0);
		newPageManager.touchPage(toSerializePage);
		newPageManager.commitRequest();

		// Allow some time for the PageSavingRunnable to save the page to disk
		Thread.sleep(1000);

		// Page should be stored on disk
		Assert.assertNotNull(dataStore.getData("dummy_id", 0));

		// "Stop" the PageSavingRunnable, so we can simulate a pending page
		Thread t = null;
		try
		{
			t = (Thread)new FieldReader(asyncPageStore,
				AsynchronousPageStore.class.getDeclaredField("pageSavingThread")).read();
			Runnable r = (Runnable)new FieldReader(t, Thread.class.getDeclaredField("target"))
				.read();
			FieldSetter.setField(asyncPageStore,
				AsynchronousPageStore.class.getDeclaredField("pageSavingThread"), null);

			// Allow some time for the original PageSavingRunnable to exit
			Thread.sleep(1000);

			t = new Thread(r, "Wicket-AsyncPageStore-PageSavingThread");
			t.setDaemon(true);
			FieldSetter.setField(asyncPageStore,
				AsynchronousPageStore.class.getDeclaredField("pageSavingThread"), t);
		}
		catch (NoSuchFieldException | SecurityException e)
		{
			throw new RuntimeException(e);
		}

		// Commit a second page to this session
		newPageManager.touchPage(new TestPage(1));
		newPageManager.commitRequest();

		Thread.sleep(1000);

		// PageSavingRunnable is not running so page should not be in the datastore
		Assert.assertNull(dataStore.getData("dummy_id", 1));

		// Session is invalidated and a clear on the PageStoreManager is called
		newPageManager.clear();

		// Datastore is indeed empty after the clear
		Assert.assertNull(dataStore.getData("dummy_id", 0));
		Assert.assertNull(dataStore.getData("dummy_id", 1));

		// "Restart" the PageSavingRunnable
		t.start();

		// Allow some time for the PageSavingRunnable to save any pages left in the queue to disk
		Thread.sleep(1000);

		// Session has been invalidated. The datastore should not contain any pages for this
		// session, because they will never be cleaned! Not in the DiskDataStore.sessionEntryMap
		// (OOM) and not on disk (slowly filling disk as this even survives application restarts.
		Assert.assertNull(dataStore.getData("dummy_id", 0));
		Assert.assertNull(dataStore.getData("dummy_id", 1));
	}

	private static class TestPage implements IManageablePage
	{
		private static final long serialVersionUID = 1L;

		private final int instanceID;

		private TestPage(int id)
		{
			instanceID = id;
		}

		@Override
		public boolean isPageStateless()
		{
			return false;
		}

		@Override
		public int getPageId()
		{
			return instanceID;
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
	}
}
