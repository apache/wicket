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
package org.apache.wicket.examples.islands.taskboard;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * The values the edit dialog's form works on.
 */
public class CardForm implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String title;

	private String description;

	private LocalDate due;

	private String assigneeId;

	static CardForm of(Card card)
	{
		CardForm form = new CardForm();
		form.title = card.title();
		form.description = card.description();
		form.due = card.due();
		form.assigneeId = card.assigneeId();
		return form;
	}

	CardEdit toEdit()
	{
		return new CardEdit(title, description == null ? "" : description, due, assigneeId);
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public LocalDate getDue()
	{
		return due;
	}

	public void setDue(LocalDate due)
	{
		this.due = due;
	}

	public String getAssigneeId()
	{
		return assigneeId;
	}

	public void setAssigneeId(String assigneeId)
	{
		this.assigneeId = assigneeId;
	}
}
