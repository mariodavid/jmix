/*
 * Copyright 2020 Haulmont.
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
 */

package io.jmix.ui.widget.client.richtextarea;

import com.google.gwt.dev.util.HttpHeaders;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.MetaElement;
import com.google.gwt.user.client.ui.RichTextArea;
import com.vaadin.client.ui.VRichTextArea;

import java.util.Map;

public class JmixRichTextAreaWidget extends VRichTextArea {

    protected AfterAttachedValueSupplier valueSupplier;

    protected int tabIndex = 0;

    public interface AfterAttachedValueSupplier {
        String getValue();
    }

    public JmixRichTextAreaWidget() {
        super();

        setContentCharset();

        rta.addAttachHandler(event -> {
            if (event.isAttached()) {
                // There are cases when 'ReachTextArea' is not attached but value has already set to 'HTML'
                // that is also not attached. When 'ReachTextArea' is attached we should set value again.
                if (valueSupplier != null) {
                    setValue(valueSupplier.getValue());
                }
            }
        });

        getElement().setTabIndex(tabIndex);
    }

    public void setValueSupplier(AfterAttachedValueSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;

        getElement().setTabIndex(tabIndex);
    }

    protected void setContentCharset() {
        rta.addInitializeHandler(event -> {
            IFrameElement iFrameElement = IFrameElement.as(rta.getElement());
            HeadElement headElement = iFrameElement.getContentDocument().getHead();

            MetaElement charsetMetaElement = Document.get().createMetaElement();
            charsetMetaElement.setHttpEquiv(HttpHeaders.CONTENT_TYPE);
            charsetMetaElement.setContent(HttpHeaders.CONTENT_TYPE_TEXT_HTML_UTF8);

            headElement.appendChild(charsetMetaElement);
        });
    }

    @Override
    protected void createRichTextToolbar(RichTextArea rta) {
        formatter = new JmixRichTextToolbarWidget(rta);
    }

    public void setLocaleMap(Map<String, String> localeMap) {
        ((JmixRichTextToolbarWidget) formatter).setLocaleMap(localeMap);
    }
}
