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
package org.apache.wicket.dbsc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class StructuredFieldsTest
{
	@Test
	void stringEscapesQuoteAndBackslash()
	{
		assertEquals("\"a\\\\b\\\"c\"", StructuredFields.string("a\\b\"c"));
	}

	@Test
	void stringRefusesNonPrintableCharacters()
	{
		assertThrows(IllegalArgumentException.class, () -> StructuredFields.string("a\nb"));
		assertThrows(IllegalArgumentException.class, () -> StructuredFields.string("é"));
	}

	@Test
	void parseStringUnescapesAndDropsParameters()
	{
		assertEquals("a\\b\"c", StructuredFields.parseString(" \"a\\\\b\\\"c\";id=\"x\""));
	}

	@Test
	void parseStringTakesABareValueUpToItsParameters()
	{
		assertEquals("abc.def", StructuredFields.parseString("abc.def;x=1"));
	}

	@Test
	void parseStringRejectsMalformedValues()
	{
		assertNull(StructuredFields.parseString(null));
		assertNull(StructuredFields.parseString(" "));
		assertNull(StructuredFields.parseString("\"unterminated"));
		assertNull(StructuredFields.parseString("\"bad \\n escape\""));
	}
}
