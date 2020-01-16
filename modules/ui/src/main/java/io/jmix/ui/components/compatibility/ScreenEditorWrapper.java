/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.components.compatibility;

import io.jmix.core.entity.Entity;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.Frame;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;

/**
 * Wrapper object for compatibility with legacy code.
 */
@Deprecated
public class ScreenEditorWrapper /*extends AbstractEditor */{

    private Screen screen;

    public ScreenEditorWrapper(Screen screen) {
        this.screen = screen;
    }

    /*
    TODO: legacy-ui
    @Override
    protected void initCommitActions(InitEvent event) {
        // do nothing
    }

    @Override
    public Frame getWrappedFrame() {
        return screen.getWindow();
    }

    @Override
    public void addListener(CloseListener listener) {
        screen.addAfterCloseListener(new AfterCloseListenerAdapter(listener));
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        screen.addAfterCloseListener(new AfterCloseListenerAdapter(listener));
    }

    @Override
    public Entity getItem() {
        return ((EditorScreen) screen).getEditedEntity();
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return screen.getWindow().getComponent(id);
    }*/
}