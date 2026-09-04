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
package org.apache.wicket.markup;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

/**
 * Dummy component used for ComponentCreateTagTest
 * 
 * @author Juergen Donnerstag
 */
public class MyComponent extends WebMarkupContainer
{
	private static final long serialVersionUID = 1L;

	private int intParam;
	private Integer integerParam;
	private long long1Param;
	private Long long2Param;
	private float float1Param;
	private Float float2Param;
	private double double1Param;
	private Double double2Param;
	private String hexParam;
	// private Date dateParam;
	private String dateParam;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public MyComponent(final String id)
	{
		super(id, new Model<String>(""));
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param intParam
	 */
	public void setIntParam(final int intParam)
	{
		this.intParam = intParam;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param integerParam
	 */
	public void setIntegerParam(final Integer integerParam)
	{
		this.integerParam = integerParam;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param long1Param
	 */
	public void setLong1Param(final long long1Param)
	{
		this.long1Param = long1Param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param long2Param
	 */
	public void setLong2Param(final Long long2Param)
	{
		this.long2Param = long2Param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param float1Param
	 */
	public void setFloat1Param(final float float1Param)
	{
		this.float1Param = float1Param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param float2Param
	 */
	public void setFloat2Param(final Float float2Param)
	{
		this.float2Param = float2Param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param double1Param
	 */
	public void setDouble1Param(final double double1Param)
	{
		this.double1Param = double1Param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param double2Param
	 */
	public void setDouble2Param(final Double double2Param)
	{
		this.double2Param = double2Param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param dateParam
	 */
	// public void setDateParam(final Date dateParam)
	public void setDateParam(final String dateParam)
	{
		this.dateParam = dateParam;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param hexParam
	 */
	public void setHexParam(final String hexParam)
	{
		this.hexParam = hexParam;
	}

	/**
	 * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
	 *      org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		StringBuilder str = new StringBuilder();

		str.append("intParam: ").append(intParam).append("<br/>");
		str.append("integerParam: ").append(integerParam.toString()).append("<br/>");
		str.append("long1Param: ").append(long1Param).append("<br/>");
		str.append("long2Param: ").append(long2Param.toString()).append("<br/>");
		str.append("float1Param: ").append(float1Param).append("<br/>");
		str.append("float2Param: ").append(float2Param.toString()).append("<br/>");
		str.append("double1Param: ").append(double1Param).append("<br/>");
		str.append("double2Param: ").append(double2Param.toString()).append("<br/>");
		str.append("dateParam: ").append(dateParam).append("<br/>");
		str.append("hexParam: ").append(hexParam).append("<br/>");

		getResponse().write(str);

		super.onComponentTagBody(markupStream, openTag);
	}
}
