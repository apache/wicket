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

import org.apache.wicket.model.IModel;

/**
 * An abstract helper implementation of {@link IDataExporter}.
 *
 * @author Jesse Long
 */
public abstract class AbstractDataExporter implements IDataExporter
{
	private IModel<String> dataFormatNameModel;

	private String contentType;

	private String fileNameExtension;

	/**
	 * Creates a new instance with the data format name model, content type and file name extensions provided.
	 *
	 * @param dataFormatNameModel
	 *      The model of the exported data format name.
	 * @param contentType
	 *      The MIME content type of the exported data type.
	 * @param fileNameExtension
	 *      The file name extensions to use in the file name for the exported data.
	 */
	public AbstractDataExporter(IModel<String> dataFormatNameModel, String contentType, String fileNameExtension)
	{
		this.dataFormatNameModel = dataFormatNameModel;
		this.contentType = contentType;
		this.fileNameExtension = fileNameExtension;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public IModel<String> getDataFormatNameModel()
	{
		return dataFormatNameModel;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public String getFileNameExtension()
	{
		return fileNameExtension;
	}

	/**
	 * Sets the MIME contentType for the data export format.
	 *
	 * @param contentType
	 *      The MIME contentType for the data export format.
	 * @return {@code this}, for chaining.
	 */
	public AbstractDataExporter setContentType(String contentType)
	{
		this.contentType = contentType;
		return this;
	}

	/**
	 * Sets the data format name model.
	 *
	 * @param dataFormatNameModel
	 *      the data format name model.
	 * @return {@code this}, for chaining.
	 */
	public AbstractDataExporter setDataFormatNameModel(IModel<String> dataFormatNameModel)
	{
		this.dataFormatNameModel = dataFormatNameModel;
		return this;
	}

	/**
	 * Sets the file name extension to be used in the exported file name.
	 *
	 * @param fileNameExtension
	 *      the file name extension to be used in the exported file name.
	 * @return {@code this}, for chaining.
	 */
	public AbstractDataExporter setFileNameExtension(String fileNameExtension)
	{
		this.fileNameExtension = fileNameExtension;
		return this;
	}
}
