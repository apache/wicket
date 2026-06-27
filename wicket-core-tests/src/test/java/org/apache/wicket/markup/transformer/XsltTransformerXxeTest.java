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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link XsltTransformer} does not resolve external entities declared in the markup it
 * transforms.
 */
class XsltTransformerXxeTest extends WicketTestCase
{
	@Test
	void externalEntityIsNotResolved() throws Exception
	{
		String secret = "T0P-SECRET-XXE-MARKER";
		File secretFile = File.createTempFile("wicket-xxe", ".txt");
		secretFile.deleteOnExit();
		Files.write(secretFile.toPath(), secret.getBytes(StandardCharsets.UTF_8));

		String payload = "<?xml version=\"1.0\"?>" +
			"<!DOCTYPE root [ <!ENTITY xxe SYSTEM \"" + secretFile.toURI() + "\"> ]>" +
			"<root>&xxe;</root>";

		Component component = new Label("xslt");
		XsltTransformer transformer =
			new XsltTransformer("org/apache/wicket/markup/transformer/copyText.xsl");

		String result;
		try
		{
			result = transformer.transform(component, payload).toString();
		}
		catch (Exception expected)
		{
			// a hardened factory rejects the external DTD access outright
			return;
		}
		assertFalse(result.contains(secret), "external entity was resolved by XsltTransformer");
	}
}
