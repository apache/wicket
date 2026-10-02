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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Lets the user move the columns of a table to other places: a movable column gets a handle in its
 * header, which the user drags with the mouse, a pen or a finger and drops between two other
 * columns, a marker showing where it will land, or moves with the left and right arrow keys. A
 * column that is not movable keeps its place, so no marker is shown where it would move.
 * <p>
 * The behavior is added to a component attached to a {@code <table>} that implements
 * {@link IMovableColumnsTable}, such as an {@link ArrangeableDataTable} or a
 * {@link org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable}:
 * the table tells which columns can move, and a move is reported to it with an Ajax request,
 * after it was checked to leave every column that cannot move in its place. The handle's label
 * is the resource {@code MovableColumnsBehavior.moveColumn}, written escaped.
 *
 * @since 11.0.0
 */
public class MovableColumnsBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/** The script moving the columns in the browser. */
	public static final ResourceReference JS = new JavaScriptResourceReference(
		MovableColumnsBehavior.class, "wicket-movable-columns.js");

	/** The style sheet of the handles, the marker and the column being dragged. */
	public static final ResourceReference CSS = new CssResourceReference(
		MovableColumnsBehavior.class, "wicket-movable-columns.css");

	private static final String FROM_PARAMETER = "from";

	private static final String TO_PARAMETER = "to";

	@Override
	protected void onBind()
	{
		Args.isTrue(getComponent() instanceof IMovableColumnsTable,
			"MovableColumnsBehavior has to be added to an IMovableColumnsTable: %s", getComponent());
		super.onBind();
		getComponent().setOutputMarkupId(true);
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		if ("table".equalsIgnoreCase(tag.getName()) == false)
		{
			throw new IllegalStateException(
				"MovableColumnsBehavior has to be added to a component attached to a <table>, " +
					"not <" + tag.getName() + ">: " + getComponent());
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(CssHeaderItem.forReference(CSS));

		JSONArray movable = new JSONArray();
		boolean[] columns = ((IMovableColumnsTable)component).getMovableColumns();
		for (int i = 0; i < columns.length; i++)
		{
			if (columns[i])
			{
				movable.put(i);
			}
		}
		JSONObject options = new JSONObject();
		options.put("movable", movable);
		options.put("label", component.getString("MovableColumnsBehavior.moveColumn", null,
			"Move the column: drag it, or use the arrow keys"));
		response.render(OnDomReadyHeaderItem.forScript("var options = " + options + ";" +
			"options.onMove = " +
			getCallbackFunction(CallbackParameter.explicit(FROM_PARAMETER),
				CallbackParameter.explicit(TO_PARAMETER)) +
			";Wicket.MovableColumns.attach(" + JSONObject.quote(component.getMarkupId()) +
			", options);"));
	}

	/**
	 * Hands a move reported by the browser to the {@link IMovableColumnsTable}, ignoring it unless
	 * it moves a movable column and leaves every other column that is not movable in its place.
	 */
	@Override
	protected void respond(AjaxRequestTarget target)
	{
		IRequestParameters parameters = getComponent().getRequest().getRequestParameters();
		int from = parameters.getParameterValue(FROM_PARAMETER).toInt(-1);
		int to = parameters.getParameterValue(TO_PARAMETER).toInt(-1);
		IMovableColumnsTable table = (IMovableColumnsTable)getComponent();
		if (isValidMove(table.getMovableColumns(), from, to))
		{
			table.moveColumn(target, from, to);
		}
	}

	/**
	 * @param movable
	 *            whether each column is movable
	 * @param from
	 *            the index of the column to move
	 * @param to
	 *            the index of the column it goes in front of, or the number of columns for the end
	 * @return whether the move moves a movable column and leaves every other column that is not
	 *         movable in its place
	 */
	public static boolean isValidMove(boolean[] movable, int from, int to)
	{
		if (from < 0 || from >= movable.length || to < 0 || to > movable.length || to == from ||
			to == from + 1 || movable[from] == false)
		{
			return false;
		}
		int[] order = new int[movable.length];
		for (int i = 0, j = 0; i <= movable.length; i++)
		{
			if (i == to)
			{
				order[j++] = from;
			}
			if (i < movable.length && i != from)
			{
				order[j++] = i;
			}
		}
		for (int i = 0; i < order.length; i++)
		{
			if (movable[order[i]] == false && order[i] != i)
			{
				return false;
			}
		}
		return true;
	}
}
