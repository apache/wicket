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
package org.apache.wicket.util.tester;

import java.io.Serializable;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.INamedParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.StringValue;

/**
 * Mock form for use when testing FormTester's addFile functionality.
 * 
 * @author frankbille
 */
public class MockFormFileUploadPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Model object used in this test.
	 * 
	 * @author frankbille
	 */
	public static class MockDomainObjectFileUpload implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String text;

		/**
		 * @return text
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * @param text
		 */
		public void setText(String text)
		{
			this.text = text;
		}
	}

	private final MockDomainObjectFileUpload domainObject;

	private final FileUploadField fileUploadField;

	private FileUpload fileUpload;


	/**
	 * Construct.
	 */
	@SuppressWarnings("deprecation")
	public MockFormFileUploadPage()
	{
		this(new PageParameters().set("required", "true", INamedParameters.Type.MANUAL));
	}

	/**
	 * Construct.
	 * 
	 * @param param
	 */
	public MockFormFileUploadPage(final PageParameters param)
	{
		domainObject = new MockDomainObjectFileUpload();
		Form<MockDomainObjectFileUpload> form = new Form<MockDomainObjectFileUpload>("form",
			new CompoundPropertyModel<MockDomainObjectFileUpload>(domainObject))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit()
			{
				fileUpload = fileUploadField.getFileUpload();
			}
		};
		add(form);
		form.setMultiPart(true);
		form.setMaxSize(Bytes.kilobytes(100));
		form.add(new TextField<String>("text"));
		fileUploadField = new FileUploadField("file", new ListModel<FileUpload>());
		StringValue requiredParam = param.get("required");
		boolean required = requiredParam.toBoolean();
		fileUploadField.setRequired(required);
		form.add(fileUploadField);
	}

	/**
	 * @return domainObject
	 */
	public MockDomainObjectFileUpload getDomainObject()
	{
		return domainObject;
	}

	/**
	 * @return fileUpload
	 */
	public FileUpload getFileUpload()
	{
		return fileUpload;
	}

}
