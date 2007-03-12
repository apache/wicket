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
package wicket;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.model.IModel;

/**
 * Test page for detach logic.
 * 
 * @author dashorst
 */
public class TestDetachPage extends WebPage
{
	/** For serialization. */
	private static final long serialVersionUID = 1L;

	private int nrPageInternalDetachCalls = 0;
	private int nrPageOnDetachCalls = 0;
	private int nrPageDetachModelCalls = 0;
	private int nrPageDetachModelsCalls = 0;

	private int nrComponentInternalDetachCalls = 0;
	private int nrComponentOnDetachCalls = 0;
	private int nrComponentDetachModelCalls = 0;
	private int nrComponentDetachModelsCalls = 0;

	private int nrAjaxBehaviorDetachModelCalls = 0;

	private int nrModelDetachCalls = 0;

	private AjaxEventBehavior ajaxEventBehavior;

	/**
	 * Model for testing detach logic.
	 * 
	 * @author dashorst
	 */
	private class DetachModel implements IModel
	{
		/** for serialization. */
		private static final long serialVersionUID = 1L;

		public IModel getNestedModel()
		{
			return null;
		}

		public Object getObject(Component component)
		{
			return "body";
		}

		public void setObject(Component component, Object object)
		{
		}

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

			public void internalDetach()
			{
				nrComponentInternalDetachCalls++;
				super.internalDetach();
			}

			protected void onDetach()
			{
				nrComponentOnDetachCalls++;
				super.onDetach();
			}

			protected void detachModel()
			{
				nrComponentDetachModelCalls++;
				super.detachModel();
			}

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

			public void detachModel(Component component)
			{
				nrAjaxBehaviorDetachModelCalls++;
				super.detachModel(component);
			}

			protected void onEvent(AjaxRequestTarget target)
			{
				target.addComponent(label);
			}
		};
		label.add(ajaxEventBehavior);
		add(label);
	}

	public void internalDetach()
	{
		nrPageInternalDetachCalls++;
		super.internalDetach();
	}

	protected void onDetach()
	{
		nrPageOnDetachCalls++;
		super.onDetach();
	}

	protected void detachModel()
	{
		nrPageDetachModelCalls++;
		super.detachModel();
	}

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
	 * @return nrComponentInternalDetachCalls
	 */
	public int getNrComponentInternalDetachCalls()
	{
		return nrComponentInternalDetachCalls;
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
	 * @return nrPageInternalDetachCalls
	 */
	public int getNrPageInternalDetachCalls()
	{
		return nrPageInternalDetachCalls;
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
