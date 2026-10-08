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
package org.apache.wicket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;

import org.apache.wicket.mock.MockApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Test cases for the <code>Application-Metadata</code> class.
 *
 * @author Hans Hosea Schaefer
 */
public class ApplicationMetadataTest {

	private static final MetaDataKey<Serializable> MY_METADATA_KEY = new MetaDataKey<>() {};

	@AfterEach
	void detachThreadContext() {
		ThreadContext.detach();
	}

	@Test
	void metadataAdded() {
		final MockApplication application = new MockApplication();
		assertNull(application.getMetaData(MY_METADATA_KEY));
		application.setMetaData(MY_METADATA_KEY, "I love wicket");
		assertEquals("I love wicket", application.getMetaData(MY_METADATA_KEY));
	}

	@Test
	void metadataReplaced() {
		final MockApplication application = new MockApplication();
		application.setMetaData(MY_METADATA_KEY, "I love wicket");
		application.setMetaData(MY_METADATA_KEY, "I love Idea");
		assertEquals("I love Idea", application.getMetaData(MY_METADATA_KEY));
	}

	@Test
	void metadataRemoved() {
		final MockApplication application = new MockApplication();
		application.setMetaData(MY_METADATA_KEY, "I love wicket");
		application.setMetaData(MY_METADATA_KEY, null);
		assertNull(application.getMetaData(MY_METADATA_KEY));
	}
}
