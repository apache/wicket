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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.disk.DiskFileItemFactory;
import org.apache.commons.fileupload2.pub.FileSizeLimitExceededException;
import org.apache.commons.fileupload2.pub.FileUploadIOException;
import org.apache.commons.fileupload2.pub.SizeLimitExceededException;
import org.apache.commons.fileupload2.servlet.ServletFileUpload;
import org.apache.commons.fileupload2.util.Streams;
import org.junit.jupiter.api.Test;

/**
 * Unit test for items with varying sizes.
 */
public class SizesTest {

    /**
     * Runs a test with varying file sizes.
     */
    @Test
    public void testFileUpload()
            throws IOException, FileUploadException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int add = 16;
        int num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            final String header = "-----1234\r\n"
                + "Content-Disposition: form-data; name=\"field" + (num++) + "\"\r\n"
                + "\r\n";
            baos.write(header.getBytes(StandardCharsets.US_ASCII));
            for (int j = 0;  j < i;  j++) {
                baos.write((byte) j);
            }
            baos.write("\r\n".getBytes(StandardCharsets.US_ASCII));
        }
        baos.write("-----1234--\r\n".getBytes(StandardCharsets.US_ASCII));

        final List<FileItem> fileItems =
                Util.parseUpload(new ServletFileUpload(new DiskFileItemFactory()), baos.toByteArray());
        final Iterator<FileItem> fileIter = fileItems.iterator();
        add = 16;
        num = 0;
        for (int i = 0;  i < 16384;  i += add) {
            if (++add == 32) {
                add = 16;
            }
            final FileItem item = fileIter.next();
            assertEquals("field" + (num++), item.getFieldName());
            final byte[] bytes = item.get();
            assertEquals(i, bytes.length);
            for (int j = 0;  j < i;  j++) {
                assertEquals((byte) j, bytes[j]);
            }
        }
        assertTrue(!fileIter.hasNext());
    }

    /** Checks, whether limiting the file size works.
     */
    @Test
    public void testFileSizeLimit()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40);
        req = new MockHttpServletRequest(request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(30);
        req = new MockHttpServletRequest(request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileSizeLimitExceededException e) {
            assertEquals(30, e.getPermittedSize());
        }
    }

    /** Checks, whether a faked Content-Length header is detected.
     */
    @Test
    public void testFileSizeLimitWithFakedContentLength()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"foo.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        HttpServletRequest req = new MockHttpServletRequest(
                request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        List<FileItem> fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        FileItem item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(40);
        req = new MockHttpServletRequest(request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        fileItems = upload.parseRequest(req);
        assertEquals(1, fileItems.size());
        item = fileItems.get(0);
        assertEquals("This is the content of the file\n", new String(item.get()));

        // provided Content-Length is larger than the FileSizeMax -> handled by ctor
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(5);
        req = new MockHttpServletRequest(request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileSizeLimitExceededException e) {
            assertEquals(5, e.getPermittedSize());
        }

        // provided Content-Length is wrong, actual content is larger -> handled by LimitedInputStream
        upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(15);
        req = new MockHttpServletRequest(request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final FileSizeLimitExceededException e) {
            assertEquals(15, e.getPermittedSize());
        }
    }

    /** Checks, whether the maxSize works.
     */
    @Test
    public void testMaxSizeLimit()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        final ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(200);

        final MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        try {
            upload.parseRequest(req);
            fail("Expected exception.");
        } catch (final SizeLimitExceededException e) {
            assertEquals(200, e.getPermittedSize());
        }
    }

    @Test
    public void testMaxSizeLimitUnknownContentLength()
            throws IOException, FileUploadException {
        final String request =
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file1\"; filename=\"foo1.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "Content-Length: 10\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234\r\n" +
            "Content-Disposition: form-data; name=\"file2\"; filename=\"foo2.tab\"\r\n" +
            "Content-Type: text/whatever\r\n" +
            "\r\n" +
            "This is the content of the file\n" +
            "\r\n" +
            "-----1234--\r\n";

        final ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        upload.setFileSizeMax(-1);
        upload.setSizeMax(300);

        // the first item should be within the max size limit
        // set the read limit to 10 to simulate a "real" stream
        // otherwise the buffer would be immediately filled

        final MockHttpServletRequest req = new MockHttpServletRequest(
                request.getBytes(StandardCharsets.US_ASCII), Constants.CONTENT_TYPE);
        req.setContentLength(-1);
        req.setReadLimit(10);

        final FileItemIterator it = upload.getItemIterator(req);
        assertTrue(it.hasNext());

        FileItemStream item = it.next();
        assertFalse(item.isFormField());
        assertEquals("file1", item.getFieldName());
        assertEquals("foo1.tab", item.getName());

        {
            final InputStream stream = item.openStream();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Streams.copy(stream, baos, true);
        }

        // the second item is over the size max, thus we expect an error
        try {
            // the header is still within size max -> this shall still succeed
            assertTrue(it.hasNext());
        } catch (final SizeLimitExceededException e) {
            fail();
        }

        item = it.next();

        try {
            final InputStream stream = item.openStream();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Streams.copy(stream, baos, true);
            fail();
        } catch (final FileUploadIOException e) {
            // expected
        }
    }

}
