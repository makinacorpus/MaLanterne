/*
 * Copyright (c) 2015, Makina Corpus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.makina.osmnav;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class helper about Unit tests.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TestHelper {

    /**
     * Reads the contents of a file as {@code InputStream}.
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be {@code null}
     *
     * @return the file contents as {@code InputStream}.
     */
    @Nullable
    public static InputStream getFixtureAsStream(@NonNull final String name) {
        return TestHelper.class.getClassLoader()
                               .getResourceAsStream("fixtures/" + name);
    }

    /**
     * Reads the contents of a file into a {@code String}.
     * <p/>
     * The file is always closed.
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be {@code null}
     *
     * @return the file contents, never {@code null}
     */
    @NonNull
    public static String getFixtureAsString(@NonNull final String name) {
        final StringBuilder stringBuilder = new StringBuilder();

        final InputStream inputStream = getFixtureAsStream(name);

        if (inputStream == null) {
            return stringBuilder.toString();
        }

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line = bufferedReader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
                line = bufferedReader.readLine();
            }
        }
        catch (IOException ignored) {
        }
        finally {
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {
            }
        }

        return stringBuilder.toString();
    }
}
