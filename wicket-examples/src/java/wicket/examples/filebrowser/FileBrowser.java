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
import wicket.markup.html.panel.Panel;
import wicket.markup.html.tree.AbstractTree;
import wicket.markup.html.tree.FlatTree;
import wicket.markup.html.tree.Tree;
import wicket.markup.html.tree.TreeNodeModel;
import wicket.markup.html.tree.TreeRowReplacementModel;
import wicket.markup.html.tree.TreeStateCache;

/**
 * Tree example that uses the user-home dirs to populate the tree.
 *
 * @author Eelco Hillenius
 */
public class FileBrowser extends HtmlPage
{
	/** our override of the nested tree. */
	private static final String TYPE_NESTED_CUSTOM = "custom nested tree";

	/** the normal, nested tree. */
	private static final String TYPE_NESTED = "plain nested tree";

	/** the flat tree. */
	private static final String TYPE_FLAT = "flat tree";

	/** the types of lists that are available for selection. */
	private static final List types;
	static {
		types = new ArrayList(3);
		types.add(TYPE_NESTED_CUSTOM);
		types.add(TYPE_NESTED);
		types.add(TYPE_FLAT);
	}

    /** Log. */
    private static Log log = LogFactory.getLog(FileBrowser.class);

	/** property that holds the current selection of tree types. */
	private String currentType;

	/** the current tree component (holds the tree state we're interested in). */
	private AbstractTree currentTree = null;

    /**
     * Constructor.
     * @param parameters Page parameters
     */
    public FileBrowser(final PageParameters parameters)
    {
        addDefaultComponents();
        TreeModel model = new FileModelProvider().getFileModel();
        HtmlContainer treeContainer = new HtmlContainer("tree");
        currentTree = new FileTree("fileTree", model, true);
        add(currentTree);
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
         * @param name name of component
         * @param the types to select from
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
        	FileBrowser.this.removeAll();
        	addDefaultComponents();
            String type = (String)newSelection;
            final AbstractTree tree;
            if(TYPE_NESTED.equals(type))
            {
                tree = new Tree("fileTree", currentTree.getTreeState());
            }
            else if(TYPE_FLAT.equals(type))
            {
            	tree = new FlatFileTree("fileTree", currentTree.getTreeState());
            }
            else // TYPE_NESTED_CUSTOM
            {
            	tree = new FileTree("fileTree", currentTree.getTreeState());
            }
            FileBrowser.this.add(tree);
            FileBrowser.this.currentTree = tree;
        }
    }

    /** Custom tree that provides our own row panel. */
    private static class FileTree extends Tree
    {
        /**
         * Constructor.
         * @param componentName The name of this container
         * @param model the underlying tree model
         * @param makeTreeModelUnique whether to make the user objects of the tree model
         * unique. If true, the default implementation will wrapp all user objects in
         * instances of {@link IdWrappedUserObject}. If false, users must ensure that the
         * user objects are unique within the tree in order to have the tree working properly
         */
        public FileTree(final String componentName, final TreeModel model,
        		final boolean makeTreeModelUnique)
        {
            super(componentName, model, makeTreeModelUnique);
        }

        /**
         * Constructor using the given tree state. This tree state holds the tree model and
         * the currently visible paths.
         * @param componentName The name of this container
         * @param treeState the tree state that holds the tree model and the currently visible
         * paths
         */
        public FileTree(final String componentName, TreeStateCache treeState)
        {
            super(componentName, treeState);
        }

		/**
         * Override to provide a custom row panel.
         * @see wicket.markup.html.tree.Tree#getTreeRowPanel(java.lang.String, wicket.markup.html.tree.TreeNodeModel)
         */
        protected Panel getTreeRowPanel(String componentName, TreeNodeModel nodeModel)
        {
            TreeRowReplacementModel replacementModel =
                new TreeRowReplacementModel(nodeModel);
            Panel rowPanel = new FileTreeRow(componentName, this, nodeModel);
            rowPanel.add(new ComponentTagAttributeModifier(
                    "class", true, replacementModel));
            return rowPanel;
        } 
    }

    /** flat tree implementation. */
    private static class FlatFileTree extends FlatTree
    {

        /**
         * Constructor using the given tree state. This tree state holds the tree model and
         * the currently visible paths.
         * @param componentName The name of this container
         * @param treeState the tree state that holds the tree model and the currently visible
         * paths
         */
        public FlatFileTree(final String componentName, TreeStateCache treeState)
        {
            super(componentName, treeState);
        }

		/**
         * Get image name for junction.
         * @param node the model with the current node
         * @return image name
         */
        protected String getJunctionImageName(TreeNodeModel node)
        {
            final String img;

            if(!node.isLeaf())
            {
	            if (node.isExpanded())
	            {
	                img = "filebrowser/minus.gif";
	            }
	            else
	            {
	                img = "filebrowser/plus.gif";
	            }
            }
            else
            {
            	img = "filebrowser/blank.gif";
            }

            return img;
        }

        /**
         * Get image name for node.
         * @param node the model with the current node
         * @return image name
         */
        protected String getNodeImageName(TreeNodeModel node)
        {
            final String img;

            if (node.isLeaf())
            {
                img = "filebrowser/node.gif";
            }
            else
            {
                if (node.isExpanded())
                {
                    img = "filebrowser/folderopen.gif";
                }
                else
                {
                    img = "filebrowser/folder.gif";
                }
            }

            return img;
        }
    }
}
