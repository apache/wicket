/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.sort;

import java.io.Serializable;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.html.link.Link;
import wicket.model.AbstractModel;
import wicket.model.IModel;
import wicket.util.lang.Objects;
import wicket.version.undo.Change;

/**
 * A component that represents a sort header. When the link is clicked it will
 * toggle the state of a sortable property within the sort state object.
 * 
 * @author Phil Kulak
 * @author Igor Vaynberg (ivaynberg)
 */
public class OrderByLink extends Link
{
	private static final long serialVersionUID = 1L;

	/** sortable property */
	private String property;

	/** locator for sort state object */
	private ISortStateLocator stateLocator;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the component id of the link
	 * @param property
	 *            the name of the sortable property this link represents. this
	 *            value will be used as parameter for sort state object methods.
	 *            sort state object will be located via the stateLocator
	 *            argument.
	 * @param stateLocator
	 *            locator used to locate sort state object that this will use to
	 *            read/write state of sorted properties
	 */
	public OrderByLink(String id, String property, ISortStateLocator stateLocator)
	{
		this(id, property, stateLocator, DefaultCssProvider.getInstance());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the component id of the link
	 * @param property
	 *            the name of the sortable property this link represents. this
	 *            value will be used as parameter for sort state object methods.
	 *            sort state object will be located via the stateLocator
	 *            argument.
	 * @param stateLocator
	 *            locator used to locate sort state object that this will use to
	 *            read/write state of sorted properties
	 * @param cssProvider
	 *            CSS provider that will be used generate the value of class
	 *            attribute for this link
	 * 
	 * @see OrderByLink.ICssProvider
	 * 
	 */
	public OrderByLink(String id, String property, ISortStateLocator stateLocator,
			ICssProvider cssProvider)
	{
		super(id);

		if (cssProvider == null)
		{
			throw new IllegalArgumentException("argument [cssProvider] cannot be null");
		}

		if (property == null)
		{
			throw new IllegalArgumentException("argument [sortProperty] cannot be null");
		}

		this.property = property;
		this.stateLocator = stateLocator;
		add(new CssModifier(this, cssProvider));
	}

	/**
	 * @see wicket.markup.html.link.Link
	 */
	public final void onClick()
	{
		sort();
		onSortChanged();
	}

	/**
	 * This method is a hook for subclasses to perform an action after sort has
	 * changed
	 */
	protected void onSortChanged()
	{
		// noop
	}

	/**
	 * Re-sort data provider according to this link
	 * 
	 * @return this
	 */
	public final OrderByLink sort()
	{
		if (isVersioned())
		{
			// version the old state
			Change change = new SortStateChange();
			addStateChange(change);
		}

		ISortState state = stateLocator.getSortState();

		int oldDir = state.getPropertySortOrder(property);

		int newDir = ISortState.ASCENDING;

		if (oldDir == ISortState.ASCENDING)
		{
			newDir = ISortState.DESCENDING;
		}

		state.setPropertySortOrder(property, newDir);

		return this;
	}

	private final class SortStateChange extends Change
	{
		private static final long serialVersionUID = 1L;

		private final ISortState old = (ISortState)Objects.cloneModel(stateLocator.getSortState());

		/**
		 * @see wicket.version.undo.Change#undo()
		 */
		public void undo()
		{
			stateLocator.setSortState(old);
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return "[StateOrderChange old=" + old.toString() + "]";
		}
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

					final ISortState sortState = link.stateLocator.getSortState();
					return provider.getClassAttributeValue(sortState, link.property);
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
		 *            current sort state
		 * @param property
		 *            sort property represented by the {@link OrderByLink}
		 * @return the value of the "class" attribute for the given sort
		 *         state/sort property combination
		 */
		public String getClassAttributeValue(ISortState state, String property);
	}


	/**
	 * Easily constructible implementation of ICSSProvider
	 * 
	 * @author Igor Vaynberg (ivaynberg)
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
		 * @see wicket.extensions.markup.html.repeater.data.sort.OrderByLink.ICssProvider#getClassAttributeValue(wicket.extensions.markup.html.repeater.data.sort.ISortState,
		 *      java.lang.String)
		 */
		public String getClassAttributeValue(ISortState state, String property)
		{
			int dir = state.getPropertySortOrder(property);
			if (dir == ISortState.ASCENDING)
			{
				return ascending;
			}
			else if (dir == ISortState.DESCENDING)
			{
				return descending;
			}
			else
			{
				return none;
			}
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
			super("", "", "");
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
