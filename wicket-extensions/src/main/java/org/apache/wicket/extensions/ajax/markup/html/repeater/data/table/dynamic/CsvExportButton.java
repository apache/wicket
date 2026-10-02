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
package org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.dynamic;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.convert.IConverter;

/**
 * The icon button of a {@link CsvExportToolbarAction}.
 */
class CsvExportButton<T, K extends Serializable> extends Panel
{
	private static final long serialVersionUID = 1L;

	private final DynamicDataTable<T, K> table;

	private final CsvExportToolbarAction<T, K> action;

	CsvExportButton(String id, DynamicDataTable<T, K> table, CsvExportToolbarAction<T, K> action)
	{
		super(id);
		this.table = table;
		this.action = action;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		AjaxDownloadBehavior download = new AjaxDownloadBehavior(new CsvResource());
		download.setLocation(action.getLocation());
		add(download);

		IModel<String> label = label();
		add(new AjaxLink<Void>("link")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target)
			{
				download.initiate(target);
			}
		}.add(AttributeModifier.replace("title", label),
			AttributeModifier.replace("aria-label", label)));
	}

	private IModel<String> label()
	{
		IModel<String> label = action.getLabel();
		return label instanceof IComponentAssignedModel<String> assigned
			? assigned.wrapOnAssignment(this) : label;
	}

	private String header(IDynamicColumn<T> column)
	{
		IModel<String> header = column.getHeader();
		if (header instanceof IComponentAssignedModel<String> assigned)
		{
			IModel<String> wrapped = assigned.wrapOnAssignment(table);
			try
			{
				return wrapped.getObject();
			}
			finally
			{
				wrapped.detach();
			}
		}
		return header != null ? header.getObject() : null;
	}

	@SuppressWarnings("unchecked")
	private String format(Object value)
	{
		if (value == null)
		{
			return "";
		}
		if (value instanceof TemporalAccessor)
		{
			return value.toString();
		}
		IConverter<Object> converter = (IConverter<Object>)table.getConverter(value.getClass());
		return converter.convertToString(value, table.getLocale());
	}

	private String quote(String value)
	{
		return '"' + (value != null ? value.replace("\"", "\"\"") : "") + '"';
	}

	private void write(Writer writer) throws IOException
	{
		List<IDynamicColumn<T>> columns = new ArrayList<>();
		for (IDynamicColumn<T> column : table.getVisibleColumns())
		{
			if (column.isExportable())
			{
				columns.add(column);
			}
		}
		List<String> fields = new ArrayList<>();
		for (IDynamicColumn<T> column : columns)
		{
			fields.add(quote(header(column)));
		}
		writeLine(writer, fields);

		ISelection<T, K> selection = table.getSelection();
		Iterable<T> rows = selection.isEmpty() ? table.fetchAllRows() : selection.fetch();
		for (T row : rows)
		{
			fields.clear();
			for (IDynamicColumn<T> column : columns)
			{
				fields.add(quote(format(column.getExportValue(row))));
			}
			writeLine(writer, fields);
		}
	}

	private void writeLine(Writer writer, List<String> fields) throws IOException
	{
		writer.write(String.join(String.valueOf(action.getDelimiter()), fields));
		writer.write("\r\n");
	}

	private class CsvResource extends AbstractResource
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ResourceResponse newResourceResponse(Attributes attributes)
		{
			ResourceResponse response = new ResourceResponse();
			response.setContentType("text/csv");
			response.setTextEncoding(StandardCharsets.UTF_8.name());
			response.setFileName(action.getFileName());
			response.setContentDisposition(ContentDisposition.ATTACHMENT);
			response.disableCaching();
			response.setWriteCallback(new WriteCallback()
			{
				@Override
				public void writeData(Attributes attributes) throws IOException
				{
					Writer writer = new OutputStreamWriter(
						attributes.getResponse().getOutputStream(), StandardCharsets.UTF_8);
					write(writer);
					writer.flush();
				}
			});
			return response;
		}
	}
}
