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

import org.apache.wicket.util.string.Strings;

/**
 * An implementation of IMarkupIdGenerator that uses the Session to generate
 * sequence numbers for the component markup ids.
 * As a prefix for the generated markup id in development mode it uses the component id
 * and in production mode the string <em>id</em>.
 */
public class DefaultMarkupIdGenerator implements IMarkupIdGenerator
{
	@Override
	public String generateMarkupId(Component component)
	{
		Object storedMarkupId = component.getMarkupIdImpl();
		if (storedMarkupId instanceof String)
		{
			return (String)storedMarkupId;
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
			markupIdPrefix = component.getId();
		}

		String markupIdPostfix = Integer.toHexString(generatedMarkupId).toLowerCase();

		String markupId = markupIdPrefix + markupIdPostfix;

		// make sure id is compliant with w3c requirements (starts with a letter)
		char c = markupId.charAt(0);
		if (!Character.isLetter(c))
		{
			markupId = "id" + markupId;
		}

		// escape some noncompliant characters
		markupId = Strings.replaceAll(markupId, "_", "__").toString();
		markupId = markupId.replace('.', '_');
		markupId = markupId.replace('-', '_');
		markupId = markupId.replace(' ', '_');

		return markupId;
	}
}
