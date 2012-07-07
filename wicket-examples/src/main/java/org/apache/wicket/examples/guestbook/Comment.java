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
package org.apache.wicket.examples.guestbook;

import java.util.Date;

import org.apache.wicket.util.io.IClusterable;


/**
 * Simple "POJO" bean for guestbook comments.
 * 
 * @author Jonathan Locke
 */
public class Comment implements IClusterable
{
	private String text;
	private Date date = new Date();

	/**
	 * Constructor
	 */
	public Comment()
	{
	}

	/**
	 * Copy constructor
	 * 
	 * @param comment
	 *            The comment to copy
	 */
	public Comment(final Comment comment)
	{
		this.text = comment.text;
		this.date = comment.date;
	}

	/**
	 * @return Returns the text.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @param text
	 *            The text to set.
	 */
	public void setText(String text)
	{
		this.text = text;
	}

	/**
	 * @return Returns the date.
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * @param date
	 *            The date to set.
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[Comment date = " + date + ", text = " + text + "]";
	}
}
