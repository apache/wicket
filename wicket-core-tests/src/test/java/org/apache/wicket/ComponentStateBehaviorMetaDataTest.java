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

import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * A behavior can change the component's state from within its callbacks. Such a change replaces
 * the component's state object, so it must not be reverted by the behavior bookkeeping that is
 * running at the same time. See WICKET-6877.
 */
class ComponentStateBehaviorMetaDataTest extends WicketTestCase
{
	private static final MetaDataKey<Boolean> KEY = new MetaDataKey<>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** clears the component's only meta data entry while being detached */
	private static class ClearMetaDataOnDetach extends Behavior
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void detach(Component component)
		{
			component.setMetaData(KEY, null);
			super.detach(component);
		}
	}

	private static Label newLabelWithBehaviors(Behavior clearing)
	{
		Label label = new Label("id", "body");
		label.add(clearing, new Behavior()
		{
			private static final long serialVersionUID = 1L;
		});
		label.setMetaData(KEY, Boolean.TRUE);
		return label;
	}

	@Test
	void metaDataClearedWhileDetachingStaysCleared()
	{
		Label label = newLabelWithBehaviors(new ClearMetaDataOnDetach());

		label.detach();

		assertNull(label.getMetaData(KEY), "meta data cleared while detaching must stay cleared");
	}

	@Test
	void metaDataClearedWhileRemovingStaysCleared()
	{
		ClearMetaDataOnDetach clearing = new ClearMetaDataOnDetach();
		Label label = newLabelWithBehaviors(clearing);

		// removing a behavior detaches it, which clears the meta data
		label.remove(clearing);

		assertNull(label.getMetaData(KEY), "meta data cleared while removing must stay cleared");
	}
}
