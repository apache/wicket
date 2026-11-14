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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * The card opened last, rendered by Wicket. Its description is rendered without escaping, which is
 * only safe because {@link RichTextField} sanitized it on the way in; the seeded descriptions are
 * written by the application itself.
 */
public class CardPreview extends Panel
{
	private static final long serialVersionUID = 1L;

	private final IModel<String> cardId;

	public CardPreview(String id, IModel<Board> board, IModel<String> cardId)
	{
		super(id);
		this.cardId = cardId;
		setOutputMarkupPlaceholderTag(true);

		IModel<Card> card = board.combineWith(cardId,
			(b, selected) -> selected == null ? null : b.findCard(selected).orElse(null));
		add(new Label("title", card.map(Card::title)));
		add(new Label("description", card.map(Card::description))
			.setEscapeModelStrings(false));
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		setVisible(cardId.getObject() != null);
	}
}
