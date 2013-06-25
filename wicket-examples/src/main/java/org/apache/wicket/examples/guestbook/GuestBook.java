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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.util.value.ValueMap;


/**
 * A simple "guest book" example that allows visitors to the page to add a comment and see the
 * comments others have added.
 * 
 * For unit testing, added a parameter to clear the commentList.
 * 
 * @author Jonathan Locke
 * @author Martijn Dashorst
 */
public final class GuestBook extends WicketExamplePage
{
	/** A global list of all comments from all users across all sessions */
	private static final List<Comment> commentList = new ArrayList<>();

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 */
	public GuestBook()
	{
		// Add comment form
		add(new CommentForm("commentForm"));

		// Add commentListView of existing comments
		add(new PropertyListView<Comment>("comments", commentList)
		{
			@Override
			public void populateItem(final ListItem<Comment> listItem)
			{
				listItem.add(new Label("date"));
				listItem.add(new MultiLineLabel("text"));
			}
		}).setVersioned(false);
	}

	/**
	 * A form that allows a user to add a comment.
	 * 
	 * @author Jonathan Locke
	 */
	public final class CommentForm extends Form<ValueMap>
	{
		/**
		 * Constructor
		 * 
		 * @param id
		 *            The name of this component
		 */
		public CommentForm(final String id)
		{
			// Construct form with no validation listener
			super(id, new CompoundPropertyModel<>(new ValueMap()));

			// this is just to make the unit test happy
			setMarkupId("commentForm");

			// Add text entry widget
			add(new TextArea<>("text").setType(String.class));

			// Add simple automated spam prevention measure.
			add(new TextField<>("comment").setType(String.class));
		}

		/**
		 * Show the resulting valid edit
		 */
		@Override
		public final void onSubmit()
		{
			ValueMap values = getModelObject();

			// check if the honey pot is filled
			if (StringUtils.isNotBlank((String)values.get("comment")))
			{
				error("Caught a spammer!!!");
				return;
			}
			// Construct a copy of the edited comment
			Comment comment = new Comment();

			// Set date of comment to add
			comment.setDate(new Date());
			comment.setText((String)values.get("text"));
			commentList.add(0, comment);

			// Clear out the text component
			values.put("text", "");
		}
	}

	/**
	 * Clears the comments.
	 */
	public static void clear()
	{
		commentList.clear();
	}
}
