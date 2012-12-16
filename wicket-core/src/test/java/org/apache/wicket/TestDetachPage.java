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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IWriteableModel;

/**
 * Test page for detach logic.
 * 
 * @author dashorst
 */
public class TestDetachPage extends WebPage
{
	/** For serialization. */
	private static final long serialVersionUID = 1L;

	private int nrPageOnDetachCalls = 0;
	private int nrPageDetachModelCalls = 0;
	private int nrPageDetachModelsCalls = 0;

	private int nrComponentOnDetachCalls = 0;
	private int nrComponentDetachModelCalls = 0;
	private int nrComponentDetachModelsCalls = 0;

	private int nrAjaxBehaviorDetachModelCalls = 0;

	private int nrModelDetachCalls = 0;

	private final AjaxEventBehavior ajaxEventBehavior;

	/**
	 * Model for testing detach logic.
	 * 
	 * @author dashorst
	 */
	private class DetachModel implements IWriteableModel<String>
	{
		/** for serialization. */
		private static final long serialVersionUID = 1L;

		@Override
		public String getObject()
		{
			return "body";
		}

		@Override
		public void setObject(String object)
		{
		}

		@Override
		public void detach()
		{
			nrModelDetachCalls++;
		}
	}

	/**
	 * Construct.
	 */
	public TestDetachPage()
	{
		final Label label = new Label("comp", new DetachModel())
		{
			/** For serialization */
			private static final long serialVersionUID = 1L;

			@Override
			protected void onDetach()
			{
				nrComponentOnDetachCalls++;
				super.onDetach();
			}

			@Override
			protected void detachModel()
			{
				nrComponentDetachModelCalls++;
				super.detachModel();
			}

			@Override
			public void detachModels()
			{
				nrComponentDetachModelsCalls++;
				super.detachModels();
			}
		};
		label.setOutputMarkupId(true);
		ajaxEventBehavior = new AjaxEventBehavior("onclick")
		{
			/** for serialization. */
			private static final long serialVersionUID = 1L;

			@Override
			public void detach(Component component)
			{
				nrAjaxBehaviorDetachModelCalls++;
				super.detach(component);
			}

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				target.add(label);
			}
		};
		label.add(ajaxEventBehavior);
		add(label);
	}

	@Override
	protected void onDetach()
	{
		nrPageOnDetachCalls++;
		super.onDetach();
	}

	@Override
	protected void detachModel()
	{
		nrPageDetachModelCalls++;
		super.detachModel();
	}

	@Override
	public void detachModels()
	{
		nrPageDetachModelsCalls++;
		super.detachModels();
	}

	/**
	 * @return nrComponentDetachModelCalls
	 */
	public int getNrComponentDetachModelCalls()
	{
		return nrComponentDetachModelCalls;
	}

	/**
	 * @return nrComponentDetachModelsCalls
	 */
	public int getNrComponentDetachModelsCalls()
	{
		return nrComponentDetachModelsCalls;
	}

	/**
	 * @return nrComponentOnDetachCalls
	 */
	public int getNrComponentOnDetachCalls()
	{
		return nrComponentOnDetachCalls;
	}

	/**
	 * @return nrPageDetachModelCalls
	 */
	public int getNrPageDetachModelCalls()
	{
		return nrPageDetachModelCalls;
	}

	/**
	 * @return nrPageDetachModelsCalls
	 */
	public int getNrPageDetachModelsCalls()
	{
		return nrPageDetachModelsCalls;
	}

	/**
	 * @return nrPageOnDetachCalls
	 */
	public int getNrPageOnDetachCalls()
	{
		return nrPageOnDetachCalls;
	}

	/**
	 * @return nrModelDetachCalls
	 */
	public int getNrModelDetachCalls()
	{
		return nrModelDetachCalls;
	}

	/**
	 * @return nrAjaxBehaviorDetachModelCalls
	 */
	public int getNrAjaxBehaviorDetachModelCalls()
	{
		return nrAjaxBehaviorDetachModelCalls;
	}

	/**
	 * @return ajaxEventBehavior
	 */
	public AjaxEventBehavior getAjaxBehavior()
	{
		return ajaxEventBehavior;
	}
}
