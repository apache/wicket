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

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.junit.Test;

/**
 * @author Pedro Santos
 */
public class DetachableTest extends WicketTestCase
{

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3872">WICKET-3872</a>
	 */
	@Test
	public void testDetachRemovedChildrenTree()
	{
		DetachableComponent a = new DetachableComponent("a");
		DetachableComponent ab = new DetachableComponent("b");
		DetachableComponent ac = new DetachableComponent("c");
		DetachableComponent abd = new DetachableComponent("d");
		DetachableModel detachableModel = new DetachableModel();
		DetachableBehavior detachableBehavior = new DetachableBehavior();
		abd.setDefaultModel(detachableModel);
		abd.add(detachableBehavior);

		a.add(ab);
		a.add(ac);
		ab.add(abd);
		a.removeAll();

		assertFalse(a.detached);
		assertTrue(ab.detached);
		assertTrue(ac.detached);
		assertTrue(abd.detached);
		assertTrue(detachableModel.detached);
		assertTrue(detachableBehavior.detached);
	}


	private class DetachableComponent extends WebMarkupContainer
	{
		private boolean detached;

		private DetachableComponent(String id)
		{
			super(id);
		}

		@Override
		protected void onDetach()
		{
			super.onDetach();
			detached = true;
		}
	}

	private class DetachableModel extends Model<Serializable>
	{
		private boolean detached;

		@Override
		public void detach()
		{
			detached = true;
		}
	}

	private class DetachableBehavior extends Behavior
	{
		private boolean detached;

		@Override
		public void detach(Component component)
		{
			detached = true;
		}
	}
}
