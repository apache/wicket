package org.apache.wicket.page;

import java.io.Serializable;

/**
 * Lock manager for {@link PageAccessSynchronizer} responsible for locking and unlocking pages for
 * the duration of a request.
 */
public interface IPageLockManager
{

	/**
	 * Acquire a lock to a page
	 *
	 * @param pageId
	 *            page id
	 * @throws CouldNotLockPageException
	 *             if lock could not be acquired
	 */
	void lockPage(int pageId) throws CouldNotLockPageException;

	/**
	 * Unlocks all pages locked by this thread
	 */
	void unlockAllPages();

	/**
	 * Unlocks a single page locked by the current thread.
	 *
	 * @param pageId
	 *            the id of the page which should be unlocked.
	 */
	void unlockPage(int pageId);

}
