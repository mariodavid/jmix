/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.browserframe.JmixBrowserFrameState;

import java.util.Objects;

public class JmixBrowserFrame extends com.vaadin.ui.BrowserFrame {

    @Override
    protected JmixBrowserFrameState getState() {
        return (JmixBrowserFrameState) super.getState();
    }

    @Override
    protected JmixBrowserFrameState getState(boolean markAsDirty) {
        return (JmixBrowserFrameState) super.getState(markAsDirty);
    }

    public void setSandbox(String sandbox) {
        if (!Objects.equals(sandbox, getState(false).sandbox)) {
            getState().sandbox = sandbox;
        }
    }

    public String getSandbox() {
        return getState(false).sandbox;
    }

    public void setSrcdoc(String srcdoc) {
        if (!Objects.equals(srcdoc, getState(false).srcdoc)) {
            getState().srcdoc = srcdoc;
        }
    }

    public String getSrcdoc() {
        return getState(false).srcdoc;
    }

    public void setAllow(String allow) {
        if (!Objects.equals(allow, getState(false).allow)) {
            getState().allow = allow;
        }
    }

    public String getAllow() {
        return getState(false).allow;
    }

    public void setReferrerPolicy(String referrerpolicy) {
        if (!Objects.equals(referrerpolicy, getState(false).referrerpolicy)) {
            getState().referrerpolicy = referrerpolicy;
        }
    }

    public String getReferrerPolicy() {
        return getState(false).referrerpolicy;
    }
}
