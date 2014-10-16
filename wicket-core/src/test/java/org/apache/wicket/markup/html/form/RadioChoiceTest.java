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
package org.apache.wicket.markup.html.form;

import java.util.Arrays;

import org.apache.wicket.WicketTestCase;
import org.junit.Test;

public class RadioChoiceTest extends WicketTestCase
{
	@Test
	public void defaultLabelPositionIsAfter() throws Exception
	{
		RadioChoice<Integer> radioChoice = new RadioChoice<Integer>("testid", Arrays.asList(1));
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><input name=\"testid\" type=\"radio\" value=\"0\" id=\"testid1-0\"/><label for=\"testid1-0\">1</label><br />\n</span>");
	}

	@Test
	public void labelPositionBefore() throws Exception
	{
		RadioChoice<Integer> radioChoice = new RadioChoice<Integer>("testid", Arrays.asList(1));
		radioChoice.setLabelPosition(AbstractChoice.LabelPosition.BEFORE);
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><label for=\"testid1-0\">1</label><input name=\"testid\" type=\"radio\" value=\"0\" id=\"testid1-0\"/><br />\n</span>");
	}

	@Test
	public void labelPositionWrapBefore() throws Exception
	{
		RadioChoice<Integer> radioChoice = new RadioChoice<Integer>("testid", Arrays.asList(1));
		radioChoice.setLabelPosition(AbstractChoice.LabelPosition.WRAP_BEFORE);
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><label>1 <input name=\"testid\" type=\"radio\" value=\"0\" id=\"testid1-0\"/></label><br />\n</span>");
	}

	@Test
	public void labelPositionWrapAfter() throws Exception
	{
		RadioChoice<Integer> radioChoice = new RadioChoice<Integer>("testid", Arrays.asList(1));
		radioChoice.setLabelPosition(AbstractChoice.LabelPosition.WRAP_AFTER);
		tester.startComponentInPage(radioChoice);

		tester.assertResultPage("<span wicket:id=\"testid\"><label><input name=\"testid\" type=\"radio\" value=\"0\" id=\"testid1-0\"/> 1</label><br />\n</span>");
	}
}
