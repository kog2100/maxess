/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package playground.ikaddoura.optAV;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.noise.personLinkMoneyEvents.PersonLinkMoneyEvent;
import org.matsim.core.api.experimental.events.EventsManager;

import com.google.inject.Inject;

import playground.ikaddoura.decongestion.data.DecongestionInfo;

/**
 * Throws agent money events for the tolled links and time bins.
 * 
 * @author ikaddoura
 */

public class AVIntervalBasedTolling implements LinkLeaveEventHandler, ActivityEndEventHandler {

	private final Set<Id<Person>> taxiDrivers = new HashSet<Id<Person>>();

	@Inject
	private EventsManager eventsManager;
	
	@Inject
	private DecongestionInfo decongestionInfo;
	
	private double totalTollPayments;

	@Override
	public void reset(int iteration) {
		this.totalTollPayments = 0.;
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		if (this.taxiDrivers.contains(this.decongestionInfo.getVehicleId2personId().get(event.getVehicleId()))) {
			// taxi
			
			if (decongestionInfo.getlinkInfos().get(event.getLinkId()) != null) {
				
				int currentTimeBin = (int) (event.getTime() / this.decongestionInfo.getScenario().getConfig().travelTimeCalculator().getTraveltimeBinSize());
				
				if (decongestionInfo.getlinkInfos().get(event.getLinkId()).getTime2toll().get(currentTimeBin) != null) {
					double toll = decongestionInfo.getlinkInfos().get(event.getLinkId()).getTime2toll().get(currentTimeBin);
					this.eventsManager.processEvent(new PersonMoneyEvent(event.getTime(), this.decongestionInfo.getVehicleId2personId().get(event.getVehicleId()), -1. * toll));
					this.eventsManager.processEvent(new PersonLinkMoneyEvent(event.getTime(), this.decongestionInfo.getVehicleId2personId().get(event.getVehicleId()), event.getLinkId(), -1. * toll, event.getTime()));
					this.totalTollPayments = this.totalTollPayments + toll;
				}		
			}
			
		} else {
			// no taxi, do not charge normal car users
		}
	}

	public double getTotalTollPayments() {
		return totalTollPayments;
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if (event.getActType().equals(VrpAgentLogic.BEFORE_SCHEDULE_ACTIVITY_TYPE)) {
			this.taxiDrivers.add(event.getPersonId());
		}
	}

}

