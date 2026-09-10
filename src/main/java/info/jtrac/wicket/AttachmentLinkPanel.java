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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.springframework.util.StringUtils;

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
        final String fileType = AttachmentUtils.guessFileType(attachment, getJtrac().getJtracHome());
        File initialFile = AttachmentUtils.getFile(attachment, getJtrac().getJtracHome());
        final boolean isText = fileType != null && fileType.startsWith("text");
        final boolean isImage = fileType != null && fileType.startsWith("image");
        final boolean isUtf8Text = isText && AttachmentUtils.isValidUtf8(initialFile);
        final boolean isViewable = isImage || isUtf8Text;
        final boolean openInNewWindow = openNewWindow() && isViewable;

        Link<Void> link = new Link<Void>("attachment") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                File file = AttachmentUtils.getFile(attachment, getJtrac().getJtracHome());
                if (file == null || !file.exists()) {
                    error("Attachment file not found: " + fileName);
                    return;
                }

                final boolean isTextFile = fileType != null && fileType.startsWith("text");
                final boolean isImageFile = fileType != null && fileType.startsWith("image");
                final boolean isUtf8 = isTextFile && AttachmentUtils.isValidUtf8(file);
                final boolean canViewInline = openInNewWindow && (isImageFile || isUtf8);

                final String contentType;
                if (isUtf8) {
                    contentType = fileType.toLowerCase().contains("charset") ? fileType : (fileType + "; charset=UTF-8");
                } else {
                    contentType = fileType != null ? fileType : "application/octet-stream";
                }

                IResourceStream resourceStream = new FileResourceStream(file) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getContentType() {
                        return contentType;
                    }
                };

                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(resourceStream, fileName) {
                    @Override
                    public void respond(IRequestCycle requestCycle) {
                        WebResponse r = (WebResponse) requestCycle.getResponse();
                        String disposition = canViewInline ? "inline" : "attachment";
                        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
                        r.setHeader("Content-Disposition", disposition + "; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
                        if (contentType != null) {
                            r.setHeader("Content-Type", contentType);
                        }
                        super.respond(requestCycle);
                    }
                };

                if (canViewInline) {
                    handler.setContentDisposition(ContentDisposition.INLINE);
                } else {
                    handler.setContentDisposition(ContentDisposition.ATTACHMENT);
                }

                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            }
        };

        if (openInNewWindow) {
            link.add(AttributeModifier.replace("target", "_blank"));
        }

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
