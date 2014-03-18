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
package org.apache.wicket.extensions.markup.html.repeater.data.table.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceStreamResource;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.IResourceStreamWriter;

/**
 * A toolbar that provides links to download the data represented by all {@link IExportableColumn}s in the table
 * exported to formats supported by the {@link IDataExporter}s configured.
 *
 * @author Jesse Long
 * @see IDataExporter
 * @see IExportableColumn
 */
public class ExportToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;

	private static final IModel<String> DEFAULT_MESSAGE_MODEL = new ResourceModel(
		"datatable.export-to");

	private static final IModel<String> DEFAULT_FILE_NAME_MODEL = new ResourceModel(
		"datatable.export-file-name");

	private final List<IDataExporter> dataExporters = new LinkedList<>();

	private IModel<String> messageModel;

	private IModel<String> fileNameModel;

	/**
	* Creates a new instance with the default message model. This instance will use "export." as the exported
	* file name prefix.
	 *
	* @param table
	 *      The data table this toolbar belongs to.
	*/
	public ExportToolbar(final DataTable<?, ?> table)
	{
		this(table, DEFAULT_MESSAGE_MODEL, DEFAULT_FILE_NAME_MODEL);
	}

	/**
	 * Creates a new instance with the provided data table and file name model.
	 *
	 * @param table
	 *      The table to which this toolbar belongs.
	 * @param fileNameModel
	 *      The model of the file name. This should exclude the file extensions.
	 */
	public ExportToolbar(DataTable<?, ?> table, IModel<String> fileNameModel)
	{
		this(table, DEFAULT_MESSAGE_MODEL, fileNameModel);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param table
	 *      The table to which this toolbar belongs.
	 * @param messageModel
	 *      The model of the export message.
	 * @param fileNameModel
	 *      The model of the file name. This should exclude the file extensions.
	 */
	public ExportToolbar(DataTable<?, ?> table, IModel<String> messageModel, IModel<String> fileNameModel)
	{
		super(table);
		this.messageModel = messageModel;
		this.fileNameModel = fileNameModel;
	}

	/**
	 * Sets the models of the export message displayed in the toolbar.
	 *
	 * @param messageModel
	 *      the models of the export message displayed in the toolbar.
	 * @return {@code this}, for chaining.
	 */
	public ExportToolbar setMessageModel(IModel<String> messageModel)
	{
		this.messageModel = Args.notNull(messageModel, "messageModel");
		return this;
	}

	/**
	 * Sets the model of the file name used for the exported data.
	 *
	 * @param fileNameModel
	 *      The model of the file name used for the exported data.
	 * @return {@code this}, for chaining.
	 */
	public ExportToolbar setFileNameModel(IModel<String> fileNameModel)
	{
		this.fileNameModel = Args.notNull(fileNameModel, "fileNameModel");
		return this;
	}

	/**
	 * Returns the model of the file name used for the exported data.
	 *
	 * @return the model of the file name used for the exported data.
	 */
	public IModel<String> getFileNameModel()
	{
		return fileNameModel;
	}

	/**
	 * Returns the model of the export message displayed in the toolbar.
	 *
	 * @return the model of the export message displayed in the toolbar.
	 */
	public IModel<String> getMessageModel()
	{
		return messageModel;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		WebMarkupContainer td = new WebMarkupContainer("td");
		add(td);

		td.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel<String>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject()
			{
				return String.valueOf(getTable().getColumns().size()).intern();
			}
		}));

		td.add(new Label("exportTo", messageModel));

		RepeatingView linkContainers = new RepeatingView("linkContainer");
		td.add(linkContainers);

		for (IDataExporter exporter : dataExporters)
		{
			WebMarkupContainer span = new WebMarkupContainer(linkContainers.newChildId());
			linkContainers.add(span);

			span.add(createExportLink("exportLink", exporter));
		}
	}

	/**
	 * Creates a new link to the exported data for the provided {@link IDataExporter}.
	 *
	 * @param componentId
	 *      The component of the link.
	 * @param dataExporter
	 *      The data exporter to use to export the data.
	 * @return a new link to the exported data for the provided {@link IDataExporter}.
	 */
	protected Component createExportLink(String componentId, final IDataExporter dataExporter)
	{
		IResource resource = new ResourceStreamResource()
		{
			@Override
			protected IResourceStream getResourceStream()
			{
				return new DataExportResourceStreamWriter(dataExporter, getTable());
			}
		}.setFileName(fileNameModel.getObject() + "." + dataExporter.getFileNameExtension());

		return new ResourceLink<Void>(componentId, resource)
			.setBody(dataExporter.getDataFormatNameModel());
	}

	/**
	 * This toolbar is only visible if there are rows in the data set and if there are exportable columns in the
	 * data table and if there are data exporters added to the toolbar.
	 */
	@Override
	public boolean isVisible()
	{
		if (dataExporters.isEmpty())
		{
			return false;
		}

		if (getTable().getRowCount() == 0)
		{
			return false;
		}

		for (IColumn<?, ?> col : getTable().getColumns())
		{
			if (col instanceof IExportableColumn)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	protected void onDetach()
	{
		fileNameModel.detach();
		messageModel.detach();
		super.onDetach();
	}

	/**
	 * Adds a {@link IDataExporter} to the list of data exporters to be used in this toolbar.
	 *
	 * @param exporter
	 *      The {@link IDataExporter} to add to the toolbar.
	 * @return {@code this}, for chaining.
	 */
	public ExportToolbar addDataExporter(IDataExporter exporter)
	{
		Args.notNull(exporter, "exporter");
		dataExporters.add(exporter);
		return this;
	}

	/**
	 * An {@link IResourceStreamWriter} which writes the exportable data from a table to an output stream.
	 */
	public static class DataExportResourceStreamWriter extends AbstractResourceStreamWriter
	{
		private final IDataExporter dataExporter;

		private final DataTable<?, ?> dataTable;

		/**
		 * Creates a new instance using the provided {@link IDataExporter} to export data.
		 *
		 * @param dataExporter
		 *      The {@link IDataExporter} to use to export data.
		 * @param dataTable
		 *      The {@link DataTable} from which to export.
		 */
		public DataExportResourceStreamWriter(IDataExporter dataExporter, DataTable<?, ?> dataTable)
		{
			this.dataExporter = dataExporter;
			this.dataTable = dataTable;
		}

		/**
		 * Writes the exported data to the output stream. This implementation calls
		 * {@link #exportData(org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable, org.apache.wicket.extensions.markup.html.repeater.data.table.export.IDataExporter, java.io.OutputStream) }.
		 *
		 * @param output
		 *      The output stream to which to export the data.
		 * @throws IOException if an error occurs.
		 */
		@Override
		public void write(OutputStream output)
			throws IOException
		{
			exportData(dataTable, dataExporter, output);
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This method returns the content type returned by {@link IDataExporter#getContentType()}.
		 *
		 * @return the content type returned by {@link IDataExporter#getContentType()}.
		 */
		@Override
		public String getContentType()
		{
			return dataExporter.getContentType();
		}

		/**
		 * Exports the data from the provided data table to the provided output stream.
		 * This methods calls {@link IDataExporter#exportData(org.apache.wicket.markup.repeater.data.IDataProvider, java.util.List, java.io.OutputStream) }
		 * passing it the {@link IDataProvider} of the {@link DataTable}, and a list of the {@link IExportableColumn}s in the table.
		 *
		 * @param <T>
		 *      The type of each row in the data table.
		 * @param <S>
		 *      The type of the sort property of the table.
		 * @param dataTable
		 *      The {@link DataTable} to export.
		 * @param dataExporter
		 *      The {@link IDataExporter} to use to export the data.
		 * @param outputStream
		 *      The {@link OutputStream} to which the data should be exported to.
		 * @throws IOException
		 */
		private <T, S> void exportData(DataTable<T, S> dataTable, IDataExporter dataExporter, OutputStream outputStream)
			throws IOException
		{
			IDataProvider<T> dataProvider = dataTable.getDataProvider();
			List<IExportableColumn<T, ?>> exportableColumns = new LinkedList<>();
			for (IColumn<T, S> col : dataTable.getColumns())
			{
				if (col instanceof IExportableColumn)
				{
					exportableColumns.add((IExportableColumn<T, ?>)col);
				}
			}
			dataExporter.exportData(dataProvider, exportableColumns, outputStream);
		}
	}
}
