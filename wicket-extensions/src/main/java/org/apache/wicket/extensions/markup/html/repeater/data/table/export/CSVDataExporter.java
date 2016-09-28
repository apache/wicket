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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;

/**
 * An {@link IDataExporter} that exports data to a CSV file. This class allows for customization of the exact CSV format, including
 * setting the delimiter, the text quoting character and the character set.
 * <p>
 * This class will export CSV files in a format consistent with RFC4180 by default.
 *
 * @author Jesse Long
 */
public class CSVDataExporter extends AbstractDataExporter
{
	private char delimiter = ',';

	private String characterSet = "utf-8";

	private char quoteCharacter = '"';

	private boolean exportHeadersEnabled = true;

	/**
	 * Creates a new instance.
	 */
	public CSVDataExporter()
	{
		super(Model.of("CSV"), "text/csv", "csv");
	}

	/**
	 * Sets the delimiter to be used to separate fields. The default delimiter is a colon.
	 *
	 * @param delimiter
	 *      The delimiter to be used to separate fields.
	 * @return {@code this}, for chaining.
	 */
	public CSVDataExporter setDelimiter(char delimiter)
	{
		this.delimiter = delimiter;
		return this;
	}

	/**
	 * Returns the delimiter to be used for separating fields.
	 *
	 * @return the delimiter to be used for separating fields.
	 */
	public char getDelimiter()
	{
		return delimiter;
	}

	/**
	 * Returns the character set encoding to be used when exporting data.
	 *
	 * @return the character set encoding to be used when exporting data.
	 */
	public String getCharacterSet()
	{
		return characterSet;
	}

	/**
	 * Sets the character set encoding to be used when exporting data. This defaults to UTF-8.
	 *
	 * @param characterSet
	 *      The character set encoding to be used when exporting data.
	 * @return {@code this}, for chaining.
	 */
	public CSVDataExporter setCharacterSet(String characterSet)
	{
		this.characterSet = Args.notNull(characterSet, "characterSer");
		return this;
	}

	/**
	 * Returns the character to be used for quoting fields.
	 *
	 * @return the character to be used for quoting fields.
	 */
	public char getQuoteCharacter()
	{
		return quoteCharacter;
	}

	/**
	 * Sets the character to be used to quote fields. This defaults to double quotes,
	 *
	 * @param quoteCharacter
	 *      The character to be used to quote fields.
	 * @return {@code this}, for chaining.
	 */
	public CSVDataExporter setQuoteCharacter(char quoteCharacter)
	{
		this.quoteCharacter = quoteCharacter;
		return this;
	}

	/**
	 * Returns the content type of the exported data. For CSV, this is normally
	 * "text/csv". This methods adds the character set and header values, in accordance with
	 * RFC4180.
	 *
	 * @return  the content type of the exported data.
	 */
	@Override
	public String getContentType()
	{
		return super.getContentType() + "; charset=" + characterSet + "; header=" + ((exportHeadersEnabled) ? "present" : "absent");
	}

	/**
	 * Turns on or off export headers functionality. If this is set to {@code true}, then the first
	 * line of the export will contain the column headers. This defaults to {@code true}.
	 *
	 * @param exportHeadersEnabled
	 *      A boolean indicating whether or not headers should be exported.
	 * @return {@code this}, for chaining.
	 */
	public CSVDataExporter setExportHeadersEnabled(boolean exportHeadersEnabled)
	{
		this.exportHeadersEnabled = exportHeadersEnabled;
		return this;
	}

	/**
	 * Indicates if header exporting is enabled. Defaults to {@code true}.
	 *
	 * @return a boolean indicating if header exporting is enabled.
	 */
	public boolean isExportHeadersEnabled()
	{
		return exportHeadersEnabled;
	}

	/**
	 * Quotes a value for export to CSV. According to RFC4180, this should just duplicate all occurrences
	 * of the quote character and wrap the result in the quote character.
	 *
	 * @param value
	 *      The value to be quoted.
	 * @return a quoted copy of the value.
	 */
	protected String quoteValue(String value)
	{
		return quoteCharacter + value.replace("" + quoteCharacter, "" + quoteCharacter + quoteCharacter) + quoteCharacter;
	}

	@Override
	public <T> void exportData(IDataProvider<T> dataProvider, List<IExportableColumn<T, ?>> columns, OutputStream outputStream)
		throws IOException
	{
		PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, Charset.forName(characterSet)));
		try
		{
			if (isExportHeadersEnabled())
			{
				boolean first = true;
				for (IExportableColumn<T, ?> col : columns)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						out.print(delimiter);
					}
					out.print(quoteValue(col.getDisplayModel().getObject()));
				}
				out.print("\r\n");
			}
			long numberOfRows = dataProvider.size();
			Iterator<? extends T> rowIterator = dataProvider.iterator(0, numberOfRows);
			while (rowIterator.hasNext())
			{
				T row = rowIterator.next();

				boolean first = true;
				for (IExportableColumn<T, ?> col : columns)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						out.print(delimiter);
					}

					Object o = col.getDataModel(dataProvider.model(row)).getObject();

					if (o != null)
					{
						Class<?> c = o.getClass();

						String s;

						IConverter converter = Application.get().getConverterLocator().getConverter(c);

						if (converter == null)
						{
							s = o.toString();
						}
						else
						{
							s = converter.convertToString(o, Session.get().getLocale());
						}

						out.print(quoteValue(s));
					}
				}
				out.print("\r\n");
			}
		}
		finally
		{
			out.close();
		}
	}
}
