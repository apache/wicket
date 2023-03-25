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
package org.apache.commons.fileupload2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ParameterParser}.
 */
public class ParameterParserTest {

    @Test
    public void testParsing() {
        String s =
            "test; test1 =  stuff   ; test2 =  \"stuff; stuff\"; test3=\"stuff";
        final ParameterParser parser = new ParameterParser();
        Map<String, String> params = parser.parse(s, ';');
        assertNull(params.get("test"));
        assertEquals("stuff", params.get("test1"));
        assertEquals("stuff; stuff", params.get("test2"));
        assertEquals("\"stuff", params.get("test3"));

        params = parser.parse(s, new char[] {',', ';' });
        assertNull(params.get("test"));
        assertEquals("stuff", params.get("test1"));
        assertEquals("stuff; stuff", params.get("test2"));
        assertEquals("\"stuff", params.get("test3"));

        s = "  test  , test1=stuff   ,  , test2=, test3, ";
        params = parser.parse(s, ',');
        assertNull(params.get("test"));
        assertEquals("stuff", params.get("test1"));
        assertNull(params.get("test2"));
        assertNull(params.get("test3"));

        s = "  test";
        params = parser.parse(s, ';');
        assertNull(params.get("test"));

        s = "  ";
        params = parser.parse(s, ';');
        assertEquals(0, params.size());

        s = " = stuff ";
        params = parser.parse(s, ';');
        assertEquals(0, params.size());
    }

    @Test
    public void testContentTypeParsing() {
        final String s = "text/plain; Charset=UTF-8";
        final ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        final Map<String, String> params = parser.parse(s, ';');
        assertEquals("UTF-8", params.get("charset"));
    }

    @Test
    public void testParsingEscapedChars() {
        String s = "param = \"stuff\\\"; more stuff\"";
        final ParameterParser parser = new ParameterParser();
        Map<String, String> params = parser.parse(s, ';');
        assertEquals(1, params.size());
        assertEquals("stuff\\\"; more stuff", params.get("param"));

        s = "param = \"stuff\\\\\"; anotherparam";
        params = parser.parse(s, ';');
        assertEquals(2, params.size());
        assertEquals("stuff\\\\", params.get("param"));
        assertNull(params.get("anotherparam"));
    }

    // See: https://issues.apache.org/jira/browse/FILEUPLOAD-139
    @Test
    public void testFileUpload139() {
        final ParameterParser parser = new ParameterParser();
        String s = "Content-type: multipart/form-data , boundary=AaB03x";
        Map<String, String> params = parser.parse(s, new char[] {',', ';' });
        assertEquals("AaB03x", params.get("boundary"));

        s = "Content-type: multipart/form-data, boundary=AaB03x";
        params = parser.parse(s, new char[] {';', ',' });
        assertEquals("AaB03x", params.get("boundary"));

        s = "Content-type: multipart/mixed, boundary=BbC04y";
        params = parser.parse(s, new char[] {',', ';' });
        assertEquals("BbC04y", params.get("boundary"));
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-199">FILEUPLOAD-199</a>
     */
    @Test
    public void testFileUpload199() {
        final ParameterParser parser = new ParameterParser();
        final String s = "Content-Disposition: form-data; name=\"file\"; filename=\"=?ISO-8859-"
                + "1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= =?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n";
        final Map<String, String> params = parser.parse(s, new char[] { ',', ';' });
        assertEquals("If you can read this you understand the example.", params.get("filename"));
    }

    /**
     * Test for <a href="https://issues.apache.org/jira/browse/FILEUPLOAD-274">FILEUPLOAD-274</a>
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testFileUpload274() {
        final ParameterParser parser = new ParameterParser();

        // Should parse a UTF-8 charset
        String s = "Content-Disposition: form-data; "
                + "name=\"file\"; filename*=UTF-8''%E3%81%93%E3%82%93%E3%81%AB%E3%81%A1%E3%81%AF\r\n";
        Map<String, String> params = parser.parse(s, new char[] { ',', ';' });
        assertEquals("\u3053\u3093\u306B\u3061\u306F", params.get("filename")); //filename = "こんにちは" in japanese

        // Should parse ISO-8859-1 charset
        s = "Content-Disposition: form-data; name=\"file\"; filename*=UTF-8''%70%C3%A2%74%C3%A9\r\n";
        params = parser.parse(s, new char[] { ',', ';' });
        assertEquals("\u0070\u00e2\u0074\u00e9", params.get("filename")); //filename = "pâté" in french

        // Should not decode if '*' is not at the end of param-name
        s = "Content-Disposition: form-data; name=\"file\"; file*name=UTF-8''%61%62%63\r\n";
        params = parser.parse(s, new char[] {',', ';' });
        assertEquals("UTF-8''%61%62%63", params.get("file*name"));

        // Should not decode if param-value does not follow <charset>'<lang>'<encoded>
        s = "Content-Disposition: form-data; name=\"file\"; filename*=a'bc\r\n";
        params = parser.parse(s, new char[] {',', ';' });
        assertEquals("a'bc", params.get("filename"));

        // Should not decode if param-name doesn't have '*' at end
        s = "Content-Disposition: form-data; name=\"file\"; filename=a'b'c\r\n";
        params = parser.parse(s, new char[] {',', ';' });
        assertEquals("a'b'c", params.get("filename"));
    }

}
