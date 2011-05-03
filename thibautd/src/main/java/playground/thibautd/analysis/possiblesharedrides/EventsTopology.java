/* *********************************************************************** *
 * project: org.matsim.*
 * EventsTopology.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package playground.thibautd.analysis.possiblesharedrides;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.Event;
import org.matsim.core.api.experimental.events.PersonEvent;

/**
 * Defines a topology on events.
 * Neigborhood is defined by a LinkTopology and a time window.
 *
 * @author thibautd
 */
public class EventsTopology {

	private final List<? extends PersonEvent> events;
	private final LinkTopology linkTopology;
	private final Comparator<Event> timeComparator = new TimeComparator();
	private final double timeWindowRadius;
	
	public EventsTopology(
			final List<? extends PersonEvent> events,
			final double timeWindowRadius,
			final LinkTopology linkTopology) {
		// not safe, clone events!
		this.events = events;
		Collections.sort(events, this.timeComparator); 
		this.linkTopology = linkTopology;
		this.timeWindowRadius = timeWindowRadius;
	}

	/**
	 * get neighbors based on the link topology and the default time
	 * window
	 */
	public List<PersonEvent> getNeighbors(final Event event) {
		return null;
	}

	/**
	 * @return all events in the specified time window at the given link
	 */
	public List<PersonEvent> getEventsInTimeWindow(
			final Id linkId,
			final double timeWindowCenter,
			final double timeWindowRadius) {
		return null;
	}

	private class TimeComparator implements Comparator<Event> {
		public TimeComparator() {}

		@Override
		public int compare(Event event1, Event event2) {
			double time1 = event1.getTime();
			double time2 = event2.getTime();

			if (time1 < time2) {
				return -1;
			}
			if (time1 > time2) {
				return 1;
			}
			return 0;
		}
	}
}

