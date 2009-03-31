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
package org.apache.wicket.markup.html.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.IClusterable;
import org.apache.wicket.model.IDetachable;

/**
 * Default implementation of TreeState.
 * <p>
 * This implementation tries to be as lightweight as possible. By default all nodes are collapsed.
 * 
 * @author Matej Knopp
 */
public class DefaultTreeState implements ITreeState, IClusterable, IDetachable
{
	private static final long serialVersionUID = 1L;

	/** Whether multiple selections can be done. */
	private boolean allowSelectMultiple = false;

	/** Tree state listeners. */
	private final List<ITreeStateListener> listeners = new ArrayList<ITreeStateListener>(1);

	/**
	 * set of nodes which are collapsed or expanded (depends on nodesCollapsed variable).
	 */
	private final Set<Object> nodes = new HashSet<Object>();

	/** Whether the nodes set should be treated as set of collapsed or expanded nodes. */
	private boolean nodesCollapsed = false; // by default treat the node set as expanded nodes

	/** Set selected nodes. */
	private final Set<Object> selectedNodes = new HashSet<Object>();

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#addTreeStateListener(org.apache.wicket.markup.html.tree.ITreeStateListener)
	 */
	public void addTreeStateListener(ITreeStateListener l)
	{
		if (listeners.contains(l) == false)
		{
			listeners.add(l);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#collapseAll()
	 */
	public void collapseAll()
	{
		if (nodes.isEmpty() && nodesCollapsed == false)
		{
			// all nodes are already collapsed, do nothing
		}
		else
		{
			// clear all nodes from the set and sets the nodes as expanded
			nodes.clear();
			nodesCollapsed = false;

			Object[] listenersCopy = listeners.toArray();
			for (int i = 0; i < listenersCopy.length; i++)
			{
				ITreeStateListener l = (ITreeStateListener)listenersCopy[i];
				l.allNodesCollapsed();
			}
		}
	}

	public void collapseNode(Object node)
	{
		if (nodesCollapsed == true)
		{
			nodes.add(node);
		}
		else
		{
			nodes.remove(node);
		}

		Object[] listenersCopy = listeners.toArray();
		for (int i = 0; i < listenersCopy.length; i++)
		{
			ITreeStateListener l = (ITreeStateListener)listenersCopy[i];
			l.nodeCollapsed(node);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#expandAll()
	 */
	public void expandAll()
	{
		if (nodes.isEmpty() && nodesCollapsed == true)
		{
			// all nodes are already expanded, do nothing
		}
		else
		{
			// clear node set and set nodes policy as collapsed
			nodes.clear();
			nodesCollapsed = true;

			Object[] listenersCopy = listeners.toArray();
			for (int i = 0; i < listenersCopy.length; i++)
			{
				ITreeStateListener l = (ITreeStateListener)listenersCopy[i];
				l.allNodesExpanded();
			}
		}
	}

	public void expandNode(Object node)
	{
		if (nodesCollapsed == false)
		{
			nodes.add(node);
		}
		else
		{
			nodes.remove(node);
		}

		Object[] listenersCopy = listeners.toArray();
		for (int i = 0; i < listenersCopy.length; i++)
		{
			ITreeStateListener l = (ITreeStateListener)listenersCopy[i];
			l.nodeExpanded(node);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#getSelectedNodes()
	 */
	public Collection<Object> getSelectedNodes()
	{
		return Collections.unmodifiableList(new ArrayList<Object>(selectedNodes));
	}

	protected void removeSelectedNodeSilent(Object node)
	{
		selectedNodes.remove(node);
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#isAllowSelectMultiple()
	 */
	public boolean isAllowSelectMultiple()
	{
		return allowSelectMultiple;
	}

	public boolean isNodeExpanded(Object node)
	{
		if (nodesCollapsed == false)
		{
			return nodes.contains(node);
		}
		else
		{
			return nodes.contains(node) == false;
		}
	}

	public boolean isNodeSelected(Object node)
	{
		return selectedNodes.contains(node);
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#removeTreeStateListener(org.apache.wicket.markup.html.tree.ITreeStateListener)
	 */
	public void removeTreeStateListener(ITreeStateListener l)
	{
		listeners.remove(l);
	}

	public void selectNode(Object node, boolean selected)
	{
		if (isAllowSelectMultiple() == false && selectedNodes.size() > 0)
		{
			for (Iterator<Object> i = selectedNodes.iterator(); i.hasNext();)
			{
				Object current = i.next();
				if (current.equals(node) == false)
				{
					i.remove();
					Object[] listenersCopy = listeners.toArray();
					for (int j = 0; j < listenersCopy.length; j++)
					{
						ITreeStateListener l = (ITreeStateListener)listenersCopy[j];
						l.nodeUnselected(current);
					}
				}
			}
		}

		if (selected == true && selectedNodes.contains(node) == false)
		{

			selectedNodes.add(node);
			Object[] listenersCopy = listeners.toArray();
			for (int i = 0; i < listenersCopy.length; i++)
			{
				ITreeStateListener l = (ITreeStateListener)listenersCopy[i];
				l.nodeSelected(node);
			}
		}
		else if (selected == false && selectedNodes.contains(node) == true)
		{
			selectedNodes.remove(node);
			Object[] listenersCopy = listeners.toArray();
			for (int i = 0; i < listenersCopy.length; i++)
			{
				ITreeStateListener l = (ITreeStateListener)listenersCopy[i];
				l.nodeUnselected(node);
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.tree.ITreeState#setAllowSelectMultiple(boolean)
	 */
	public void setAllowSelectMultiple(boolean value)
	{
		allowSelectMultiple = value;
	}

	public void detach()
	{
		for (Object node : nodes)
		{
			if (node instanceof IDetachable)
			{
				((IDetachable)node).detach();
			}
		}
		for (Object node : selectedNodes)
		{
			if (node instanceof IDetachable)
			{
				((IDetachable)node).detach();
			}
		}
	}
}
