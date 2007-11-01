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
package org.apache.wicket.markup.html.link;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.wicket.markup.html.WebPage;


/**
 * test page with various downloads.
 */
public class DownloadPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	static final String HELLO_WORLD = "Hello, World!";
	static final String TEXT_DOWNLOAD_LINK = "textDownload";
	static final String PDF_DOWNLOAD_LINK = "pdfDownload";
	static final String DELETE_DOWNLOAD_LINK = "deleteDownload";
	static final String CUSTOM_DOWNLOAD_LINK = "customDownload";

	private final File temporaryFile;

	/**
	 * Construct.
	 * 
	 * @throws IOException
	 */
	public DownloadPage() throws IOException
	{
		File textFile = File.createTempFile("Download", ".txt");
		add(new DownloadLink(TEXT_DOWNLOAD_LINK, textFile));

		File pdfFile = File.createTempFile("Download", ".pdf");
		FileWriter writer = new FileWriter(pdfFile);
		writer.write(HELLO_WORLD);
		writer.close();
		add(new DownloadLink(PDF_DOWNLOAD_LINK, pdfFile));

		File customFile = File.createTempFile("Download", ".custom");
		add(new DownloadLink(CUSTOM_DOWNLOAD_LINK, customFile));

		temporaryFile = File.createTempFile("Download", ".delete");
		add(new DownloadLink(DELETE_DOWNLOAD_LINK, temporaryFile).setDeleteAfterDownload(true));
	}

	/**
	 * 
	 * @return temporary file
	 */
	public File getTemporaryFile()
	{
		return temporaryFile;
	}
}
