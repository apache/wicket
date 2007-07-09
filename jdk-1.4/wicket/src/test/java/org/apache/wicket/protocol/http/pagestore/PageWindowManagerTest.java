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

import org.apache.wicket.protocol.http.pagestore.PageWindowManager.PageWindow;

import junit.framework.TestCase;

/**
 * @author Matej Knopp
 */
public class PageWindowManagerTest extends TestCase
{
	/**
	 * 
	 */
	public void testAddRemove() 
	{
		PageWindowManager manager = new PageWindowManager(300);
		PageWindow window;
		
		window = manager.savePage(1, 1, 1, 50);
		assertWindow(window, 1, 1, 1, 0, 50);
		
		window = manager.savePage(1, 1, 2, 40);
		assertWindow(window, 1, 1, 2, 50, 40);
		
		assertEquals(manager.getTotalSize(), 90);
		
		window = manager.savePage(1, 1, 2, 30);
		assertWindow(window, 1, 1, 2, 50, 30);
		assertEquals(manager.getTotalSize(), 80);
		
		manager.removePage(1, 1, 2);
		assertEquals(manager.getTotalSize(), 50);
	}
	
	/**
	 * 
	 */
	public void testPageWindowCycle() 
	{
		PageWindowManager manager = new PageWindowManager(100);
		PageWindow window;
		
		window = manager.savePage(1, 1, 1, 30);
		
		window = manager.savePage(1, 1, 2, 30);
		
		window = manager.savePage(1, 1, 3, 30);
		
		assertWindow(window, 1, 1, 3, 60, 30);
		
		window = manager.savePage(1, 1, 4, 30);
		
		assertWindow(window, 1, 1, 4, 90, 30);
		
		// should start at the beginging
		
		window = manager.savePage(1, 1, 5, 20);
		
		assertWindow(window, 1, 1, 5, 0, 20);
		
		assertNull(manager.getPageWindow(1, 1, 1));
		
		window = manager.getPageWindow(1, 1, 2);
		assertWindow(window, 1, 1, 2, 30, 30);
		
		window = manager.savePage(1, 1, 6, 10);

		assertWindow(window, 1, 1, 6, 20, 10);
	
		window = manager.getPageWindow(1, 1, 2);
		assertWindow(window, 1, 1, 2, 30, 30);

		window = manager.savePage(1, 1, 6, 30);
		assertWindow(window, 1, 1, 6, 20, 30);
		
		assertNull(manager.getPageWindow(1, 1, 2));
		assertNotNull(manager.getPageWindow(1, 1, 3));
		
		window = manager.savePage(1, 1, 6, 60);
		assertWindow(window, 1, 1, 6, 20, 60);
		
		assertNull(manager.getPageWindow(1, 1, 3));
		
		window = manager.savePage(1, 1, 7, 20);
		assertWindow(window, 1, 1, 7, 80, 20);
		
		assertNotNull(manager.getPageWindow(1, 1, 7));
		
		// should start at the beginning again
		
		window = manager.savePage(1, 1, 8, 10);
		assertWindow(window, 1, 1, 8, 0, 10);
		
		assertNull(manager.getPageWindow(1, 1, 5));
		assertNotNull(manager.getPageWindow(1, 1, 6));
		
		window = manager.savePage(1, 1, 9, 20);
		assertWindow(window, 1, 1, 9, 10, 20);
		
		assertNull(manager.getPageWindow(1, 1, 6));
		assertNotNull(manager.getPageWindow(1, 1, 7));
		
		window = manager.savePage(1, 1, 10, 20);
		assertWindow(window, 1, 1, 10, 30, 20);
		
		assertNull(manager.getPageWindow(1, 1, 6));
		assertNotNull(manager.getPageWindow(1, 1, 7));
		
		// make sure when replacing a page that's not last the old "instance" is not valid anymore
		
		manager.savePage(1, 1, 8, 10);
		
		window = manager.getPageWindow(1, 1, 8);
		assertWindow(window, 1, 1, 8, 50, 10);
		
	}
	
	private void assertWindow(PageWindow window, int pageId, int versionNumber, int ajaxVersionNumber,
			                  int filePartOffset, int filePartSize)
	{
		assertTrue(window.getPageId() == pageId && window.getVersionNumber() == versionNumber &&
				   window.getAjaxVersionNumber() == ajaxVersionNumber && window.getFilePartOffset() == filePartOffset &&
				   window.getFilePartSize() == filePartSize);
	}
}
