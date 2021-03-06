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

package io.jmix.ui.widget.client.treegrid;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.widget.grid.selection.SelectionModel;
import com.vaadin.client.widget.grid.selection.SelectionModelWithSelectionColumn;
import io.jmix.ui.widget.client.grid.JmixEditorEventHandler;
import io.jmix.ui.widget.client.grid.JmixGridEmptyState;
import io.jmix.ui.widget.client.grid.HasClickSettings;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.renderers.Renderer;
import com.vaadin.client.widget.escalator.FlyweightCell;
import com.vaadin.client.widget.escalator.RowContainer;
import com.vaadin.client.widget.grid.events.GridClickEvent;
import com.vaadin.client.widget.treegrid.TreeGrid;
import elemental.events.Event;
import elemental.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class JmixTreeGridWidget extends TreeGrid {

    public static final String JMIX_ID_COLUMN_PREFIX = "column_";
    public static final String JMIX_ID_COLUMN_HIDING_TOGGLE_PREFIX = "cc_";
    public static final String COLUMN_HIDING_TOGGLE_STYLENAME = "column-hiding-toggle";
    public static final String MULTI_CHECK_STYLENAME = "multi-check";

    protected Map<Column<?, JsonObject>, String> columnIds = null;

    protected JmixGridEmptyState emptyState;
    protected Runnable emptyStateLinkClickHandler;

    protected String selectAllLabel;
    protected String deselectAllLabel;

    @Override
    public void setSelectionModel(SelectionModel<JsonObject> selectionModel) {
        super.setSelectionModel(selectionModel);

        boolean multiCheck = getSelectionModel() instanceof SelectionModelWithSelectionColumn
                && ((SelectionModelWithSelectionColumn) getSelectionModel()).getRenderer() != null;
        getEscalator().setStyleName(MULTI_CHECK_STYLENAME, multiCheck);
    }

    public Map<Column<?, JsonObject>, String> getColumnIds() {
        return columnIds;
    }

    public void setColumnIds(Map<Column<?, JsonObject>, String> columnProperties) {
        this.columnIds = columnProperties;
    }

    public void addColumnId(Column<?, JsonObject> column, String id) {
        if (columnIds == null) {
            columnIds = new HashMap<>();
        }

        columnIds.put(column, id);
    }

    public void removeColumnId(Column<?, JsonObject> column) {
        if (columnIds != null) {
            columnIds.remove(column);
        }
    }

    public String getSelectAllLabel() {
        return selectAllLabel;
    }

    public void setSelectAllLabel(String selectAllLabel) {
        this.selectAllLabel = selectAllLabel;
    }

    public String getDeselectAllLabel() {
        return deselectAllLabel;
    }

    public void setDeselectAllLabel(String deselectAllLabel) {
        this.deselectAllLabel = deselectAllLabel;
    }

    /*
     * Workaround to avoid disappearing footer when changing the predefined styles at runtime in Safari
     */
    public void updateFooterVisibility() {
        Footer footer = getFooter();
        if (!footer.isVisible()) {
            return;
        }

        footer.setVisible(false);
        footer.setVisible(true);
    }

    public void showEmptyState(boolean show) {
        if (show) {
            if (emptyState == null) {
                emptyState = new JmixGridEmptyState();
            }

            Element wrapper = getEscalator().getTableWrapper();
            Element panelParent = emptyState.getElement().getParentElement();

            if (panelParent == null || !panelParent.equals(wrapper)) {
                wrapper.appendChild(emptyState.getElement());
            }
        } else if (emptyState != null) {
            emptyState.getElement().removeFromParent();
            emptyState = null;
        }
    }

    public JmixGridEmptyState getEmptyState() {
        return emptyState;
    }

    @Override
    protected Editor<JsonObject> createEditor() {
        Editor<JsonObject> editor = super.createEditor();
        editor.setEventHandler(new JmixEditorEventHandler<>());
        return editor;
    }

    @Override
    protected boolean isWidgetAllowsClickHandling(Element targetElement, NativeEvent nativeEvent) {
        // By default, clicking on widget renderer prevents row selection.
        // We want to allow row selection. Every time selection is changed,
        // all renderers render their content, as the result, components rendered by
        // ComponentRenderer lose focus because they are replaced with new instances,
        // so we prevent click handling for Focus widgets.
        Widget widget = WidgetUtil.findWidget(targetElement, null);
        return !isWidgetOrParentFocusable(widget);
    }

    protected boolean isWidgetOrParentFocusable(Widget widget) {
        boolean widgetFocusable = isWidgetFocusable(widget);
        if (!widgetFocusable) {
            Widget parent = widget.getParent();
            while (parent != null
                    && !widgetFocusable
                    && !isGridCell(parent)) {
                widgetFocusable = isWidgetFocusable(parent);
                parent = parent.getParent();
            }
        }
        return widgetFocusable;
    }

    private boolean isGridCell(Widget parent) {
        String styleName = parent.getStyleName();
        // We assume that in most cases Widget is added by a ComponentRenderer,
        // so it's wrapped by a div with the 'component-wrap' style name.
        // If for some reason we didn't find a component wrapper, we stop when we reached a grid.
        return styleName != null && styleName.contains("component-wrap")
                || parent instanceof JmixTreeGridWidget;
    }

    protected boolean isWidgetFocusable(Widget widget) {
        return widget instanceof com.vaadin.client.Focusable
                || widget instanceof com.google.gwt.user.client.ui.Focusable;
    }

    @Override
    protected boolean isEventHandlerShouldHandleEvent(Element targetElement, GridEvent<JsonObject> event) {
        if (!event.getDomEvent().getType().equals(Event.MOUSEDOWN)
                && !event.getDomEvent().getType().equals(Event.CLICK)) {
            return super.isEventHandlerShouldHandleEvent(targetElement, event);
        }

        // By default, clicking on widget renderer prevents cell focus changing
        // for some widget renderers we want to allow focus changing
        Widget widget = WidgetUtil.findWidget(targetElement, null);
        return !(isWidgetOrParentFocusable(widget))
                || isClickThroughEnabled(targetElement);
    }

    protected boolean isClickThroughEnabled(Element e) {
        Widget widget = WidgetUtil.findWidget(e, null);
        return widget instanceof HasClickSettings &&
                ((HasClickSettings) widget).isClickThroughEnabled();
    }

    @Override
    protected UserSorter createUserSorter() {
        return new JmixUserSorter();
    }

    protected class JmixUserSorter extends UserSorter {

        protected JmixUserSorter() {
        }

        @Override
        public void sort(Column<?, ?> column, boolean multisort) {
            // ignore 'multisort' until datasources don't support multi-sorting
            super.sort(column, false);
        }
    }

    protected class JmixStaticSectionUpdater extends StaticSectionUpdater {

        public JmixStaticSectionUpdater(StaticSection<?> section, RowContainer container) {
            super(section, container);
        }

        @Override
        protected void addAdditionalData(StaticSection.StaticRow<?> staticRow, FlyweightCell cell) {
            if (columnIds != null) {
                Column<?, JsonObject> column = getVisibleColumns().get(cell.getColumn());
                Object columnId = (columnIds.containsKey(column))
                        ? columnIds.get(column)
                        : cell.getColumn();

                Element cellElement = cell.getElement();
                cellElement.setAttribute("j-test-id", JMIX_ID_COLUMN_PREFIX + columnId);
            }
        }
    }

    @Override
    protected ColumnHider createColumnHider() {
        return new JmixColumnHider();
    }

    protected class JmixColumnHider extends ColumnHider {

        protected boolean defaultTogglesInitialized;

        @Override
        protected String getCustomHtmlAttributes(Column<?, JsonObject> column) {
            if (columnIds != null) {
                Object columnId = (columnIds.get(column));
                if (columnId != null) {
                    return "j-test-id=\"" +
                            JMIX_ID_COLUMN_HIDING_TOGGLE_PREFIX +
                            JMIX_ID_COLUMN_PREFIX +
                            columnId + "\"";
                }
            }

            return super.getCustomHtmlAttributes(column);
        }

        @Override
        protected int getFirstColumnToggleIndex() {
            // Column toggles will be displayed after default toggles (selectAll + deselectAll + separator)
            return 3;
        }

        @Override
        protected void updateTogglesOrder() {
            if (getHidableColumnsCount() != 0) {
                if (!defaultTogglesInitialized) {
                    defaultTogglesInitialized = true;
                    initDefaultToggles();
                }

                super.updateTogglesOrder();
            } else {
                getSidebarMenu().removeFromParent();
            }
        }

        protected void initDefaultToggles() {
            MenuBar sidebarMenu = getSidebarMenu();

            sidebarMenu.addItem(createSelectAllToggle());
            sidebarMenu.addItem(createDeselectAllToggle());

            sidebarMenu.addSeparator();
        }

        protected MenuItem createSelectAllToggle() {
            MenuItem selectAllToggle = new MenuItem(getDefaultToggleHTML(getSelectAllLabel()), true, getSelectAllCommand());
            selectAllToggle.addStyleName(COLUMN_HIDING_TOGGLE_STYLENAME);
            return selectAllToggle;
        }

        protected Scheduler.ScheduledCommand getSelectAllCommand() {
            return () -> {
                for (Column column : getColumns()) {
                    if (column.isHidden()) {
                        column.setHidden(false);
                    }
                }
            };
        }

        protected MenuItem createDeselectAllToggle() {
            MenuItem deselectAllToggle = new MenuItem(getDefaultToggleHTML(getDeselectAllLabel()), true, getDeselectAllCommand());
            deselectAllToggle.addStyleName(COLUMN_HIDING_TOGGLE_STYLENAME);
            return deselectAllToggle;
        }

        protected Scheduler.ScheduledCommand getDeselectAllCommand() {
            return () -> {
                for (Column column : getColumns()) {
                    if (column.isHidable() && !column.isHidden()) {
                        column.setHidden(true);
                    }
                }
            };
        }

        protected String getDefaultToggleHTML(String caption) {
            return "<span class=\"v-off\">" +
                    "<div>" +
                    SafeHtmlUtils.htmlEscape(caption) +
                    "</div>" +
                    "</span>";
        }

        protected long getHidableColumnsCount() {
            return getColumns().stream()
                    .filter(Column::isHidable)
                    .count();
        }
    }

    @Override
    protected SelectionColumn createSelectionColumn(Renderer<Boolean> selectColumnRenderer) {
        return new JmixSelectionColumn(selectColumnRenderer);
    }

    protected class JmixSelectionColumn extends SelectionColumn {

        public JmixSelectionColumn(Renderer<Boolean> selectColumnRenderer) {
            super(selectColumnRenderer);
        }

        @Override
        protected void onHeaderClickEvent(GridClickEvent event) {
            // do nothing, as we want to trigger select/deselect all only by clicking on the checkbox
        }
    }
}
