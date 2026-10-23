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
package org.apache.wicket.examples.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.examples.guestbook.Comment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.ValidationError;

/**
 * The guestbook of {@link org.apache.wicket.examples.guestbook.GuestBook}, built.
 */
public class GuestBook extends BasePage
{
	private static final long serialVersionUID = 1L;

	private static final List<Comment> comments = new ArrayList<>();

	public GuestBook()
	{
		IModel<Comment> draft = Model.of(new Comment());

		add(form("commentForm", draft)
				.body(form -> form.add(
					textArea("text", form.bind(Comment::getText, Comment::setText)),
					textField("comment", Model.<String> of())
						.apply(honeypot -> honeypot.add(GuestBook::rejectSpam))))
				.onSubmit(GuestBook::save),
			list("comments", () -> comments)
				.each(row -> row.add(
					label("date", row.bind(Comment::getDate)),
					multiLineLabel("text", row.bind(Comment::getText))))
				.apply(list -> list.setVersioned(false)));
	}

	private static void save(Comment draft)
	{
		Comment comment = new Comment(draft);
		comment.setDate(new Date());
		comments.add(0, comment);
		draft.setText("");
	}

	private static void rejectSpam(IValidatable<String> honeypot)
	{
		if (!Strings.isEmpty(honeypot.getValue()))
		{
			honeypot.error(new ValidationError("Caught a spammer!!!"));
		}
	}
}
