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
package org.apache.wicket.markup.head;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.stream.Stream;

import org.apache.wicket.mock.MockWebResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class JavaScriptContentHeaderItemTest
{
    @Test
    void outputsTextJavascriptAsTypeIfNoTypeSet()
    {
        JavaScriptContentHeaderItem item = new JavaScriptContentHeaderItem("", "the id");
        MockWebResponse response = new MockWebResponse();

        item.render(response);

        assertEquals("""
                <script type="text/javascript" id="the id">
                /*<![CDATA[*/
                
                /*]]>*/
                </script>
                """, response.getTextResponse().toString());
    }

    @Test
    void outputsTypeThatIsSet()
    {
        JavaScriptContentHeaderItem item = new JavaScriptContentHeaderItem("", "the id")
                .setType(JavaScriptBrowserProcessedContentType.MODULE);
        MockWebResponse response = new MockWebResponse();

        item.render(response);

        assertEquals("""
                <script type="module" id="the id">
                /*<![CDATA[*/
                
                /*]]>*/
                </script>
                """, response.getTextResponse().toString());
    }


    @Test
    void itemsWithSameJavascriptAndDifferentTypesAreInequal()
    {
        JavaScriptContentHeaderItem item1 = new JavaScriptContentHeaderItem("", "the id")
                .setType(JavaScriptBrowserProcessedContentType.TEXT_JAVASCRIPT);
        JavaScriptContentHeaderItem item2 = new JavaScriptContentHeaderItem("", "the id")
                .setType(JavaScriptBrowserProcessedContentType.MODULE);

        assertNotEquals(item1, item2);
    }

    @Test
    void itemsWithSameJavascriptAndDifferentTypesHaveDifferentHashCodes()
    {
        JavaScriptContentHeaderItem item1 = new JavaScriptContentHeaderItem("", "the id")
                .setType(JavaScriptBrowserProcessedContentType.TEXT_JAVASCRIPT);
        JavaScriptContentHeaderItem item2 = new JavaScriptContentHeaderItem("", "the id")
                .setType(JavaScriptBrowserProcessedContentType.MODULE);

        assertNotEquals(item1.hashCode(), item2.hashCode());
    }

    @ParameterizedTest
    @MethodSource("contentTypes")
    void outputsContentTypeThatIsSet(final JavaScriptContentType contentType)
    {
        final JavaScriptHeaderItem item = new JavaScriptContentHeaderItem("", "the id").setContentType(contentType);
        final MockWebResponse response = new MockWebResponse();
        item.render(response);

        assertEquals("<script type=\"" + contentType.getType() + "\" id=\"the id\">\n" + """
            /*<![CDATA[*/
  
            /*]]>*/
            </script>
            """, response.getTextResponse().toString());
    }

    private static Stream<JavaScriptContentType> contentTypes()
    {
        return Stream.of(JavaScriptContentType.TEXT_JAVASCRIPT, JavaScriptContentType.MODULE, JavaScriptContentType.IMPORT_MAP, JavaScriptContentType.SPECULATION_RULES, (JavaScriptContentType) () -> "custom-type");
    }
}
