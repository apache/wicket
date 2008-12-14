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
package org.apache.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.PropertyModel;

/**
 * @author Juergen Donnerstag
 */
public class DropDownChoiceTestPage extends WebPage
{
	/** */
	public class DocumentType
	{
		private String name;
		private boolean hasExpiryDate;

		/**
		 * Construct.
		 * 
		 * @param name
		 * @param expiryDate
		 */
		public DocumentType(String name, boolean expiryDate)
		{
			this.name = name;
			hasExpiryDate = expiryDate;
		}

		/**
		 * Gets name.
		 * 
		 * @return name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Sets name.
		 * 
		 * @param name
		 *            name
		 */
		public void setName(String name)
		{
			this.name = name;
		}

		/**
		 * Gets hasExpiryDate.
		 * 
		 * @return hasExpiryDate
		 */
		public boolean getHasExpiryDate()
		{
			return hasExpiryDate;
		}

		/**
		 * Sets hasExpiryDate.
		 * 
		 * @param hasExpiryDate
		 *            hasExpiryDate
		 */
		public void setHasExpiryDate(boolean hasExpiryDate)
		{
			this.hasExpiryDate = hasExpiryDate;
		}
	}

	/**
	 * 
	 */
	public enum MyEnum {
		A("a"), B("b"), C("c"), D("d"), E("e");

		private String text;

		MyEnum(String text)
		{
			this.text = text;
		}
	}

	public DocumentType dtype;

	public String myDate;

	/**
	 * Construct.
	 * 
	 * @param name
	 */
	public DropDownChoiceTestPage()
	{
		Form<String> form = new MyForm("form");
		add(form);

		DocumentType[] docTypes = new DocumentType[3];
		docTypes[0] = new DocumentType("a", true);
		docTypes[1] = new DocumentType("b", false);
		docTypes[2] = new DocumentType("c", true);

		List<DocumentType> docTypes2 = new ArrayList<DocumentType>();
		docTypes2.add(new DocumentType("a", true));
		docTypes2.add(new DocumentType("b", false));
		docTypes2.add(new DocumentType("c", true));

		// List<DocumentType> allDocumentTypes = docTypes2;
		// List<DocumentType> allDocumentTypes = Arrays.asList(docTypes);
		List<MyEnum> allDocumentTypes = Arrays.asList(MyEnum.values());

		final DropDownChoice ddc = new DropDownChoice("dropdown", new PropertyModel(this, "dtype"),
			allDocumentTypes, new ChoiceRenderer("name"));

		TextField expiryDate = new TextField("text", new PropertyModel(this, "myDate"), Date.class)
		{
			@Override
			public boolean isRequired()
			{
				// ddc.validate();
				DocumentType dt = (DocumentType)ddc.getConvertedInput();
				return dt != null && dt.getHasExpiryDate();
			}
		};

		form.add(ddc);
		form.add(expiryDate);
	}

	/**
	 * 
	 */
	public class MyForm extends Form<String>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public MyForm(String id)
		{
			super(id);
		}
	}

	/**
	 * Gets dtype.
	 * 
	 * @return dtype
	 */
	public DocumentType getDtype()
	{
		return dtype;
	}

	/**
	 * Sets dtype.
	 * 
	 * @param dtype
	 *            dtype
	 */
	public void setDtype(DocumentType dtype)
	{
		this.dtype = dtype;
	}

	/**
	 * Gets myDate.
	 * 
	 * @return myDate
	 */
	public String getMyDate()
	{
		return myDate;
	}

	/**
	 * Sets myDate.
	 * 
	 * @param myDate
	 *            myDate
	 */
	public void setMyDate(String myDate)
	{
		this.myDate = myDate;
	}
}
