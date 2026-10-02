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

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
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

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Lets the user resize the columns of a table by dragging the right edge of a header cell, or by
 * focusing that edge and pressing the left and right arrow keys. While dragging, a vertical guide
 * shows where the edge will land, and the new width is applied when the mouse is released.
 * <p>
 * Works on any {@code <table>} whose head has a row of {@code <th>} cells, such as a
 * {@link DataTable} or a
 * {@link org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic.DynamicDataTable}.
 * The widths are applied in the browser, in a {@code <colgroup>} the behavior adds, so they
 * survive the header row being re-rendered by Ajax. A table implementing
 * {@link IResizableColumnsTable} keeps them on the server too: the behavior hands them to the
 * browser whenever the table is rendered and reports every resize back with an Ajax request.
 * Other tables lose them when they are rendered again.
 * <p>
 * In {@link Mode#FIT} the table keeps the full width of its container and a column takes the
 * space it gains from its right neighbour; in {@link Mode#STRETCH} every column, the last one too,
 * grows or shrinks on its own and the table becomes as wide as its columns. No column gets
 * narrower than the {@link #setMinColumnWidth(int) minimum width}.
 *
 * @since 11.0.0
 */
public class ResizableColumnsBehavior extends AbstractDefaultAjaxBehavior
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

	/** The widest a column may be reported, in pixels. */
	public static final double MAX_COLUMN_WIDTH = 100_000;

	private static final String WIDTHS_PARAMETER = "widths";

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
	protected void onBind()
	{
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
				"ResizableColumnsBehavior has to be added to a component attached to a <table>, " +
					"not <" + tag.getName() + ">: " + getComponent());
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
		StringBuilder script = new StringBuilder("var options = ").append(options).append(";");
		if (component instanceof IResizableColumnsTable table)
		{
			double[] widths = table.getShownColumnWidths();
			if (widths != null && Arrays.stream(widths).allMatch(width -> width > 0))
			{
				script.append("options.widths = ").append(new JSONArray(widths)).append(";");
			}
			script.append("options.onResize = function (widths) { (")
				.append(getCallbackFunction(CallbackParameter.explicit(WIDTHS_PARAMETER)))
				.append(")(widths.map(function (width) { return Math.round(width * 10) / 10; })")
				.append(".join(',')); };");
		}
		script.append("Wicket.ResizableColumns.attach(")
			.append(JSONObject.quote(component.getMarkupId()))
			.append(", options);");
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	/**
	 * Hands the widths reported by the browser to the {@link IResizableColumnsTable}, ignoring
	 * them unless every one is a positive, finite number of at most {@value #MAX_COLUMN_WIDTH}
	 * pixels.
	 */
	@Override
	protected void respond(AjaxRequestTarget target)
	{
		if (getComponent() instanceof IResizableColumnsTable table)
		{
			String value = getComponent().getRequest()
				.getRequestParameters()
				.getParameterValue(WIDTHS_PARAMETER)
				.toOptionalString();
			double[] widths = parseWidths(value);
			if (widths != null)
			{
				table.columnsResized(target, widths);
			}
		}
	}

	private static double[] parseWidths(String value)
	{
		if (value == null || value.isEmpty())
		{
			return null;
		}
		String[] parts = value.split(",");
		double[] widths = new double[parts.length];
		for (int i = 0; i < parts.length; i++)
		{
			try
			{
				widths[i] = Double.parseDouble(parts[i]);
			}
			catch (NumberFormatException e)
			{
				return null;
			}
			if (Double.isFinite(widths[i]) == false || widths[i] <= 0 ||
				widths[i] > MAX_COLUMN_WIDTH)
			{
				return null;
			}
		}
		return widths;
	}
}
