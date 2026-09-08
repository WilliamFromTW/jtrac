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

package info.jtrac.wicket;

import info.jtrac.domain.Attachment;
import info.jtrac.util.AttachmentUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.util.StringUtils;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.io.Streams;

/**
 * link for downloading an attachment
 */
public class AttachmentLinkPanel extends BasePanel {

    public AttachmentLinkPanel (String id, final Attachment attachment) {
        super(id);

        if (attachment == null) {
            add(new Label("attachment", ""));
            setVisible(false);
            return;
        }

        final String fileName = attachment.getFileName();

        Link link = new Link("attachment") {
            // adapted from wicket.markup.html.link.DownloadLink
            // with the difference that the File is instantiated only after onClick
			@Override
            public void onClick() {
                getRequestCycle().scheduleRequestHandlerAfterCurrent(new IRequestHandler() {

					@Override
                    public void detach (IRequestCycle requestCycle) { }

					@Override
                    public void respond (IRequestCycle requestCycle) {
                        WebResponse r = (WebResponse) requestCycle.getResponse();
						String fileType = AttachmentUtils.guessFileType(attachment, getJtrac().getJtracHome());
						
						// https://stackoverflow.com/questions/30307406/wicket-webresponse-attachment-header-with-utf-8-charackers
						String encodedFileName = fileName;
						try {
							encodedFileName = URLEncoder.encode(attachment.getFileName(), "UTF-8");
						} catch (UnsupportedEncodingException ex) { }

						if (openNewWindow()) {
							if (fileType != null && (fileType.startsWith("image") || fileType.startsWith("text"))) {
								r.setHeader("Content-Disposition", "inline; filename=\""+fileName+"\"; filename*=UTF-8''"+encodedFileName);
								//r.setInlineHeader(fileName);
								// not available in Wicket 1.3.7
							} else {
								r.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"; filename*=UTF-8''"+encodedFileName);
							}
						} else {
							r.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"; filename*=UTF-8''"+encodedFileName);
						}
                        try {
                            File file = AttachmentUtils.getFile(attachment, getJtrac().getJtracHome());
                            InputStream is = new FileInputStream(file);
                            try {
                                Streams.copy(is, r.getOutputStream());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

			@Override
			public void renderHead (IHeaderResponse response) {
				if (openNewWindow()) {
					response.render(CssHeaderItem.forCSS("#oldStyle { display: none; } #newStyle { display: inline}", "attachmentStyle"));
				} else {
					response.render(CssHeaderItem.forCSS("#oldStyle { display: inline; } #newStyle { display: none}", "attachmentStyle"));
				}
			}
        };

        link.add(new Label("fileName", fileName));
        add(link);
    }

	protected boolean openNewWindow() {
		String openNewWindow = getJtrac().loadConfig("attachments.openNewWindow");
		if (StringUtils.hasText(openNewWindow)) {
			return openNewWindow.trim().equalsIgnoreCase("true");
		} else {
			return true;
		}
	}
}
