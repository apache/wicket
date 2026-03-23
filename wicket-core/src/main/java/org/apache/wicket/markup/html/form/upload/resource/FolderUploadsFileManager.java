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
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.lang.Args;

/**
 * Implementation of {@link IUploadsFileManager} that stores files in sub-folders of a given folder.
 * Sub-folders are named using the input field ID (and those are unique per application). Thus, there
 * is no possibility that uploads get mixed up.
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
     * Returns the path for a directory for use in path traversal checks. Uses {@code toRealPath()}
     * when the directory exists; otherwise falls back to {@code toAbsolutePath().normalize()} to
     * support validation when the directory has not yet been created.
     */
    private static Path getPathForComparison(java.io.File dir) throws IOException
    {
        try
        {
            return dir.toPath().toRealPath();
        }
        catch (IOException e)
        {
            return dir.toPath().toAbsolutePath().normalize();
        }
    }

    @Override
    public void save(FileUpload fileItem, String uploadFieldId)
    {
        try
        {
            Path baseFolderPath = getFolder().toPath().toRealPath();
            java.io.File uploadFieldFolder = new File(getFolder(), uploadFieldId).getCanonicalFile();
            if (!uploadFieldFolder.toPath().startsWith(baseFolderPath))
            {
                throw new SecurityException("Path traversal detected in uploadFieldId");
            }
            uploadFieldFolder.mkdirs();
            java.io.File target = new File(uploadFieldFolder, fileItem.getClientFileName()).getCanonicalFile();
            Path uploadFieldFolderPath = getPathForComparison(uploadFieldFolder);
            if (!target.toPath().startsWith(uploadFieldFolderPath))
            {
                throw new SecurityException("Path traversal detected in client filename");
            }
            IOUtils.copy(fileItem.getInputStream(), new FileOutputStream(target));
        }
        catch (IOException e)
        {
            throw new WicketRuntimeException(e);
        }
    }

    @Override
    public File getFile(String uploadFieldId, String clientFileName)
    {
        try
        {
            Path baseFolderPath = getFolder().toPath().toRealPath();
            java.io.File uploadFieldFolder = new File(getFolder(), uploadFieldId).getCanonicalFile();
            if (!uploadFieldFolder.toPath().startsWith(baseFolderPath))
            {
                throw new SecurityException("Path traversal detected in uploadFieldId");
            }
            java.io.File target = new File(uploadFieldFolder, clientFileName).getCanonicalFile();
            Path uploadFieldFolderPath = getPathForComparison(uploadFieldFolder);
            if (!target.toPath().startsWith(uploadFieldFolderPath))
            {
                throw new SecurityException("Path traversal detected in client filename");
            }
            return new File(target);
        }
        catch (IOException e)
        {
            throw new WicketRuntimeException(e);
        }
    }
}
