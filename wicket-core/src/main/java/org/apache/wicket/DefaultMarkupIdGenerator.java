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
package org.apache.wicket;

import java.util.Locale;

import org.apache.wicket.util.string.Strings;

/**
 * An implementation of IMarkupIdGenerator that uses the Session to generate
 * sequence numbers for the component markup ids.
 * As a prefix for the generated markup id in development mode it uses the component id
 * and in production mode the string <em>id</em>. In development mode an underscore separates
 * the component id from the sequence number, so that, for example, component <em>1</em> with
 * sequence <em>1a</em> and component <em>11</em> with sequence <em>a</em> get different ids.
 */
public class DefaultMarkupIdGenerator implements IMarkupIdGenerator
{
	@Override
	public String generateMarkupId(Component component, boolean createIfDoesNotExist)
	{
		Object storedMarkupId = component.getMarkupIdImpl();
		if (storedMarkupId instanceof String)
		{
			return (String)storedMarkupId;
		}

		if (storedMarkupId == null && createIfDoesNotExist == false)
		{
			return null;
		}

		Session session = component.getSession();
		int generatedMarkupId = storedMarkupId instanceof Integer ? (Integer)storedMarkupId
				: session.nextSequenceValue();

		if (generatedMarkupId == 0xAD)
		{
			// WICKET-4559 skip suffix 'ad' because some ad-blocking solutions may hide the component
			generatedMarkupId = session.nextSequenceValue();
		}

		if (storedMarkupId == null)
		{
			component.setMarkupIdImpl(generatedMarkupId);
		}

		String markupIdPrefix = "id";
		if (component.getApplication().usesDevelopmentConfig())
		{
			// in non-deployment mode we make the markup id include component id
			// so it is easier to debug
			markupIdPrefix = escape(component.getId()) + "_";
		}

		String markupIdPostfix = Integer.toHexString(generatedMarkupId).toLowerCase(Locale.ROOT);

		String markupId = markupIdPrefix + markupIdPostfix;

		// make sure id is compliant with w3c requirements (starts with a letter)
		char c = markupId.charAt(0);
		if (!Character.isLetter(c))
		{
			markupId = "id" + markupId;
		}

		return markupId;
	}

	private static String escape(String componentId)
	{
		String escaped = Strings.replaceAll(componentId, "_", "__").toString();
		escaped = escaped.replace('.', '_');
		escaped = escaped.replace('-', '_');
		escaped = escaped.replace(' ', '_');
		// a component id may contain these, a markup id may not
		escaped = escaped.replace('(', '_');
		escaped = escaped.replace(')', '_');
		escaped = escaped.replace('[', '_');
		escaped = escaped.replace(']', '_');
		return escaped;
	}
}
