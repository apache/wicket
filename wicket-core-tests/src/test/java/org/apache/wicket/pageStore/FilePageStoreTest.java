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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.wicket.util.WicketTestTag;
import org.apache.wicket.util.lang.Bytes;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FilePageStore}.
 */
@Tag(WicketTestTag.SLOW)
public class FilePageStoreTest extends AbstractConcurrentPageStoreTest
{
	
	private static final Bytes MAX_SIZE_PER_SESSION = Bytes.megabytes(10);
	
	/**
	 * @throws IOException 
	 */
	@Test
	void store() throws IOException
	{
		File folder = Files.createTempDirectory(null).toFile();

		IPageStore pageStore = new FilePageStore("app1", folder, MAX_SIZE_PER_SESSION);

		doTestStore(pageStore);

		pageStore.destroy();
	}
}
