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

import java.util.Iterator;

import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Represents a "dehydrated" component state keeping only the minimum information needed to
 * reconstruct the component. That includes component id, {@link IComponentSource} and componentInfo
 * string. The string contains generated <code>markupID</code>s and <code>markupIndex</code>es
 * of the component and all it's children. Those are wicket internal variables and it's up to wicket
 * (not {@link IComponentSource} implementation to reconstruct that transparently for the user.
 * These information are encoded as one string to minimize object overhead.
 */
abstract class ComponentSourceEntry implements IClusterable
{
	private static final long serialVersionUID = 1L;

	final String id;
	private final IComponentSource componentSource;
	private final String componentInfo;

	/**
	 * Checks if the component ID or markup ID doesn't contain invalid characters. This might be a
	 * little more strict that default wicket rules, but it is necessary for the componentInfo
	 * string
	 * 
	 * @param name
	 * @param id
	 */
	private final void checkId(String name, String id)
	{
		if (id.indexOf('(') != -1 || id.indexOf('(') != -1 || id.indexOf(' ') != -1 ||
			id.indexOf(',') != -1)
		{
			throw new IllegalStateException(name + "'" + id +
				"' is not valid, it may not contain any of the ' ', '(', ')', ',' characters");
		}
	}

	/**
	 * Appends component state to info string
	 * 
	 * @param buffer
	 * @param component
	 */
	private final void appendComponent(AppendingStringBuffer buffer, Component< ? > component)
	{
		checkId("Component id", component.getId());
		buffer.append(component.getId());
		buffer.append(' ');
		Object markupId = component.getMarkupIdImpl();
		if (markupId != null)
		{
			if (markupId instanceof String)
			{
				checkId("Component markup id", (String)markupId);
			}

			// if the markup id is an integer, prefixed it with '*'
			if (markupId instanceof Integer)
			{
				buffer.append('*');
			}
			buffer.append(markupId);

			buffer.append(' ');
		}
		buffer.append(component.markupIndex);

		if (component instanceof MarkupContainer &&
			((MarkupContainer< ? >)component).iterator().hasNext())
		{
			buffer.append('(');

			Iterator<Component< ? >> i = ((MarkupContainer< ? >)component).iterator();
			while (i.hasNext())
			{
				Component< ? > child = i.next();
				appendComponent(buffer, child);
				if (i.hasNext())
				{
					buffer.append(',');
				}
			}

			buffer.append(')');
		}
	}

	/**
	 * Creates a ComponentSourceEntry instance
	 * 
	 * @param container
	 * @param component
	 * @param componentSource
	 */
	ComponentSourceEntry(MarkupContainer< ? > container, Component< ? > component,
		IComponentSource componentSource)
	{
		id = component.getId();

		this.componentSource = componentSource;
		AppendingStringBuffer buffer = new AppendingStringBuffer();
		appendComponent(buffer, component);
		componentInfo = buffer.toString();

		System.out.println("Info: " + componentInfo);
	}

	/**
	 * The subclass of this method calls private method on {@link MarkupContainer}, so it needs to
	 * be implemented by a markup container inner class
	 * 
	 * @param parent
	 * @param index
	 * @param child
	 */
	protected abstract void setChild(MarkupContainer< ? > parent, int index, Component< ? > child);

	/**
	 * Reconstructs the component
	 * 
	 * @param parent
	 *            parent of the component
	 * @param index
	 *            position in parent's children
	 * @return reconstructed component
	 */
	Component< ? > reconstruct(MarkupContainer< ? > parent, int index)
	{
		Component< ? > component = componentSource.restoreComponent(id);

		if (parent != null)
		{
			component.setParent(parent);
		}

		component.beforeRender();

		parseComponentInfo(parent, componentInfo, component);

		return component;
	};

	/**
	 * Returns the first part of string that belongs to a single component
	 * 
	 * @param string
	 * @return first part of string that belongs to a single component
	 */
	private static String getComponentSubString(String string)
	{
		int len = string.length();

		int i = string.indexOf(',');
		if (i != -1 && i < len)
		{
			len = i;
		}

		i = string.indexOf(')');
		if (i != -1 && i < len)
		{
			len = i;
		}

		i = string.substring(0, len).indexOf('(');
		if (i != -1 && i < len)
		{
			len = i;
		}

		return string.substring(0, len);
	}

	/**
	 * Parses the component info substring and applies it to component with id specified in string
	 * that belongs to 'parent'. If the component is a MarkupContainer, returns the component
	 * instance otherwise returns null
	 * 
	 * @param parent
	 * @param info
	 * @param component
	 * @return
	 */
	private static MarkupContainer< ? > applyComponentInfo(MarkupContainer< ? > parent,
		String info, Component< ? > component)
	{
		if (parent == null)
		{
			return null;
		}

		String parts[] = info.split(" ");

		final String id = parts[0];
		final Object markupId;
		final int markupIndex;

		if (parts.length == 2)
		{
			markupId = null;
			markupIndex = Integer.parseInt(parts[1]);
		}
		else if (parts.length == 3)
		{

			if (parts[1] != null && parts[1].startsWith("*"))
			{
				markupId = Integer.valueOf(parts[1].substring(1));
			}
			else
			{
				markupId = parts[1];
			}
			markupIndex = Integer.parseInt(parts[2]);
		}
		else
		{
			throw new IllegalArgumentException("Malformed component info string '" + info + "'.");
		}

		if (component == null)
		{
			component = parent.get(id);
		}

		if (component == null)
		{
			logger.warn("Couldn't find component with id '" + id +
				"'. This means that the component was not properly reconstructed from ComponentSource.");
		}
		else
		{
			if (markupId != null)
			{
				component.setMarkupIdImpl(markupId);
			}
			component.markupIndex = (short)markupIndex;
		}
		return component instanceof MarkupContainer ? (MarkupContainer< ? >)component : null;
	}

	/**
	 * Parses the info string and applies the stored attributes (markupId and markupIndex) to the
	 * components (recursively)
	 * 
	 * @param component
	 *            The initial (root) reconstructed component. We need to specify this component
	 *            explicitly, because the parent still contains ComponentSourceEntry (and not the
	 *            component itself) during reconstruction
	 * @param parent
	 * @param info
	 * @return
	 */
	private static int parseComponentInfo(MarkupContainer< ? > parent, String info,
		Component< ? > component)
	{
		// find the first part for the component
		final String substring = getComponentSubString(info);

		// if it is followed by '(' it means there are children
		int len = substring.length();
		boolean hasChildren = false;
		if (len < info.length() && info.charAt(len) == '(')
		{
			hasChildren = true;
			++len; // skip the '('
		}

		final MarkupContainer< ? > child = applyComponentInfo(parent, substring, component);

		if (hasChildren)
		{
			int i = 0;
			String children = info.substring(len); // part with children info


			while (i < children.length())
			{
				if (children.charAt(i) == ',')
				{
					++i; // skip the ',' that can be left there from previous child
				}

				i += parseComponentInfo(child, children.substring(i), null);

				// if the child is followed by a ')' it means there are no more children left
				if (children.charAt(i) == ')')
				{
					++i;
					break;
				}
			}

			// advance by the length of component part and the length of children part
			return len + i;
		}
		else
		{
			// advance by the length of component part
			return len;
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(ComponentSourceEntry.class);


}