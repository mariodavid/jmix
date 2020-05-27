/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.component.impl;

import com.vaadin.ui.Component;
import io.jmix.core.FileStorageException;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.Notifications;
import io.jmix.ui.Notifications.NotificationType;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.FileMultiUploadField;
import io.jmix.ui.component.Window;
import io.jmix.ui.upload.TemporaryStorage;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.ui.component.ComponentsHelper.getScreenContext;
import static io.jmix.ui.upload.FileUploadTypesHelper.convertToMIME;

public class WebFileMultiUploadField extends WebAbstractUploadComponent<JmixFileUpload>
        implements FileMultiUploadField, InitializingBean {

    protected final Map<UUID, String> files = new LinkedHashMap<>();

    protected TemporaryStorage temporaryStorage;
    protected UUID tempFileId;
    protected String accept;

    public WebFileMultiUploadField() {
        component = createComponent();
    }

    @Autowired
    public void setTemporaryStorage(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixFileUpload impl) {
        impl.setMultiSelect(true);

        Messages messages = beanLocator.get(Messages.NAME);
        impl.setProgressWindowCaption(messages.getMessage("upload.uploadingProgressTitle"));
        impl.setUnableToUploadFileMessage(messages.getMessage("upload.unableToUploadFile"));
        impl.setCancelButtonCaption(messages.getMessage("upload.cancel"));
        impl.setCaption(messages.getMessage("upload.submit"));
        impl.setDropZonePrompt(messages.getMessage("upload.dropZonePrompt"));
        impl.setDescription(null);

        int maxUploadSizeMb = beanLocator.get(UiProperties.class).getMaxUploadSizeMb();
        int maxSizeBytes = maxUploadSizeMb * BYTES_IN_MEGABYTE;

        impl.setFileSizeLimit(maxSizeBytes);

        impl.setReceiver((fileName, MIMEType) -> {
            FileOutputStream outputStream;
            try {
                TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();
                tempFileId = fileInfo.getId();
                File tmpFile = fileInfo.getFile();
                outputStream = new FileOutputStream(tmpFile);
            } catch (Exception e) {
                throw new RuntimeException("Unable to receive file", e);
            }
            return outputStream;
        });

        impl.addStartedListener(event -> fireFileUploadStart(event.getFileName(), event.getContentLength()));

        impl.addQueueUploadFinishedListener(event -> fireQueueUploadComplete());

        impl.addSucceededListener(event -> {
            files.put(tempFileId, event.getFileName());

            fireFileUploadFinish(event.getFileName(), event.getContentLength());
        });
        impl.addFailedListener(event -> {
            try {
                // close and remove temp file
                temporaryStorage.deleteFile(tempFileId);
                tempFileId = null;
            } catch (Exception e) {
                if (e instanceof FileStorageException) {
                    FileStorageException fse = (FileStorageException) e;
                    if (fse.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                        LoggerFactory.getLogger(WebFileMultiUploadField.class)
                                .warn("Could not remove temp file {} after broken uploading", tempFileId);
                    }
                }
                LoggerFactory.getLogger(WebFileMultiUploadField.class)
                        .warn("Error while delete temp file {}", tempFileId);
            }

            fireFileUploadError(event.getFileName(), event.getContentLength(), event.getReason());
        });
        impl.addFileSizeLimitExceededListener(e -> {
            Notifications notifications = getScreenContext(this).getNotifications();

            notifications.create(NotificationType.WARNING)
                    .withCaption(
                            messages.formatMessage("multiupload.filesizeLimitExceed",
                                    e.getFileName(), getFileSizeLimitString())
                    )
                    .show();
        });
        impl.addFileExtensionNotAllowedListener(e -> {
            Notifications notifications = getScreenContext(this).getNotifications();

            notifications.create(NotificationType.WARNING)
                    .withCaption(messages.formatMessage("upload.fileIncorrectExtension.message", e.getFileName()))
                    .show();
        });
    }

    protected JmixFileUpload createComponent() {
        return new JmixFileUpload();
    }

    /**
     * Get uploads map
     *
     * @return Map (UUID - Id of file in Temporary storage, String - FileName )
     */
    @Override
    public Map<UUID, String> getUploadsMap() {
        return Collections.unmodifiableMap(files);
    }

    @Override
    public void clearUploads() {
        files.clear();
    }

    @Override
    public Subscription addQueueUploadCompleteListener(Consumer<QueueUploadCompleteEvent> listener) {
        return getEventHub().subscribe(QueueUploadCompleteEvent.class, listener);
    }

    @Override
    public void removeQueueUploadCompleteListener(Consumer<QueueUploadCompleteEvent> listener) {
        unsubscribe(QueueUploadCompleteEvent.class, listener);
    }

    @Override
    public void setIcon(String icon) {
        this.icon = icon;

        if (!StringUtils.isEmpty(icon)) {
            component.setIcon(getIconResource(icon));
        } else {
            component.setIcon(null);
        }
    }

    @Override
    public String getAccept() {
        return accept;
    }

    @Override
    public void setAccept(String accept) {
        if (!Objects.equals(accept, getAccept())) {
            this.accept = accept;
            component.setAccept(convertToMIME(accept));
        }
    }

    @Override
    public void setDropZone(DropZone dropZone) {
        super.setDropZone(dropZone);

        if (dropZone == null) {
            component.setDropZone(null);
        } else {
            io.jmix.ui.component.Component target = dropZone.getTarget();
            if (target instanceof Window.Wrapper) {
                target = ((Window.Wrapper) target).getWrappedWindow();
            }

            Component vComponent = target.unwrapComposition(Component.class);
            this.component.setDropZone(vComponent);
        }
    }

    @Override
    public void setPasteZone(ComponentContainer pasteZone) {
        super.setPasteZone(pasteZone);

        if (pasteZone == null) {
            component.setPasteZone(null);
        } else {
            Component vComponent = pasteZone.unwrapComposition(Component.class);
            component.setPasteZone(vComponent);
        }
    }

    @Override
    public void setDropZonePrompt(String dropZonePrompt) {
        super.setDropZonePrompt(dropZonePrompt);

        component.setDropZonePrompt(dropZonePrompt);
    }

    protected void fireFileUploadStart(String fileName, long contentLength) {
        FileUploadStartEvent event = new FileUploadStartEvent(this, fileName, contentLength);
        publish(FileUploadStartEvent.class, event);
    }

    protected void fireFileUploadFinish(String fileName, long contentLength) {
        FileUploadFinishEvent event = new FileUploadFinishEvent(this, fileName, contentLength);
        publish(FileUploadFinishEvent.class, event);
    }

    protected void fireFileUploadError(String fileName, long contentLength, Exception cause) {
        FileUploadErrorEvent event = new FileUploadErrorEvent(this, fileName, contentLength, cause);
        publish(FileUploadErrorEvent.class, event);
    }

    protected void fireQueueUploadComplete() {
        QueueUploadCompleteEvent event = new QueueUploadCompleteEvent(this);
        publish(QueueUploadCompleteEvent.class, event);
    }

    @Override
    public void setFileSizeLimit(long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;

        this.component.setFileSizeLimit(fileSizeLimit);
    }

    @Override
    public void setPermittedExtensions(Set<String> permittedExtensions) {
        if (permittedExtensions != null) {
            this.permittedExtensions = permittedExtensions.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
        } else {
            this.permittedExtensions = null;
        }

        this.component.setPermittedExtensions(this.permittedExtensions);
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }
}