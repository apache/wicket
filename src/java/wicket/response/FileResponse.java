/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.response;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import wicket.Response;
import wicket.util.file.File;

/**
 * Writes response to file
 * @author Jonathan Locke
 */
public final class FileResponse extends Response
{ // TODO finalize javadoc
    // Writer to write to
    private final PrintWriter out;

    /**
     * Constructor
     * @param file The file to write to
     * @throws IOException
     */
    public FileResponse(final File file) throws IOException
    {
        this.out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
    }

    /**
     * @see wicket.Response#write(java.lang.String)
     */
    public void write(final String string)
    {
        out.print(string);
    }

    /**
     * @see wicket.Response#close()
     */
    public void close()
    {
        out.close();
    }
}

///////////////////////////////// End of File /////////////////////////////////
