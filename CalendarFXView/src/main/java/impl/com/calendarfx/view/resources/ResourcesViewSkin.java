/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package impl.com.calendarfx.view.resources;

import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.AllDayView;
import com.calendarfx.view.TimeScaleView;
import com.calendarfx.view.WeekDayHeaderView;
import com.calendarfx.view.resources.Resource;
import com.calendarfx.view.resources.ResourcesView;
import impl.com.calendarfx.view.DateControlSkin;
import impl.com.calendarfx.view.DayViewScrollPane;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import static com.calendarfx.util.ViewHelper.scrollToRequestedTime;

public class ResourcesViewSkin<T extends Resource<?>> extends DateControlSkin<ResourcesView<T>> {

    private final GridPane gridPane;
    private final ResourcesContainer<T> resourcesViewContainer;
    private final DayViewScrollPane timeScaleScrollPane;
    private final DayViewScrollPane dayViewsScrollPane;
    private final ScrollBar scrollBar;

    public ResourcesViewSkin(ResourcesView<T> view) {
        super(view);

        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);

        TimeScaleView timeScale = new TimeScaleView();
        view.bind(timeScale, true);

        // time scale scroll pane
        timeScaleScrollPane = new DayViewScrollPane(timeScale, scrollBar);
        timeScaleScrollPane.getStyleClass().addAll("calendar-scroll-pane", "day-view-timescale-scroll-pane");
        timeScaleScrollPane.setMinWidth(Region.USE_PREF_SIZE);

        final InvalidationListener visibilityListener = it -> updateView();
        view.showAllDayViewProperty().addListener(visibilityListener);
        view.showTimeScaleViewProperty().addListener(visibilityListener);
        view.layoutProperty().addListener(visibilityListener);
        view.showScrollBarProperty().addListener(visibilityListener);

        RowConstraints row0 = new RowConstraints();
        row0.setFillHeight(true);
        row0.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row0.setVgrow(Priority.NEVER);

        RowConstraints row1 = new RowConstraints();
        row1.setFillHeight(true);
        row1.setPrefHeight(Region.USE_COMPUTED_SIZE);
        row1.setVgrow(Priority.ALWAYS);


        gridPane = new GridPane();
        gridPane.getRowConstraints().setAll(row0, row1);
        gridPane.getStyleClass().add("container");

        resourcesViewContainer = new ResourcesContainer<>(view);
        view.bind(resourcesViewContainer, true);

        getChildren().add(gridPane);

        dayViewsScrollPane = new DayViewScrollPane(resourcesViewContainer, scrollBar);

        /*
         * Run later when the control has become visible.
         */
        Platform.runLater(() -> scrollToRequestedTime(view, dayViewsScrollPane));

        view.requestedTimeProperty().addListener(it -> scrollToRequestedTime(view, dayViewsScrollPane));

        view.resourcesProperty().addListener((Observable it) -> updateView());
        view.numberOfDaysProperty().addListener((Observable it) -> updateView());
        updateView();
    }


    private void updateView() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        final ResourcesView<T> view = getSkinnable();

        if (view.isShowTimeScaleView()) {
            ColumnConstraints timeScaleColumn = new ColumnConstraints();
            timeScaleColumn.setFillWidth(true);
            timeScaleColumn.setHgrow(Priority.NEVER);
            gridPane.getColumnConstraints().add(timeScaleColumn);
            gridPane.add(timeScaleScrollPane, 0, 1);
        }

        HBox headerBox = new HBox();
        headerBox.getStyleClass().add("header-box");

        headerBox.setFillHeight(true);
        gridPane.add(headerBox, 1, 0);

        Callback<T, Node> resourceHeaderFactory = view.getResourceHeaderFactory();

        ObservableList<T> resources = view.getResources();
        for (int i = 0; i < resources.size(); i++) {
            T resource = resources.get(i);
            Node headerNode = resourceHeaderFactory.call(resource);
            VBox resourceHeader = new VBox(headerNode);

            if (view.isShowAllDayView()) {
                AllDayView allDayView = new AllDayView();
                allDayView.setAdjustToFirstDayOfWeek(false);

                // bind AllDayView
                view.bind(allDayView, true);
                allDayView.numberOfDaysProperty().bind(view.numberOfDaysProperty());

                // some unbindings for AllDayView
                Bindings.unbindBidirectional(view.defaultCalendarProviderProperty(), allDayView.defaultCalendarProviderProperty());
                Bindings.unbindBidirectional(view.draggedEntryProperty(), allDayView.draggedEntryProperty());
                Bindings.unbindContentBidirectional(view.getCalendarSources(), allDayView.getCalendarSources());

                CalendarSource calendarSource = createCalendarSource(resource);
                allDayView.getCalendarSources().setAll(calendarSource);
                allDayView.setDefaultCalendarProvider(control -> calendarSource.getCalendars().get(0));

                resourceHeader.getChildren().add(allDayView);
            }

            resourceHeader.getStyleClass().add("resource-header-view");
            HBox.setHgrow(resourceHeader, Priority.ALWAYS);

            WeekDayHeaderView weekDayHeaderView = view.getWeekDayHeaderViewFactory().call(resource);
            weekDayHeaderView.setAdjustToFirstDayOfWeek(false);
            weekDayHeaderView.numberOfDaysProperty().bind(view.numberOfDaysProperty());
            view.bind(weekDayHeaderView, true);

            resourceHeader.setPrefWidth(0); // so they all end up with the same percentage width
            resourceHeader.getChildren().add(weekDayHeaderView);

            headerBox.getChildren().add(resourceHeader);
        }

        ColumnConstraints dayViewsConstraints = new ColumnConstraints();
        dayViewsConstraints.setFillWidth(true);
        dayViewsConstraints.setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().add(dayViewsConstraints);
        gridPane.add(dayViewsScrollPane, 1, 1);

        if (view.isShowScrollBar()) {
            ColumnConstraints scrollbarConstraint = new ColumnConstraints();
            scrollbarConstraint.setFillWidth(true);
            scrollbarConstraint.setHgrow(Priority.NEVER);
            scrollbarConstraint.setPrefWidth(Region.USE_COMPUTED_SIZE);
            gridPane.getColumnConstraints().add(scrollbarConstraint);

            gridPane.add(scrollBar, 2, 1);
        }
    }

    private CalendarSource createCalendarSource(T resource) {
        CalendarSource source = new CalendarSource(resource.getUserObject().toString());
        source.getCalendars().add(resource.getCalendar());
        return source;
    }
}
