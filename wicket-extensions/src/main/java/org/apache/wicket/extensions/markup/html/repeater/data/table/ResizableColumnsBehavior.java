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

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.CoreLibrariesContributor;
import org.apache.wicket.util.lang.Args;

import com.github.openjson.JSONObject;

/**
 * Lets the user resize the columns of a table by dragging the right edge of a header cell, or by
 * focusing that edge and pressing the left and right arrow keys. While dragging, a vertical guide
 * shows where the edge will land, and the new width is applied when the mouse is released.
 * <p>
 * Works on any {@code <table>} whose head has a row of {@code <th>} cells, such as a
 * {@link DataTable} or a
 * {@link org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable}.
 * The widths are kept in the browser, in a {@code <colgroup>} the behavior adds, so they survive
 * the header row being re-rendered by Ajax, but not a reload of the page.
 * <p>
 * In {@link Mode#FIT} the table keeps the full width of its container and a column takes the
 * space it gains from its right neighbour; in {@link Mode#STRETCH} every column, the last one too,
 * grows or shrinks on its own and the table becomes as wide as its columns. No column gets
 * narrower than the {@link #setMinColumnWidth(int) minimum width}.
 *
 * @since 11.0.0
 */
public class ResizableColumnsBehavior extends Behavior
{
	private static final long serialVersionUID = 1L;

	/** The script resizing the columns, {@code Wicket.ResizableColumns}. */
	public static final ResourceReference JS = new JavaScriptResourceReference(
		ResizableColumnsBehavior.class, "wicket-resizable-columns.js");

	/** The style sheet of the resize handles and guide. */
	public static final ResourceReference CSS = new CssResourceReference(
		ResizableColumnsBehavior.class, "wicket-resizable-columns.css");

	/**
	 * How the table's width follows its columns.
	 */
	public enum Mode
	{
		/**
		 * The table keeps the full width of its container; a column takes its new width from its
		 * right neighbour, so the last column has no handle of its own.
		 */
		FIT,

		/**
		 * The table is as wide as its columns, which grow or shrink independently, without
		 * limit beyond the minimum width.
		 */
		STRETCH
	}

	private final Mode mode;

	private int minColumnWidth = 30;

	/**
	 * Resizes in {@link Mode#FIT}.
	 */
	public ResizableColumnsBehavior()
	{
		this(Mode.FIT);
	}

	/**
	 * @param mode
	 *            how the table's width follows its columns
	 */
	public ResizableColumnsBehavior(Mode mode)
	{
		this.mode = Args.notNull(mode, "mode");
	}

	/**
	 * @return how the table's width follows its columns
	 */
	public Mode getMode()
	{
		return mode;
	}

	/**
	 * @return the width, in pixels, no column is resized below
	 */
	public int getMinColumnWidth()
	{
		return minColumnWidth;
	}

	/**
	 * @param minColumnWidth
	 *            the width, in pixels, no column is resized below
	 * @return {@code this}
	 */
	public ResizableColumnsBehavior setMinColumnWidth(int minColumnWidth)
	{
		this.minColumnWidth = Args.withinRange(0, Integer.MAX_VALUE, minColumnWidth,
			"minColumnWidth");
		return this;
	}

	@Override
	public void bind(Component component)
	{
		super.bind(component);
		component.setOutputMarkupId(true);
	}

	@Override
	public void onComponentTag(Component component, ComponentTag tag)
	{
		super.onComponentTag(component, tag);
		if ("table".equalsIgnoreCase(tag.getName()) == false)
		{
			throw new IllegalStateException(
				"ResizableColumnsBehavior has to be added to a component attached to a <table>, " +
					"not <" + tag.getName() + ">: " + component);
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response)
	{
		super.renderHead(component, response);
		CoreLibrariesContributor.contributeAjax(component.getApplication(), response);
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(CssHeaderItem.forReference(CSS));

		JSONObject options = new JSONObject();
		options.put("mode", mode.name().toLowerCase(Locale.ROOT));
		options.put("minWidth", minColumnWidth);
		response.render(OnDomReadyHeaderItem.forScript("Wicket.ResizableColumns.attach(" +
			JSONObject.quote(component.getMarkupId()) + ", " + options + ");"));
	}
}
