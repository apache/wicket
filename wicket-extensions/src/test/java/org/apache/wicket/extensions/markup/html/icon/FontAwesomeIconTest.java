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
package org.apache.wicket.extensions.markup.html.icon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FontAwesomeIconTest
{
	@Test
	void theNameIsTheConstantInLowerCaseWithDashes()
	{
		assertEquals("pen-to-square", FontAwesomeIcon.PEN_TO_SQUARE.getName());
		assertEquals("digital-tachograph", FontAwesomeIcon.DIGITAL_TACHOGRAPH.getName());
	}

	@Test
	void theDigitsHaveNoPrefix()
	{
		assertEquals("0", FontAwesomeIcon.DIGIT_0.getName());
		assertEquals("9", FontAwesomeIcon.DIGIT_9.getName());
	}

	@Test
	void theMarkupIsTheSolidIconOfTheFont()
	{
		assertEquals("<i class=\"fas fa-trash-can\" aria-hidden=\"true\"></i>",
			FontAwesomeIcon.TRASH_CAN.getMarkup());
	}
}
