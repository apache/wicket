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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.fileupload2.MultipartStream}.
 */
public class MultipartStreamTest {

    static private final String BOUNDARY_TEXT = "myboundary";

    @Test
    public void testThreeParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final byte[] boundary = BOUNDARY_TEXT.getBytes();
        final int iBufSize =
                boundary.length + MultipartStream.BOUNDARY_PREFIX.length + 1;
        final MultipartStream ms = new MultipartStream(
                input,
                boundary,
                iBufSize,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

    @Test
    public void testSmallBuffer() {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final byte[] boundary = BOUNDARY_TEXT.getBytes();
        final int iBufSize = 1;
        assertThrows(IllegalArgumentException.class,
                () -> new MultipartStream(
                        input,
                        boundary,
                        iBufSize,
                        new MultipartStream.ProgressNotifier(null, contents.length)));
    }

    @Test
    public void testTwoParamConstructor() throws Exception {
        final String strData = "foobar";
        final byte[] contents = strData.getBytes();
        final InputStream input = new ByteArrayInputStream(contents);
        final byte[] boundary = BOUNDARY_TEXT.getBytes();
        final MultipartStream ms = new MultipartStream(
                input,
                boundary,
                new MultipartStream.ProgressNotifier(null, contents.length));
        assertNotNull(ms);
    }

}
