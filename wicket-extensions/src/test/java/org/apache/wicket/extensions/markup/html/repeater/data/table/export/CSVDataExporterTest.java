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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.Contact;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link CSVDataExporter}.
 */
public class CSVDataExporterTest extends WicketTestCase
{

	private List<Contact> contacts;

	@Before
	public void before() {
		contacts = Arrays.asList(new Contact(), new Contact());
		contacts.get(0).setFirstName("first0");
		contacts.get(0).setLastName("last\"0");
		contacts.get(1).setFirstName("first1");
		contacts.get(1).setLastName("last\"1");
	}
	
	@Test
	public void test() throws IOException
	{
		CSVDataExporter exporter = new CSVDataExporter();

		IDataProvider<Contact> dataProvider = new ListDataProvider<Contact>(contacts);

		List<IExportableColumn<Contact, ?>> columns = new ArrayList<>();
		columns.add(new PropertyColumn<>(Model.of("firstName"), "firstName"));
		columns.add(new PropertyColumn<>(Model.of("lastName"), "lastName"));

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		exporter.exportData(dataProvider, columns, output);

		assertEquals("\"firstName\",\"lastName\"\r\n" + //
			"\"first0\",\"last\"\"0\"\r\n" + //
			"\"first1\",\"last\"\"1\"\r\n" //
			, new String(output.toByteArray(), exporter.getCharacterSet()));
	}
}
