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
package org.apache.wicket.markup.transformer;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.nio.file.Files;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link XsltTransformer} does not resolve external XML entities in the document it
 * transforms. The transformed source document is the decorated component's rendered output, which
 * can contain user data, so it must not be able to read external resources (XXE).
 */
class XsltTransformerXxeTest extends WicketTestCase
{
	@Test
	void externalEntityInSourceDocumentIsNotResolved() throws Exception
	{
		File secret = File.createTempFile("wicket-xxe", ".txt");
		String canary = "WICKET-XXE-CANARY-7f55";
		Files.write(secret.toPath(), canary.getBytes("UTF-8"));
		try
		{
			// The source document (would be the component's render output) carries an external
			// general entity pointing at the local secret file.
			String source = "<?xml version='1.0'?>"
				+ "<!DOCTYPE r [ <!ENTITY xxe SYSTEM '" + secret.toURI() + "'> ]>"
				+ "<r>&xxe;</r>";

			// A component is only needed so getResourceStream() can resolve style/locale; the
			// stylesheet is the existing identity-copy anyName.xsl from the outputTransformer tests.
			Label component = new Label("id", "x");
			tester.startComponentInPage(component);
			XsltTransformer transformer = new XsltTransformer(
				"org/apache/wicket/markup/outputTransformer/anyName.xsl");

			String out;
			try
			{
				out = transformer.transform(component, source).toString();
			}
			catch (Exception e)
			{
				// The parser is allowed to reject the DOCTYPE/entity outright; what matters is that
				// the secret is never exposed.
				out = "";
			}

			assertFalse(out.contains(canary),
				"XsltTransformer resolved an external entity (XXE): " + out);
		}
		finally
		{
			secret.delete();
		}
	}
}
