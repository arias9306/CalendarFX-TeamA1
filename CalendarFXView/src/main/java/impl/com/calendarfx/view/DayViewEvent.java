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

package impl.com.calendarfx.view;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * An event class used to signal changes done in the DayView GUI. This class was
 * created in order to avoid the unnecessary creation of flags and methods
 * (set/get) to inform about changes on the DayView GUI. {@see CalendarEvent}
 */
public class DayViewEvent extends Event {

    private static final long serialVersionUID = 5953922471280430033L;
    /**
     * An event type used to inform the DayViewScrollPane that the
     * DraggedReleaseCallback was called, and might require to stop the
     * AutoScroll if needed.
     */
    public static final EventType<DayViewEvent> DRAGGED_CALLBACK_CALLED = new EventType<>(
            DayViewEvent.ANY, "DRAGGED_CALLBACK_CALLED"); //$NON-NLS-1$

    public DayViewEvent(EventType<? extends DayViewEvent> eventType) {
        super(eventType);
    }

}
