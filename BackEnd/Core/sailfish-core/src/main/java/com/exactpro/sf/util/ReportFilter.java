/******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactpro.sf.util;

import java.io.File;
import java.io.FileFilter;

import com.exactpro.sf.scriptrunner.ZipReport;

import static com.exactpro.sf.storage.impl.DefaultTestScriptStorage.ROOT_JSON_REPORT_FILE;

import org.apache.commons.io.FilenameUtils;

public class ReportFilter implements FileFilter {
    private static final ReportFilter instance = new ReportFilter();

    public static ReportFilter getInstance() {
        return instance;
    }

    @Override
    public boolean accept(File file) {

        return !file.isDirectory() && (
                // checks that zip file has no json report file near it to avoid duplicating reports
                (FilenameUtils.isExtension(file.getName(), ZipReport.ZIP_EXTENSION) && !file.getParentFile().toPath().resolve(ROOT_JSON_REPORT_FILE).toFile().exists())
                || (file.toPath().endsWith(ROOT_JSON_REPORT_FILE))
        );
    }
}
