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
package org.apache.wicket.model;

import static org.hamcrest.core.Is.is;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

/**
 * Tests the states of a LoadableDetachableModel
 */
public class LoadableDetachableModelTest extends WicketTestCase
{
	/**
	 * Checks whether the LDM can escape recursive calls.
	 */
	@Test
	public void recursiveGetObjectDoesntCauseInfiteLoop()
	{
		class RecursiveLoad extends LoadableDetachableModel<Integer>
		{
			private static final long serialVersionUID = 1L;

			private int count = 0;

			@Override
			protected Integer load()
			{
				count++;
				getObject();
				return count;
			}
		}

		RecursiveLoad ldm = new RecursiveLoad();

		assertThat(ldm.isAttached(), is(false));
		assertThat(ldm.getObject(), is(1));
		assertThat(ldm.isAttached(), is(true));
	}

	/**
	 * Checks whether the LDM can escape recursive calls.
	 */
	@Test
	public void exceptionDuringLoadKeepsLDMDetached()
	{
		class ExceptionalLoad extends LoadableDetachableModel<Integer>
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Integer load()
			{
				throw new RuntimeException();
			}
		}

		ExceptionalLoad ldm = new ExceptionalLoad();

		assertThat(ldm.isAttached(), is(false));
		try
		{
			assertThat(ldm.getObject(), is(1));
			fail("shouldn't get here");
		}
		catch (RuntimeException e)
		{
			assertThat(ldm.isAttached(), is(false));
		}
	}
}
