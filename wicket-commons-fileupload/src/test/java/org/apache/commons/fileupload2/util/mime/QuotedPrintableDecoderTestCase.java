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
package org.apache.commons.fileupload2.util.mime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * @since 1.3
 */
public final class QuotedPrintableDecoderTestCase {

    @Test
    public void emptyDecode() throws Exception {
        assertEncoded("", "");
    }

    @Test
    public void plainDecode() throws Exception {
        // spaces are allowed in encoded data
        // There are special rules for trailing spaces; these are not currently implemented.
        assertEncoded("The quick brown fox jumps over the lazy dog.", "The quick brown fox jumps over the lazy dog.");
    }

    @Test
    public void basicEncodeDecode() throws Exception {
        assertEncoded("= Hello there =\r\n", "=3D Hello there =3D=0D=0A");
    }

    @Test
    public void invalidQuotedPrintableEncoding() throws Exception {
        assertIOException("truncated escape sequence", "YWJjMTIzXy0uKn4hQCMkJV4mKCkre31cIlxcOzpgLC9bXQ==");
    }

    @Test
    public void unsafeDecode() throws Exception {
        assertEncoded("=\r\n", "=3D=0D=0A");
    }

    @Test
    public void unsafeDecodeLowerCase() throws Exception {
        assertEncoded("=\r\n", "=3d=0d=0a");
    }

    @Test
    public void invalidCharDecode() {
        assertThrows(IOException.class, () -> assertEncoded("=\r\n", "=3D=XD=XA"));
    }

    /**
     * This is NOT supported by Commons-Codec, see CODEC-121.
     *
     * @throws Exception
     * @see <a href="https://issues.apache.org/jira/browse/CODEC-121">CODEC-121</a>
     */
    @Test
    public void softLineBreakDecode() throws Exception {
        assertEncoded("If you believe that truth=beauty, then surely mathematics is the most "
                        +  "beautiful branch of philosophy.", "If you believe that truth=3Dbeauty, then "
                        + "surely=20=\r\nmathematics is the most beautiful branch of philosophy.");
    }

    @Test
    public void invalidSoftBreak1() throws Exception {
        assertIOException("CR must be followed by LF", "=\r\r");
    }

    @Test
    public void invalidSoftBreak2() throws Exception {
        assertIOException("CR must be followed by LF", "=\rn");
    }

    @Test
    public void truncatedEscape() throws Exception {
        assertIOException("truncated", "=1");
    }

    private static void assertEncoded(final String clearText, final String encoded) throws Exception {
        final byte[] expected = clearText.getBytes(StandardCharsets.US_ASCII);

        final ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        final byte[] encodedData = encoded.getBytes(StandardCharsets.US_ASCII);
        QuotedPrintableDecoder.decode(encodedData, out);
        final byte[] actual = out.toByteArray();

        assertArrayEquals(expected, actual);
    }

    private static void assertIOException(final String messageText, final String encoded)
            throws UnsupportedEncodingException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        final byte[] encodedData = encoded.getBytes(StandardCharsets.US_ASCII);
        try {
            QuotedPrintableDecoder.decode(encodedData, out);
            fail("Expected IOException");
        } catch (final IOException e) {
            final String em = e.getMessage();
            assertTrue(em.contains(messageText), "Expected to find " + messageText + " in '" + em + "'");
        }
    }

}
