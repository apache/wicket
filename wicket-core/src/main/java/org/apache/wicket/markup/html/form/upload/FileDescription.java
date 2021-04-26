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
package org.apache.wicket.markup.html.form.upload;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import com.github.openjson.JSONObject;

/**
 * Description of file properties as in browser client side.
 */
public class FileDescription implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final long fileSize;
    private final Date lastModified;
    private final String mimeType;

   public FileDescription(JSONObject jsonObject) {
        this(jsonObject.getString("fileName"), jsonObject.getLong("fileSize"),
                jsonObject.getLong("lastModified"), jsonObject.getString("mimeType"));
    }

    public FileDescription(String fileName, long fileSize, long lastModified, String mimeType) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.lastModified = new Date(lastModified);
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescription that = (FileDescription) o;
        return fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
