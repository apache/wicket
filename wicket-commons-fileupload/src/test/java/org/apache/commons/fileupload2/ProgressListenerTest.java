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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.fileupload2.servlet.ServletFileUpload;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link ProgressListener}.
 */
public class ProgressListenerTest {

    private static class ProgressListenerImpl implements ProgressListener {

        private final long expectedContentLength;

        private final int expectedItems;

        private Long bytesRead;

        private Integer items;

        ProgressListenerImpl(final long pContentLength, final int pItems) {
            expectedContentLength = pContentLength;
            expectedItems = pItems;
        }

        @Override
        public void update(final long pBytesRead, final long pContentLength, final int pItems) {
            assertTrue(pBytesRead >= 0  &&  pBytesRead <= expectedContentLength);
            assertTrue(pContentLength == -1  ||  pContentLength == expectedContentLength);
            assertTrue(pItems >= 0  &&  pItems <= expectedItems);

            assertTrue(bytesRead == null  ||  pBytesRead >= bytesRead.longValue());
            bytesRead = Long.valueOf(pBytesRead);
            assertTrue(items == null  ||  pItems >= items.intValue());
            items = Integer.valueOf(pItems);
        }

        void checkFinished() {
            assertEquals(expectedContentLength, bytesRead.longValue());
            assertEquals(expectedItems, items.intValue());
        }

    }

    /**
     * Parse a very long file upload by using a progress listener.
     */
    @Test
    public void testProgressListener() throws Exception {
        final int NUM_ITEMS = 512;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0;  i < NUM_ITEMS;  i++) {
            final String header = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field" + (i + 1) + "\"\r\n"
                + "\r\n";
            baos.write(header.getBytes(StandardCharsets.US_ASCII));
            for (int j = 0;  j < 16384 + i;  j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        }
        baos.write("-----1234--\r\n".getBytes(StandardCharsets.US_ASCII));
        final byte[] contents = baos.toByteArray();

        MockHttpServletRequest request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE);
        runTest(NUM_ITEMS, contents.length, request);
        request = new MockHttpServletRequest(contents, Constants.CONTENT_TYPE) {
            @Override
            public int getContentLength() {
                return -1;
            }
        };
        runTest(NUM_ITEMS, contents.length, request);
    }

    private void runTest(final int NUM_ITEMS, final long pContentLength, final MockHttpServletRequest request) throws FileUploadException, IOException {
        final ServletFileUpload upload = new ServletFileUpload();
        final ProgressListenerImpl listener = new ProgressListenerImpl(pContentLength, NUM_ITEMS);
        upload.setProgressListener(listener);
        final FileItemIterator iter = upload.getItemIterator(request);
        for (int i = 0;  i < NUM_ITEMS;  i++) {
            final FileItemStream stream = iter.next();
            final InputStream istream = stream.openStream();
            for (int j = 0;  j < 16384 + i;  j++) {
                /**
                 * This used to be
                 *     assertEquals((byte) j, (byte) istream.read());
                 * but this seems to trigger a bug in JRockit, so
                 * we express the same like this:
                 */
                final byte b1 = (byte) j;
                final byte b2 = (byte) istream.read();
                if (b1 != b2) {
                    fail("Expected " + b1 + ", got " + b2);
                }
            }
            assertEquals(-1, istream.read());
            istream.close();
        }
        assertTrue(!iter.hasNext());
        listener.checkFinished();
    }

}
