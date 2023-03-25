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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import org.apache.commons.fileupload2.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Serialization Unit tests for
 *  {@link org.apache.commons.fileupload2.disk.DiskFileItem}.
 */
public class DiskFileItemSerializeTest {

    // Use a private repo to catch any files left over by tests
    private static final File REPO = new File(System.getProperty("java.io.tmpdir"), "diskfileitemrepo");

    @BeforeEach
    public void setUp() throws Exception {
        if (REPO.exists()) {
            FileUtils.deleteDirectory(REPO);
        }
        FileUtils.forceMkdir(REPO);
    }

    @AfterEach
    public void tearDown() throws IOException {
        for(final File file : FileUtils.listFiles(REPO, null, true)) {
            System.out.println("Found leftover file " + file);
        }
        FileUtils.deleteDirectory(REPO);
    }

    /**
     * Content type for regular form items.
     */
    private static final String TEXT_CONTENT_TYPE = "text/plain";

    /**
     * Very low threshold for testing memory versus disk options.
     */
    private static final int THRESHOLD = 16;

    /**
     * Helper method to test creation of a field when a repository is used.
     */
    public void testInMemoryObject(final byte[] testFieldValueBytes, final File repository) {
        final FileItem item = createFileItem(testFieldValueBytes, repository);

        // Check state is as expected
        assertTrue(item.isInMemory(), "Initial: in memory");
        assertEquals(item.getSize(), testFieldValueBytes.length, "Initial: size");
        try {
            compareBytes("Initial", item.get(), testFieldValueBytes);
        } catch (UncheckedIOException e) {
            fail("Unexpected IOException", e);
        }
        testWritingToFile(item, testFieldValueBytes);
        item.delete();
    }

    /**
     * Helper method to test writing item contents to a file.
     */
    public void testWritingToFile(final FileItem item, final byte[] testFieldValueBytes) {
        try {
            final File temp = File.createTempFile("fileupload", null);
            // Note that the file exists and is initially empty;
            // write() must be able to handle that.
            item.write(temp);
            compareBytes("Initial", FileUtils.readFileToByteArray(temp), testFieldValueBytes);
        } catch (Exception e) {
            fail("Unexpected Exception", e);
        }
    }

    /**
     * Helper method to test creation of a field.
     */
    private void testInMemoryObject(final byte[] testFieldValueBytes) {
        testInMemoryObject(testFieldValueBytes, REPO);
    }

    /**
     * Test creation of a field for which the amount of data falls below the
     * configured threshold.
     */
    @Test
    public void testBelowThreshold() {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD - 1);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Test creation of a field for which the amount of data equals the
     * configured threshold.
     */
    @Test
    public void testThreshold() {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        testInMemoryObject(testFieldValueBytes);
    }

    /**
     * Test creation of a field for which the amount of data falls above the
     * configured threshold.
     */
    @Test
    public void testAboveThreshold() {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD + 1);
        final FileItem item = createFileItem(testFieldValueBytes);

        // Check state is as expected
        assertFalse(item.isInMemory(), "Initial: in memory");
        assertEquals(item.getSize(), testFieldValueBytes.length, "Initial: size");
        try {
            compareBytes("Initial", item.get(), testFieldValueBytes);
        } catch (UncheckedIOException e) {
            fail("Unexpected IOException", e);
        }

        testWritingToFile(item, testFieldValueBytes);
        item.delete();
    }

    /**
     * Test serialization and deserialization when repository is not null.
     */
    @Test
    public void testValidRepository() {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        testInMemoryObject(testFieldValueBytes, REPO);
    }

    /**
     * Test deserialization fails when repository is not valid.
     */
    @Test
    public void testInvalidRepository() {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        final File repository = new File(System.getProperty("java.io.tmpdir"), "file");
        final FileItem item = createFileItem(testFieldValueBytes, repository);
        assertThrows(IOException.class, () -> deserialize(serialize(item)));
    }

    /**
     * Test deserialization fails when repository contains a null character.
     */
    @Test
    public void testInvalidRepositoryWithNullChar() {
        // Create the FileItem
        final byte[] testFieldValueBytes = createContentBytes(THRESHOLD);
        final File repository = new File(System.getProperty("java.io.tmpdir"), "\0");
        final FileItem item = createFileItem(testFieldValueBytes, repository);
        assertThrows(IOException.class, () -> deserialize(serialize(item)));
    }

    /**
     * Compare content bytes.
     */
    private void compareBytes(final String text, final byte[] origBytes, final byte[] newBytes) {
        assertNotNull(origBytes, "origBytes must not be null");
        assertNotNull(newBytes, "newBytes must not be null");
        assertEquals(origBytes.length, newBytes.length, text + " byte[] length");
        for (int i = 0; i < origBytes.length; i++) {
            assertEquals(origBytes[i], newBytes[i], text + " byte[" + i + "]");
        }
    }

    /**
     * Create content bytes of a specified size.
     */
    private byte[] createContentBytes(final int size) {
        final StringBuilder buffer = new StringBuilder(size);
        byte count = 0;
        for (int i = 0; i < size; i++) {
            buffer.append(count + "");
            count++;
            if (count > 9) {
                count = 0;
            }
        }
        return buffer.toString().getBytes();
    }

    /**
     * Create a FileItem with the specfied content bytes and repository.
     */
    private FileItem createFileItem(final byte[] contentBytes, final File repository) {
        final FileItemFactory factory = new DiskFileItemFactory(THRESHOLD, repository);
        final String textFieldName = "textField";

        final FileItem item = factory.createItem(
                textFieldName,
                TEXT_CONTENT_TYPE,
                true,
                "My File Name"
        );
        try {
            final OutputStream os = item.getOutputStream();
            os.write(contentBytes);
            os.close();
        } catch (final IOException e) {
            fail("Unexpected IOException" + e);
        }

        return item;

    }

    /**
     * Create a FileItem with the specfied content bytes.
     */
    private FileItem createFileItem(final byte[] contentBytes) {
        return createFileItem(contentBytes, REPO);
    }

    /**
     * Do serialization
     */
    private ByteArrayOutputStream serialize(final Object target) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(target);
        oos.flush();
        oos.close();
        return baos;
    }

    /**
     * Do deserialization
     */
    private Object deserialize(final ByteArrayOutputStream baos) throws Exception {
        final ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object result = ois.readObject();
        bais.close();

        return result;
    }
}
