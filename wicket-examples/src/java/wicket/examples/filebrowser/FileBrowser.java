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
package wicket.examples.filebrowser;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.PageParameters;
import wicket.RequestCycle;
import wicket.examples.util.NavigationPanel;
import wicket.markup.ComponentTagAttributeModifier;
import wicket.markup.html.HtmlContainer;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IOnChangeListener;
import wicket.markup.html.tree.ListTree;
import wicket.markup.html.tree.Tree;
import wicket.markup.html.tree.TreeNodeModel;
import wicket.model.IModel;

/**
 * Tree example that uses the user-home dirs to populate the tree.
 *
 * @author Eelco Hillenius
 */
public class FileBrowser extends HtmlPage
{
    /** Log. */
    private static Log log = LogFactory.getLog(FileBrowser.class);

	/** the flat tree. */
	private static final String TYPE_INDENT = "indent tree";

	/** the default ListTree. */
	private static final String TYPE_LIST = "list tree";

	/** our override of the list with a custom rows panel. */
	private static final String TYPE_LIST_CUSTOM_ROWS = "list tree with custom rows";

	/** the types of lists that are available for selection. */
	private static final List types;
	static {
		types = new ArrayList(3);
		types.add(TYPE_INDENT);
		types.add(TYPE_LIST_CUSTOM_ROWS);
		types.add(TYPE_LIST);
	}

	/** property that holds the current selection of tree types. */
	private String currentType;

	/** the current tree component (holds the tree state we're interested in). */
	private Tree currentTree = null;

    /**
     * Constructor.
     * @param parameters Page parameters
     */
    public FileBrowser(final PageParameters parameters)
    {
        TreeModel model = new FileModelProvider().getFileModel();
        setTreeComponent(new CustomListTree("fileTree", model, true));
        this.currentType = TYPE_LIST_CUSTOM_ROWS;
    }

	/**
	 * Adds the tree component after removing all other components and adding the
	 * default components.
	 * @param tree the tree to set as the currently active
	 */
	private void setTreeComponent(Tree tree)
	{
		removeAll();
		addDefaultComponents();
		currentTree = tree;
		HtmlContainer treeContainer = new HtmlContainer("tree");
		IModel idReplacementModel = new IModel() {

			public Object getObject()
			{
				if(TYPE_INDENT.equals(currentType))
				{
					return "flattree";
				}
				else if(TYPE_LIST.equals(currentType))
				{
					return "nestedtree";
				}
				else
				{
					return "customtree";
				}
			}

			public void setObject(Object object)
			{
			}
		};
		
		treeContainer.add(tree);
		treeContainer.add(new ComponentTagAttributeModifier(
				"class", true, idReplacementModel));
		add(treeContainer);
	}

	/**
	 * Adds some default components.
	 */
	private void addDefaultComponents()
	{
		add(new NavigationPanel("mainNavigation", "Filebrowser example"));
		add(new TypesDropDown("treeTypes", types));
	}

	/**
	 * Gets the property that holds the current selection of tree types.
	 * @return the current selection of tree types
	 */
	public String getCurrentType()
	{
		return currentType;
	}
	/**
	 * Sets the property that holds the current selection of tree types.
	 * @param currentType the current selection of tree types
	 */
	public void setCurrentType(String currentType)
	{
		this.currentType = currentType;
	}

    /**
     * Dropdown for selecting the kind of tree to use.
     */
    private final class TypesDropDown extends DropDownChoice implements IOnChangeListener
    {
        /**
         * Construct.
         * @param name Name of component
         * @param types The types to select from
         */
        public TypesDropDown(String name, List types)
        {
            super(name, FileBrowser.this, "currentType", types);
        }

        /**
         * @see wicket.markup.html.form.DropDownChoice#selectionChanged(wicket.RequestCycle, java.lang.Object)
         */
        public void selectionChanged(RequestCycle cycle, Object newSelection)
        {
            String type = (String)newSelection;
            setCurrentTree(type);
        }

		/**
		 * Sets the current tree.
		 * @param type type of the tree
		 */
		private void setCurrentTree(String type)
		{
			final Tree tree;
			if(TYPE_LIST.equals(type)) // create NL with simple linkClicked override
            {
                tree = new ListTree("fileTree", currentTree.getTreeState()){

                	protected void linkClicked(RequestCycle cycle, TreeNodeModel node)
                	{
                        super.linkClicked(cycle, node);
                        FileBrowser.log.info("tree link was clicked, user object: "
                        		+ node.getUserObject());
                	}
                };
            }
            else if(TYPE_INDENT.equals(type)) // create indent tree
            {
            	tree = new CustomIndentTree("fileTree", currentTree.getTreeState());
            }
            else if(TYPE_LIST_CUSTOM_ROWS.equals(type)) // create NL tree with custom panels
            {
            	tree = new CustomListTree("fileTree", currentTree.getTreeState());
            }
            else
            {
            	throw new RuntimeException("invalid type selection");
            }
			FileBrowser.this.setTreeComponent(tree);
		}
    }
}
