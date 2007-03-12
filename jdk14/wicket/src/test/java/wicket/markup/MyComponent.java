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
package wicket.markup;

import wicket.markup.html.WebMarkupContainer;
import wicket.model.Model;

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
	//private Date dateParam;
	private String dateParam;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public MyComponent(final String id)
	{
		super(id, new Model(""));
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setIntParam(final int param)
	{
		this.intParam = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setIntegerParam(final Integer param)
	{
		this.integerParam = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setLong1Param(final long param)
	{
		this.long1Param = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setLong2Param(final Long param)
	{
		this.long2Param = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setFloat1Param(final float param)
	{
		this.float1Param = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setFloat2Param(final Float param)
	{
		this.float2Param = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setDouble1Param(final double param)
	{
		this.double1Param = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setDouble2Param(final Double param)
	{
		this.double2Param = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	//public void setDateParam(final Date param)
	public void setDateParam(final String param)
	{
		this.dateParam = param;
	}

	/**
	 * Sets the number of rows per page.
	 * 
	 * @param param
	 */
	public void setHexParam(final String param)
	{
		this.hexParam = param;
	}
	
	/**
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		StringBuffer str = new StringBuffer();
		
	    str.append("intParam: " + intParam + "<br/>");
	    str.append("integerParam: " + integerParam.toString() + "<br/>");
	    str.append("long1Param: " + long1Param + "<br/>");
	    str.append("long2Param: " + long2Param.toString() + "<br/>");
	    str.append("float1Param: " + float1Param + "<br/>");
	    str.append("float2Param: " + float2Param.toString() + "<br/>");
	    str.append("double1Param: " + double1Param + "<br/>");
	    str.append("double2Param: " + double2Param.toString() + "<br/>");
	    str.append("dateParam: " + dateParam + "<br/>");
	    str.append("hexParam: " + hexParam + "<br/>");
	    
	    getResponse().write(str);
	    
		super.onComponentTagBody(markupStream, openTag);
	}
}
