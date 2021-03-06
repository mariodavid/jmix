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

package io.jmix.ui.component;

import io.jmix.ui.screen.OpenMode;

import java.util.EventObject;
import java.util.function.Consumer;

public interface AppWorkArea extends Component.BelongToFrame {

    String NAME = "workArea";

    enum Mode {
        /**
         * If the main window is in TABBED mode, it creates the Tabsheet inside
         * and opens screens with {@link OpenMode#NEW_TAB} as tabs.
         */
        TABBED,

        /**
         * In SINGLE mode each new screen opened with {@link OpenMode#NEW_TAB}
         * opening type will replace the current screen.
         */
        SINGLE
    }

    enum State {
        INITIAL_LAYOUT,
        WINDOW_CONTAINER
    }

    Mode getMode();
    void setMode(Mode mode);

    State getState();

    void switchTo(State state);

    VBoxLayout getInitialLayout();
    void setInitialLayout(VBoxLayout initialLayout);

    void addStateChangeListener(Consumer<StateChangeEvent> listener);
    void removeStateChangeListener(Consumer<StateChangeEvent> listener);

    /**
     * @deprecated Use {@link Consumer} with {@link StateChangeEvent} type instead.
     */
    @Deprecated
    interface StateChangeListener extends Consumer<StateChangeEvent> {
        void stateChanged(State newState);

        @Override
        default void accept(StateChangeEvent event) {
            stateChanged(event.getState());
        }
    }

    /**
     * Event that is fired when work area changed its state.
     */
    class StateChangeEvent extends EventObject {

        protected final State state;

        public StateChangeEvent(AppWorkArea source, State state) {
            super(source);
            this.state = state;
        }

        @Override
        public AppWorkArea getSource() {
            return (AppWorkArea) super.getSource();
        }

        public State getState() {
            return state;
        }
    }
}