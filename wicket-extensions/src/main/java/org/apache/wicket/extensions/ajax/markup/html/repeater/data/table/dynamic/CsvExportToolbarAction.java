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

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.AjaxDownloadBehavior.Location;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.lang.Args;

/**
 * An action exporting the rows of a {@link DynamicDataTable} as a CSV file, shown as an icon
 * button.
 * <p>
 * The file holds the {@link DynamicDataTable#getSelection() selected} rows if there are any, and
 * every row of the provider otherwise, in the provider's order. Its columns are those the table
 * shows that are {@link IDynamicColumn#isExportable() exportable}, in display order, headed by
 * their header text. Dates
 * and times of {@code java.time}, such as {@code LocalDate}, are written in ISO-8601, for example
 * {@code 1957-10-17}, so other tools read them unambiguously; other values are converted with the
 * table's converters for its locale. Every field is quoted.
 * <p>
 * The values are written as they are. A spreadsheet application may treat a value starting with
 * {@code =}, {@code +}, {@code -} or {@code @} as a formula, so an application exporting values
 * entered by users should make sure the export values are safe for its users' tools.
 *
 * @param <T>
 *            the type of the rows
 * @param <K>
 *            the type of the keys of the rows
 * @since 11.0.0
 */
public class CsvExportToolbarAction<T, K extends Serializable>
	implements
		IDynamicToolbarAction<T, K>
{
	private static final long serialVersionUID = 1L;

	private IModel<String> label = new ResourceModel("DynamicDataTable.exportCsv", "Export to CSV");

	private String fileName = "export.csv";

	private char delimiter = ',';

	private Location location = Location.Blob;

	@Override
	public Component newComponent(String id, DynamicDataTable<T, K> table)
	{
		return new CsvExportButton<>(id, table, this);
	}

	/**
	 * @return the label of the button, its tooltip and accessible name
	 */
	public IModel<String> getLabel()
	{
		return label;
	}

	/**
	 * @param label
	 *            the label of the button, its tooltip and accessible name, written escaped;
	 *            {@code DynamicDataTable.exportCsv} by default
	 * @return {@code this}
	 */
	public CsvExportToolbarAction<T, K> setLabel(IModel<String> label)
	{
		this.label = Args.notNull(label, "label");
		return this;
	}

	/**
	 * @return the name of the file, {@code export.csv} by default
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * @param fileName
	 *            the name of the file
	 * @return {@code this}
	 */
	public CsvExportToolbarAction<T, K> setFileName(String fileName)
	{
		this.fileName = Args.notEmpty(fileName, "fileName");
		return this;
	}

	/**
	 * @return the character separating the fields, a comma by default
	 */
	public char getDelimiter()
	{
		return delimiter;
	}

	/**
	 * @param delimiter
	 *            the character separating the fields
	 * @return {@code this}
	 */
	public CsvExportToolbarAction<T, K> setDelimiter(char delimiter)
	{
		this.delimiter = delimiter;
		return this;
	}

	/**
	 * @return where the browser downloads the file, {@link Location#Blob} by default
	 */
	public Location getLocation()
	{
		return location;
	}

	/**
	 * @param location
	 *            where the browser downloads the file
	 * @return {@code this}
	 */
	public CsvExportToolbarAction<T, K> setLocation(Location location)
	{
		this.location = Args.notNull(location, "location");
		return this;
	}
}
