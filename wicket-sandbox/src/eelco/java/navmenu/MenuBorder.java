/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package navmenu;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import wicket.AttributeModifier;
import wicket.contrib.markup.html.tree.Tree;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.border.Border;
import wicket.markup.html.link.BookmarkablePageLink;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

/**
 * Border component that holds the menu.
 *
 * @author Eelco Hillenius
 */
public class MenuBorder extends Border
{
	/**
	 * Construct.
	 * @param componentName
	 */
	public MenuBorder(String componentName)
	{
		super(componentName);
		TreeModel model = MenuApplication.getMenu();
		ULTree tree = new ULTree("tree", model);
		add(tree);
	}

	/**
	 * Tree that renders as nested lists (UL/ LI).
	 */
	private final class ULTree extends Tree
	{
		/**
		 * structure with nested nodes and lists to represent the tree model
		 * using lists.
		 */
		private List nestedList;

		/**
		 * Construct.
		 * @param componentName The name of this container
		 * @param model the tree model
		 */
		public ULTree(String componentName, TreeModel model)
		{
			super(componentName, model);
			setRootVisible(false);
			buildNestedListModel(model);
			UL treeRowsListView = new UL("rows", nestedList, 0);
			add(treeRowsListView);
		}

		/**
		 * Builds the internal structure.
		 * @param model the tree model that the internal structure is to be based on
		 */
		private void buildNestedListModel(TreeModel model)
		{
			nestedList = new ArrayList(); // reference to the first level list
			if (model != null)
			{
				DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
				if(root != null)
				{
					Enumeration children = root.children();
					while(children.hasMoreElements())
					{
						DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
						add(nestedList, child);
					}
				}
			}
		}

		/**
		 * Add node to list and add any childs recursively.
		 * @param list the list to add the node to
		 * @param node the node to add
		 */
		private void add(List list, DefaultMutableTreeNode node)
		{
			list.add(node);
			Enumeration children = node.children();
			if(children.hasMoreElements()) // any elements?
			{
				List childList = new ArrayList();
				list.add(childList);
				while(children.hasMoreElements())
				{
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
					add(childList, child);
				}
			}
		}
	}

	/**
	 * Represents UL elements.
	 */
	private final class UL extends Panel
	{
		/** the level this view is on. */
		private final int level;

	    /**
	     * Constructor.
	     * @param componentName The name of this component
	     * @param list a list where each element is either a string or another list
	     * @param level the level this view is on (from 0..n-1)
	     */
	    public UL(String componentName, List list, int level)
	    {
	        super(componentName);
	        this.level = level;
	        WebMarkupContainer ul = new WebMarkupContainer("ul");
	        ul.add(new AttributeModifier("id", true, new Model(getLevelAsString())));
			Rows rows = new Rows("rows", list);
			ul.add(rows);
			add(ul);
	    }

	    /**
	     * Gets the level of this view as a string that is usuable with CSS.
	     * @return the level of this view as a string that is usuable with CSS
	     */
	    private String getLevelAsString()
	    {
	    	return "tabNavigation";
//	    	if(level == 0)
//	    	{
//	    		return "primary";
//	    	}
//	    	else
//	    	{
//	    		return "secondary";
//	    	}
	    }

	    /**
	     * The list class.
	     */
	    private final class Rows extends ListView
	    {
	        /**
	         * Construct.
	         * @param name name of the component
	         * @param list a list where each element is either a string or another list
	         */
	        public Rows(String name, List list)
	        {
	            super(name, list);
	        }

	        /**
	         * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
	         */
	        protected void populateItem(ListItem listItem)
	        {
	        	final int index = listItem.getIndex();
	            Object modelObject = listItem.getModelObject();
	            if(modelObject instanceof List)
	            {
	                // create a panel that renders the sub list
	                List list = (List)modelObject;
					UL ul = new UL("row", list, level + 1);
	                listItem.add(ul);
	            }
	            else
	            {
	            	DefaultMutableTreeNode node = (DefaultMutableTreeNode)modelObject;
					LI li = new LI("row", node, level, index);
	                listItem.add(li);
	            }
	        }
	    }
	}

	/**
	 * Represents LI elements.
	 */
	private final class LI extends Panel
	{
		/** the level this view is on. */
		private final int level;

	    /**
	     * Constructor.
	     * @param componentName The name of this component
	     * @param node tree node
	     * @param level the level this view is on (from 0..n-1)
	     * @param index the sibling index
	     */
	    public LI(final String componentName, final DefaultMutableTreeNode node,
	    		final int level, final int index)
	    {
	        super(componentName);
	        this.level = level;
	        // add the row (with the LI element attached, and the label with the
	        // row's actual value to display
	        MenuItem menuItem = (MenuItem)node.getUserObject();
	        final String label = menuItem.getLabel();
	        final BookmarkablePageLink pageLink = new BookmarkablePageLink(
	        		"link", menuItem.getPageClass(), menuItem.getPageParameters());
	        pageLink.setAutoEnable(false);
	        pageLink.add(new Label("label", label));
	        add(pageLink);
	        // TODO this works for one level, but what about nesting?
			add(new AttributeModifier("class", true, new Model()
			{
				public Object getObject()
				{
					return (pageLink.linksTo(getPage())) ? "selectedTab" : null;
				}
			}));
	    }
	}
}