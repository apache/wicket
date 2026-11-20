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
package org.apache.wicket.csp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class RelativeURICSPValueTest
{
	@Test
	void checkValidityForSrc_withRelativeUri_accepted()
	{
		assertDoesNotThrow(() -> new RelativeURICSPValue("static/js/app.js").checkValidityForSrc());
	}

	@Test
	void checkValidityForSrc_withAbsoluteUri_throwsIllegalArgumentException()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new RelativeURICSPValue("https://example.com/path").checkValidityForSrc());
	}

	@Test
	void checkValidityForSrc_withAbsolutePath_throwsIllegalArgumentException()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new RelativeURICSPValue("/absolute/path").checkValidityForSrc());
	}

	@Test
	void checkValidityForSrc_withInvalidSyntax_throwsIllegalArgumentException()
	{
		assertThrows(IllegalArgumentException.class,
			() -> new RelativeURICSPValue("path with spaces").checkValidityForSrc());
	}
}
