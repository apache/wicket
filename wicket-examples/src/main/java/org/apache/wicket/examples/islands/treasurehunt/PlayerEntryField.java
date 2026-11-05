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
package org.apache.wicket.examples.islands.treasurehunt;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.islands.FormIsland;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * The player's name and colour, edited by the React component <code>PlayerEntryForm.tsx</code>
 * and submitted with the Wicket form around it.
 */
public class PlayerEntryField extends FormIsland<PlayerEntry, PlayerEntryField.EntryProps>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param palette
	 *            the colours to choose from
	 */
	public record EntryProps(List<String> palette) implements Serializable
	{
	}

	public PlayerEntryField(String id, IModel<PlayerEntry> model)
	{
		super(id, "player-entry", model, PlayerEntry.class,
			Model.of(new EntryProps(PlayerEntry.PALETTE)));
		setRequired(true);
		setLabel(Model.of("Your name"));
		add(new EntryValidator());
	}

	/**
	 * Checks what the browser sent: a name of 2 to 20 characters and a colour of the palette.
	 */
	private static class EntryValidator implements IValidator<PlayerEntry>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void validate(IValidatable<PlayerEntry> validatable)
		{
			PlayerEntry entry = validatable.getValue();
			String name = entry.name() == null ? "" : entry.name().strip();
			if (name.length() < 2 || name.length() > 20)
			{
				validatable.error(new ValidationError("Enter a name of 2 to 20 characters."));
			}
			if (!PlayerEntry.PALETTE.contains(entry.colour()))
			{
				validatable.error(new ValidationError("Choose one of the colours."));
			}
		}
	}
}
