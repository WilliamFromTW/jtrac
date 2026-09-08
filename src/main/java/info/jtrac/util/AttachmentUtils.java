/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.util;

import info.jtrac.domain.Attachment;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utils that deal with Attachments, upload / download and file name String manipulation
 */

public class AttachmentUtils {    

    public static String cleanFileName(String path) {
        // the client browser could be on Unix or Windows, we don't know
        int index = path.lastIndexOf('/');
        if (index == -1) {
            index = path.lastIndexOf('\\');
        }
        return (index != -1 ? path.substring(index + 1) : path);
    }   

    public static File getFile (Attachment attachment, String jtracHome) {
        return new File(jtracHome + "/attachments/" + attachment.getFilePrefix() + "_" + attachment.getFileName());
    }

	public static String guessFileType (Attachment attachment, String jtracHome) {
		String fileType = null;
		try {
			File file = getFile(attachment, jtracHome);
			if (file.exists()) {
				fileType = Files.probeContentType(file.toPath());
			}
		} catch (Exception ignored) { }
		if (fileType == null && attachment != null && attachment.getFileName() != null) {
			String name = attachment.getFileName().toLowerCase();
			if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
				fileType = "image/jpeg";
			} else if (name.endsWith(".png")) {
				fileType = "image/png";
			} else if (name.endsWith(".gif")) {
				fileType = "image/gif";
			} else if (name.endsWith(".txt") || name.endsWith(".log")) {
				fileType = "text/plain";
			} else if (name.endsWith(".pdf")) {
				fileType = "application/pdf";
			} else if (name.endsWith(".zip")) {
				fileType = "application/zip";
			}
		}
		return fileType != null ? fileType : "application/octet-stream";
	}
}
