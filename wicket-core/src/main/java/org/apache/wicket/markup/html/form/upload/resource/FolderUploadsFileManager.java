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
package org.apache.wicket.markup.html.form.upload.resource;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;

/**
 * Implementation of {@link IUploadsFileManager} that stores files in sub-folders of a given folder.
 * Sub-folders are named using the input field ID (and those are unique per application). Thus, there
 * is no possibility that uploads get mixed up. In its sub-folder, each file gets a random name of its
 * own, so files of the same client file name do not replace each other.
 */
public class FolderUploadsFileManager implements IUploadsFileManager
{

    private final File folder;

    public FolderUploadsFileManager(File folder)
    {
        Args.notNull(folder, "folder");
        if (!folder.exists())
        {
            try
            {
                Files.createDirectories(folder.toPath());
            }
            catch (IOException e)
            {
                throw new WicketRuntimeException(e);
            }
        }
        else if (folder.exists() && !folder.isDirectory())
        {
            throw new IllegalArgumentException("Not a folder : " + folder.getAbsolutePath());
        }
        this.folder = folder;
    }

    public File getFolder() {
        return folder;
    }

    /**
     * Returns the canonical path for a directory for use in path traversal checks.
     * Uses {@code toRealPath()} when the directory exists; falls back to
     * {@code toAbsolutePath().normalize()} only when the directory does not yet exist
     * (e.g. an upload sub-folder that will be created during {@link #save}).
     * Other {@link IOException} subtypes (e.g. permission errors) are intentionally
     * re-thrown so they are not silently swallowed.
     */
    private static Path getPathForComparison(java.io.File dir) throws IOException
    {
        try
        {
            return dir.toPath().toRealPath();
        }
        catch (NoSuchFileException e)
        {
            return dir.toPath().toAbsolutePath().normalize();
        }
    }

    /**
     * Validates {@code uploadFieldId} and {@code fileName} against the base folder to
     * prevent path traversal, and returns the fully resolved, canonical target file.
     *
     * @throws SecurityException if either component would escape the base folder
     */
    private java.io.File resolveTargetFile(String uploadFieldId, String fileName)
            throws IOException
    {
        Path baseFolderPath = getFolder().toPath().toRealPath();
        java.io.File uploadFieldFolder = new File(getFolder(), uploadFieldId).getCanonicalFile();
        if (!uploadFieldFolder.toPath().startsWith(baseFolderPath))
        {
            throw new SecurityException("Path traversal detected in uploadFieldId");
        }
        java.io.File target = new File(uploadFieldFolder, fileName).getCanonicalFile();
        Path uploadFieldFolderPath = getPathForComparison(uploadFieldFolder);
        if (!target.toPath().startsWith(uploadFieldFolderPath))
        {
            throw new SecurityException("Path traversal detected in file name");
        }
        return target;
    }

    @Override
    public String store(FileUpload fileItem, String uploadFieldId)
    {
        String fileName = UUID.randomUUID().toString();
        write(fileItem, uploadFieldId, fileName);
        return fileName;
    }

    /**
     * @deprecated files saved under their client file name replace each other when they share it.
     *             Use {@link #store(FileUpload, String)} instead.
     */
    @Deprecated(since = "11.0.0", forRemoval = true)
    @SuppressWarnings("removal")
    @Override
    public void save(FileUpload fileItem, String uploadFieldId)
    {
        write(fileItem, uploadFieldId, fileItem.getClientFileName());
    }

    private void write(FileUpload fileItem, String uploadFieldId, String fileName)
    {
        try
        {
            java.io.File target = resolveTargetFile(uploadFieldId, fileName);
            Files.createDirectories(target.toPath().getParent());
            try (InputStream in = fileItem.getInputStream();
                 FileOutputStream out = new FileOutputStream(target))
            {
                IOUtils.copy(in, out);
            }
        }
        catch (IOException e)
        {
            throw new WicketRuntimeException(e);
        }
    }

    @Override
    public File getFile(String uploadFieldId, String fileName)
    {
        try
        {
            return new File(resolveTargetFile(uploadFieldId, fileName));
        }
        catch (IOException e)
        {
            throw new WicketRuntimeException(e);
        }
    }
}
