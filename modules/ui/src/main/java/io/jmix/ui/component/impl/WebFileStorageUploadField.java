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

import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.Messages;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.ConversionException;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.components.impl.JmixFileUploadWrapper;
import io.jmix.ui.export.ExportDisplay;
import io.jmix.ui.icon.IconResolver;
import io.jmix.ui.upload.TemporaryStorage;
import io.jmix.ui.widget.JmixFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jmix.ui.component.ComponentsHelper.getScreenContext;
import static io.jmix.ui.upload.FileUploadTypesHelper.convertToMIME;

public class WebFileStorageUploadField<T> extends WebAbstractUploadField<JmixFileUploadWrapper, T>
        implements FileStorageUploadField<T>, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(WebFileUploadField.class);

    protected TemporaryStorage temporaryStorage;
    protected FileStorage<T, Object> fileStorage;
    protected String fileStorageName;

    protected JmixFileUpload uploadButton;
    protected String fileName;
    protected FileStoragePutMode mode = FileStoragePutMode.MANUAL;

    protected UUID fileId;
    protected UUID tempFileId;

    /*
     * This flag is used only for MANUAL mode to register that file was uploaded with the upload button rather then
     * setValue calling or changed property in the datasource.
     */
    protected boolean internalValueChangedOnUpload = false;

    public WebFileStorageUploadField() {
        uploadButton = createComponent();
        component = createWrapper();
    }

    protected JmixFileUploadWrapper createWrapper() {
        return new JmixFileUploadWrapper(uploadButton) {
            @Override
            protected void onSetInternalValue(Object newValue) {
                internalValueChanged(newValue);
            }
        };
    }

    @Override
    public void afterPropertiesSet() {
        initUploadButton(uploadButton);

        initComponent();
        attachValueChangeListener(component);
    }

    @Override
    protected void valueBindingConnected(ValueSource<T> valueSource) {
        super.valueBindingConnected(valueSource);

        setShowFileName(true);
    }

    @Autowired
    public void setTemporaryStorage(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Autowired
    public void setExportDisplay(ExportDisplay exportDisplay) {
        this.exportDisplay = exportDisplay;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;

        component.setClearButtonCaption(messages.getMessage("FileUploadField.clearButtonCaption"));
        component.setFileNotSelectedMessage(messages.getMessage("FileUploadField.fileNotSelected"));
    }

    @Autowired
    public void setFileStorageLocator(FileStorageLocator fileStorageLocator) {
        String fileStorageName = getFileStorageName();
        if (StringUtils.isNotEmpty(fileStorageName)) {
            fileStorage = fileStorageLocator.get(fileStorageName);
        } else {
            fileStorage = fileStorageLocator.getDefault();
        }
        ValueSource<T> valueSource = getValueSource();
        if (valueSource != null && !fileStorage.getReferenceType().isAssignableFrom(valueSource.getType())) {
            throw new IllegalArgumentException(String.format(
                    "File storage %s is not applicable for the property of type %s",
                    fileStorage.getClass().getName(), valueSource.getType().getName()));
        }
    }

    protected void initComponent() {
        component.addFileNameClickListener(e -> {
            T value = getValue();
            if (value == null) {
                return;
            }

            switch (mode) {
                case MANUAL:
                    String name = getFileName();
                    String fileName = StringUtils.isEmpty(name) ? getFileNameByValue(value) : name;
                    exportDisplay.show(this::getFileContent, fileName);
                    break;
                case IMMEDIATE:
                    exportDisplay.show(this::getFileContent, getFileNameByValue(value));
                    break;
            }
        });
        component.setClearButtonListener(this::clearButtonClicked);
        component.setRequiredError(null);
    }

    protected void internalValueChanged(Object newValue) {
        fileName = newValue == null ? null : newValue.toString();

        if (!internalValueChangedOnUpload) {
            fileId = null;
            tempFileId = null;
        }
    }

    protected void clearButtonClicked(@SuppressWarnings("unused") Button.ClickEvent clickEvent) {
        BeforeValueClearEvent beforeValueClearEvent = new BeforeValueClearEvent(this);
        publish(BeforeValueClearEvent.class, beforeValueClearEvent);

        if (!beforeValueClearEvent.isClearPrevented()) {
            setValue(null);
            fileName = null;
        }

        AfterValueClearEvent afterValueClearEvent = new AfterValueClearEvent(this,
                !beforeValueClearEvent.isClearPrevented());
        publish(AfterValueClearEvent.class, afterValueClearEvent);
    }

    protected void saveFile(T reference) {
        switch (mode) {
            case MANUAL:
                internalValueChangedOnUpload = true;
                setValue(reference);
                internalValueChangedOnUpload = false;
                break;
            case IMMEDIATE:
                temporaryStorage.putFileIntoStorage(fileId, reference, fileStorage);
                setValue(reference);
                break;
        }
    }

    protected void initUploadButton(JmixFileUpload impl) {
        impl.setProgressWindowCaption(messages.getMessage("upload.uploadingProgressTitle"));
        impl.setUnableToUploadFileMessage(messages.getMessage("upload.unableToUploadFile"));
        impl.setCancelButtonCaption(messages.getMessage("upload.cancel"));
        impl.setCaption(messages.getMessage("upload.submit"));
        impl.setDropZonePrompt(messages.getMessage("upload.singleDropZonePrompt"));
        impl.setDescription(null);

        impl.setFileSizeLimit(getActualFileSizeLimit());

        impl.setReceiver(this::receiveUpload);

        impl.addStartedListener(event ->
                fireFileUploadStart(event.getFileName(), event.getContentLength())
        );

        impl.addFinishedListener(event ->
                fireFileUploadFinish(event.getFileName(), event.getContentLength())
        );

        impl.addSucceededListener(event -> {
            fileName = event.getFileName();
            fileId = tempFileId;

            saveFile(getReference());
            component.setFileNameButtonCaption(fileName);

            fireFileUploadSucceed(event.getFileName(), event.getContentLength());
        });

        impl.addFailedListener(event -> {
            try {
                temporaryStorage.deleteFile(tempFileId);
                tempFileId = null;
            } catch (Exception e) {
                if (e instanceof FileStorageException) {
                    FileStorageException fse = (FileStorageException) e;
                    if (fse.getType() != FileStorageException.Type.FILE_NOT_FOUND) {
                        log.warn(String.format("Could not remove temp file %s after broken uploading", tempFileId));
                    }
                }
                log.warn(String.format("Error while delete temp file %s", tempFileId));
            }

            fireFileUploadError(event.getFileName(), event.getContentLength(), event.getReason());
        });
        impl.addFileSizeLimitExceededListener(e -> {
            Notifications notifications = getScreenContext(this).getNotifications();

            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.formatMessage("upload.fileTooBig.message", e.getFileName(), getFileSizeLimitString()))
                    .show();
        });
        impl.addFileExtensionNotAllowedListener(e -> {
            Notifications notifications = getScreenContext(this).getNotifications();

            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.formatMessage("upload.fileIncorrectExtension.message", e.getFileName()))
                    .show();
        });
    }

    protected OutputStream receiveUpload(String fileName, String MIMEType) {
        try {
            TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();
            tempFileId = fileInfo.getId();
            File tmpFile = fileInfo.getFile();

            return new FileOutputStream(tmpFile);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to receive file '%s' of MIME type: %s", fileName, MIMEType), e);
        }
    }

    protected JmixFileUpload createComponent() {
        return new JmixFileUpload();
    }

    @Override
    protected String convertToPresentation(T modelValue) throws ConversionException {
        return modelValue == null ? null : getFileNameByValue(modelValue);
    }

    @Override
    public String getFileName() {
        if (fileName == null) {
            return null;
        }

        String[] strings = fileName.split("[/\\\\]");
        return strings[strings.length - 1];
    }

    protected String getFileNameByValue(T value) {
        return String.valueOf(fileStorage.getFileInfo(value));
    }

    @Override
    public Subscription addFileUploadSucceedListener(Consumer<FileUploadSucceedEvent> listener) {
        return getEventHub().subscribe(FileUploadSucceedEvent.class, listener);
    }

    @Override
    public void removeFileUploadSucceedListener(Consumer<FileUploadSucceedEvent> listener) {
        unsubscribe(FileUploadSucceedEvent.class, listener);
    }

    /**
     * @return File id for uploaded file in {@link TemporaryStorage}
     */
    @Override
    public UUID getFileId() {
        return fileId;
    }

    @Nullable
    @Override
    public T getReference() {
        if (fileId != null) {
            return fileStorage.createReference(fileName);
        } else {
            return null;
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
            uploadButton.setAccept(convertToMIME(accept));
        }
    }

    @Override
    public void setDropZone(DropZone dropZone) {
        super.setDropZone(dropZone);

        if (dropZone == null) {
            uploadButton.setDropZone(null);
        } else {
            io.jmix.ui.component.Component target = dropZone.getTarget();
            if (target instanceof Window.Wrapper) {
                target = ((Window.Wrapper) target).getWrappedWindow();
            }

            Component vComponent = target.unwrapComposition(Component.class);
            uploadButton.setDropZone(vComponent);
        }
    }

    @Override
    public void setPasteZone(ComponentContainer pasteZone) {
        super.setPasteZone(pasteZone);

        uploadButton.setPasteZone(pasteZone != null ? pasteZone.unwrapComposition(Component.class) : null);
    }

    @Override
    public void setDropZonePrompt(String dropZonePrompt) {
        super.setDropZonePrompt(dropZonePrompt);

        uploadButton.setDropZonePrompt(dropZonePrompt);
    }

    protected void fireFileUploadStart(String fileName, long contentLength) {
        publish(FileUploadStartEvent.class, new FileUploadStartEvent(this, fileName, contentLength));
    }

    protected void fireFileUploadFinish(String fileName, long contentLength) {
        publish(FileUploadFinishEvent.class, new FileUploadFinishEvent(this, fileName, contentLength));
    }

    protected void fireFileUploadError(String fileName, long contentLength, Exception cause) {
        publish(FileUploadErrorEvent.class, new FileUploadErrorEvent(this, fileName, contentLength, cause));
    }

    protected void fireFileUploadSucceed(String fileName, long contentLength) {
        publish(FileUploadSucceedEvent.class, new FileUploadSucceedEvent(this, fileName, contentLength));
    }

    @Override
    public InputStream getFileContent() {
        if (contentProvider != null) {
            return contentProvider.get();
        }

        T reference = getValue();
        switch (mode) {
            case MANUAL:
                if (fileId == null) {
                    return fileStorage.openStream(reference);
                }

                File file = temporaryStorage.getFile(fileId);
                if (file != null) {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        log.error("Unable to get content of {}", file, e);
                    }
                    return null;
                }

                try {
                    if (fileStorage.fileExists(reference)) {
                        return fileStorage.openStream(reference);
                    }
                } catch (FileStorageException e) {
                    log.error("Unable to get content of {}", reference, e);
                    return null;
                }
                break;
            case IMMEDIATE:
                if (reference != null) {
                    return fileStorage.openStream(reference);
                }
        }
        return null;
    }

    @Override
    public void setFileStorageName(String fileStorageName) {
        this.fileStorageName = fileStorageName;
    }

    @Override
    public String getFileStorageName() {
        return fileStorageName;
    }

    @Override
    public void setContentProvider(Supplier<InputStream> contentProvider) {
        this.contentProvider = contentProvider;
    }

    @Override
    public Supplier<InputStream> getContentProvider() {
        return contentProvider;
    }

    @Override
    public Subscription addFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        return getEventHub().subscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public void removeFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        unsubscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public Subscription addFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        return getEventHub().subscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public void removeFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        unsubscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public Subscription addFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        return getEventHub().subscribe(FileUploadErrorEvent.class, listener);
    }

    @Override
    public void removeFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        unsubscribe(FileUploadErrorEvent.class, listener);
    }

    @Override
    public void setFileSizeLimit(long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;

        uploadButton.setFileSizeLimit(fileSizeLimit);
    }

    @Override
    public FileStoragePutMode getMode() {
        return mode;
    }

    @Override
    public void setMode(FileStoragePutMode mode) {
        this.mode = mode;
    }

    @Override
    public boolean isShowFileName() {
        return component.isShowFileName();
    }

    @Override
    public void setShowFileName(boolean showFileName) {
        component.setShowFileName(showFileName);
        if (showFileName) {
            component.setFileNameButtonCaption(fileName);
        }
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
        uploadButton.setPermittedExtensions(this.permittedExtensions);
    }

    @Override
    public void setShowClearButton(boolean showClearButton) {
        component.setShowClearButton(showClearButton);
    }

    @Override
    public boolean isShowClearButton() {
        return component.isShowClearButton();
    }

    @Override
    public void setClearButtonCaption(String caption) {
        component.setClearButtonCaption(caption);
    }

    @Override
    public String getClearButtonCaption() {
        return component.getClearButtonCaption();
    }

    @Override
    public void setClearButtonIcon(String icon) {
        if (icon != null) {
            IconResolver iconResolver = beanLocator.get(IconResolver.NAME);
            Resource iconResource = iconResolver.getIconResource(icon);
            component.setClearButtonIcon(iconResource);
        } else {
            component.setClearButtonIcon(null);
        }
    }

    @Override
    public String getClearButtonIcon() {
        return component.getClearButtonIcon();
    }

    @Override
    public void setClearButtonDescription(String description) {
        component.setClearButtonDescription(description);
    }

    @Override
    public String getClearButtonDescription() {
        return component.getClearButtonDescription();
    }

    @Override
    public Subscription addBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener) {
        return getEventHub().subscribe(BeforeValueClearEvent.class, listener);
    }

    @Override
    public void removeBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener) {
        unsubscribe(BeforeValueClearEvent.class, listener);
    }

    @Override
    public Subscription addAfterValueClearListener(Consumer<AfterValueClearEvent> listener) {
        return getEventHub().subscribe(AfterValueClearEvent.class, listener);
    }

    @Override
    public void removeAfterValueClearListener(Consumer<AfterValueClearEvent> listener) {
        unsubscribe(AfterValueClearEvent.class, listener);
    }

    @Override
    public void setUploadButtonCaption(String caption) {
        component.setUploadButtonCaption(caption);
    }

    @Override
    public String getUploadButtonCaption() {
        return component.getUploadButtonCaption();
    }

    @Override
    public void setUploadButtonIcon(String icon) {
        if (!StringUtils.isEmpty(icon)) {
            IconResolver iconResolver = beanLocator.get(IconResolver.class);
            component.setUploadButtonIcon(iconResolver.getIconResource(icon));
        } else {
            component.setUploadButtonIcon(null);
        }
    }

    @Override
    public String getUploadButtonIcon() {
        return component.getUploadButtonIcon();
    }

    @Override
    public void setUploadButtonDescription(String description) {
        component.setUploadButtonDescription(description);
    }

    @Override
    public String getUploadButtonDescription() {
        return component.getUploadButtonDescription();
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

    @Override
    public void commit() {
        super.commit();
    }

    @Override
    public void discard() {
        super.discard();
    }

    @Override
    public boolean isBuffered() {
        return super.isBuffered();
    }

    @Override
    public void setBuffered(boolean buffered) {
        super.setBuffered(buffered);
    }

    @Override
    public boolean isModified() {
        return super.isModified();
    }
}