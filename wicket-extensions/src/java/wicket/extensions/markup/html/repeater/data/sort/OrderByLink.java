/*
 * $Id$ $Revision:
 * 1.3 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.sort;

import java.io.Serializable;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.data.sort.ISortableDataProvider.SortState;
import wicket.markup.html.link.Link;
import wicket.model.AbstractModel;
import wicket.model.IModel;

/**
 * A link that changes the ordering on a field of a PageableDataView.
 * 
 * @author Phil Kulak
 * @author Igor Vaynberg
 */
public class OrderByLink extends Link
{
	private static final long serialVersionUID = 1L;

	private String sortProperty;

	private DataView list;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the id of the link
	 * @param sortProperty
	 *            the field of the list
	 * @param list
	 *            the DataView that contains a ISortableDataProvider
	 */
	public OrderByLink(String id, String sortProperty, DataView list)
	{
		this(id, sortProperty, list, DefaultCssProvider.getInstance());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the id of the link
	 * @param sortProperty
	 *            the field of the list
	 * @param list
	 *            the DataView that contains a ISortableDataProvider
	 * @param cssProvider
	 *            CSS provider used to generate value of class attribute for
	 *            link
	 * @see OrderByLink.ICssProvider
	 * 
	 */
	public OrderByLink(String id, String sortProperty, DataView list, ICssProvider cssProvider)
	{
		super(id);

		if (!(list.getModelObject() instanceof ISortableDataProvider))
		{
			throw new IllegalStateException("An OrderByLink must point to a "
					+ "DataView that uses an ISortableDataProvider as it's model.");
		}

		this.sortProperty = sortProperty;
		this.list = list;

		add(new CssModifier(this, cssProvider));
	}

	/**
	 * @see wicket.markup.html.link.Link
	 */
	public void onClick()
	{
		sort();
	}

	/**
	 * Resort data provider according to this link
	 * 
	 * @return this
	 */
	public OrderByLink sort()
	{
		ISortableDataProvider dataProvider = (ISortableDataProvider)list.getModelObject();

		// Add the ordering to the list.
		list.modelChanging();
		dataProvider.addSort(sortProperty);
		list.setCurrentPage(0);
		list.modelChanged();

		return this;
	}

	/**
	 * Uses the specified ICssProvider to add css class attributes to the link.
	 * 
	 * @author Igor Vaynberg ( ivaynberg )
	 * 
	 */
	public static class CssModifier extends AttributeModifier
	{
		private static final long serialVersionUID = 1L;


		/**
		 * @param link
		 *            the link this modifier is being added to
		 * @param provider
		 *            implementation of ICssProvider
		 */
		public CssModifier(final OrderByLink link, final ICssProvider provider)
		{
			super("class", true, new AbstractModel()
			{
				private static final long serialVersionUID = 1L;

				public IModel getNestedModel()
				{
					return null;
				}

				public Object getObject(Component component)
				{
					final DataView list = link.list;
					final ISortableDataProvider dataProvider = (ISortableDataProvider)list
							.getModelObject();
					final SortState state = dataProvider.getSortState(link.sortProperty);

					return provider.getClassAttributeValue(state);
				}

				public void setObject(Component component, Object object)
				{
					throw new UnsupportedOperationException();
				}

			});
		}


		/**
		 * @see wicket.AttributeModifier#isEnabled()
		 */
		public boolean isEnabled()
		{
			return getReplaceModel().getObject(null) != null;
		}


	};


	/**
	 * Interface used to generate values of css class attribute for the anchor
	 * tag If the generated value is null class attribute will not be added
	 * 
	 * @author igor
	 */
	public static interface ICssProvider extends Serializable
	{
		/**
		 * @param state
		 *            the state to represent as a string
		 * @return the value of the "class" attribute for the given sort state
		 */
		public String getClassAttributeValue(SortState state);
	}


	/**
	 * Easily constructible implementation of ICSSProvider
	 * 
	 * @author igor
	 * 
	 */
	public static class CssProvider implements ICssProvider
	{
		private static final long serialVersionUID = 1L;

		private String ascending;

		private String descending;

		private String none;

		/**
		 * @param ascending
		 *            css class when sorting is ascending
		 * @param descending
		 *            css class when sorting is descending
		 * @param none
		 *            css class when not sorted
		 */
		public CssProvider(String ascending, String descending, String none)
		{
			this.ascending = ascending;
			this.descending = descending;
			this.none = none;
		}

		/**
		 * @see ICssProvider#getClassAttributeValue(SortState)
		 */
		public String getClassAttributeValue(SortState state)
		{
			if (state.getState() == SortState.ASCENDING)
			{
				return ascending + getLevelAppend(state.getLevel());
			}
			if (state.getState() == SortState.DESCENDING)
			{
				return descending + getLevelAppend(state.getLevel());
			}
			return none;
		}

		private String getLevelAppend(int level)
		{
			if (level == 0)
			{
				return "";
			}
			return "_" + Integer.toString(level);
		}
	}

	/**
	 * Convineince implementation of ICssProvider that always returns a null and
	 * so never adds a class attribute
	 * 
	 * @author Igor Vaynberg ( ivaynberg )
	 */
	public static class VoidCssProvider extends CssProvider
	{
		private static final long serialVersionUID = 1L;
		
		private static ICssProvider instance = new VoidCssProvider();

		/**
		 * @return singleton instance
		 */
		public static ICssProvider getInstance()
		{
			return instance;
		}

		private VoidCssProvider()
		{
			super(null, null, null);
		}
	}

	/**
	 * Default implementation of ICssProvider
	 * 
	 * @author Igor Vaynberg ( ivaynberg )
	 */
	public static class DefaultCssProvider extends CssProvider
	{
		private static final long serialVersionUID = 1L;
		
		private static DefaultCssProvider instance = new DefaultCssProvider();

		private DefaultCssProvider()
		{
			super("wicket_orderUp", "wicket_orderDown", "wicket_orderNone");
		}

		/**
		 * @return singleton instance
		 */
		public static DefaultCssProvider getInstance()
		{
			return instance;
		}
	}

}
