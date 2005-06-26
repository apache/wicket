/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.extensions.markup.html.navmenu;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import wicket.PageParameters;
import wicket.RequestCycle;

/**
 * Represents an entry in a page navigation menu.
 *
 * @author Eelco Hillenius
 */
public final class MenuItem extends DefaultMutableTreeNode
{
	/** label of the menu item. */
	private String label;

	/** class of the page. */
	private Class pageClass;

	/** optional page parameters. */
	private PageParameters pageParameters;

	/**
	 * Construct.
	 */
	public MenuItem()
	{
		super();
	}

	/**
	 * Construct.
	 * @param label label of the menu item
	 * @param pageClass class of the page
	 * @param pageParameters optional page parameters
	 */
	public MenuItem(String label, Class pageClass, PageParameters pageParameters)
	{
		super();
		this.label = label;
		this.pageClass = pageClass;
		this.pageParameters = pageParameters;
	}

	/**
	 * Checks whether the given (current) request may use this item.
	 * @param requestCycle the (current) request cycle
	 * @return true if this item should be visible
	 */
	public boolean checkAccess(RequestCycle requestCycle)
	{
		return true;
	}

	/**
	 * Gets the label of the menu item.
	 * @return the label of the menu item
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Sets the label of the menu item.
	 * @param label the label of the menu item
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * Gets the class of the page.
	 * @return the class of the page
	 */
	public Class getPageClass()
	{
		return pageClass;
	}

	/**
	 * Sets the class of the page.
	 * @param pageClass the class of the page
	 */
	public void setPageClass(Class pageClass)
	{
		this.pageClass = pageClass;
	}

	/**
	 * Gets the optional page parameters.
	 * @return the page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * Sets the page parameters.
	 * @param pageParameters the page parameters
	 */
	public void setPageParameters(PageParameters pageParameters)
	{
		this.pageParameters = pageParameters;
	}

	
	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#add(javax.swing.tree.MutableTreeNode)
	 */
	public void add(MutableTreeNode newChild)
	{
		check(newChild);
		super.add(newChild);
	}

	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
	 */
	public void insert(MutableTreeNode newChild, int childIndex)
	{
		check(newChild);
		super.insert(newChild, childIndex);
	}

	/**
	 * @see javax.swing.tree.DefaultMutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
	 */
	public void setParent(MutableTreeNode newParent)
	{
		check(newParent);
		super.setParent(newParent);
	}

	/**
	 * Checks whether the given node is not null and of the correct type.
	 * @param treeNode node to check
	 */
	private void check(TreeNode treeNode)
	{
		if (treeNode == null)
		{
			throw new NullPointerException("treeNode may not be null");
		}
		if (!(treeNode instanceof MenuItem))
		{
			throw new IllegalArgumentException("argument must be of type " +
					MenuItem.class.getName() + " (but is of type " +
					treeNode.getClass().getName() + ")");
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "MenuItem{pageClass=" + getPageClass() + "}";
	}
}
