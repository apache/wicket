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
import java.util.List;

/**
 * A card on a board.
 *
 * @param id
 *            the id
 * @param title
 *            the title, plain text
 * @param description
 *            the description, HTML that went through {@link Descriptions#sanitize(String)}
 * @param due
 *            the due date, {@code null} for none
 * @param assigneeId
 *            the id of the member the card is assigned to, {@code null} for nobody
 * @param labels
 *            the labels
 * @param completedOn
 *            the day the card moved to {@link Column#DONE}, {@code null} while it is not done
 */
public record Card(String id, String title, String description, LocalDate due, String assigneeId,
	List<String> labels, LocalDate completedOn) implements Serializable
{
	public Card
	{
		labels = List.copyOf(labels);
	}

	Card completed(LocalDate day)
	{
		return new Card(id, title, description, due, assigneeId, labels, day);
	}

	Card edited(CardEdit edit)
	{
		return new Card(id, edit.title(), edit.description(), edit.due(), edit.assigneeId(), labels,
			completedOn);
	}
}
