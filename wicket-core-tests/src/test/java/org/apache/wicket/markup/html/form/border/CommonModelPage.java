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
package org.apache.wicket.markup.html.form.border;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 * 
 */
public class CommonModelPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	int quantity1;
	int quantity2;

	/**
	 * Construct.
	 */
	public CommonModelPage()
	{
		CommonBorder border = new CommonBorder("border");
		add(border);

		Form<Void> form1 = new Form<Void>("form1");
		border.add(form1);

		form1.add(new TextField<Integer>("quantity1", new PropertyModel<Integer>(this, "quantity1")));

		Form<Void> form2 = new Form<Void>("form2");
		border.add(form2);

		form2.add(new TextField<Integer>("quantity2", new PropertyModel<Integer>(this, "quantity2")));
	}

	/**
	 * @return quantity1
	 */
	public int getQuantity1()
	{
		return quantity1;
	}

	/**
	 * @param quantity1
	 */
	public void setQuantity1(int quantity1)
	{
		this.quantity1 = quantity1;
	}

	/**
	 * @return quantity2
	 */
	public int getQuantity2()
	{
		return quantity2;
	}

	/**
	 * @param quantity2
	 */
	public void setQuantity2(int quantity2)
	{
		this.quantity2 = quantity2;
	}
}
