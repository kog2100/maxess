package playground.wrashid.parkingChoice.events;

import org.matsim.api.core.v01.events.ActivityStartEvent;

import playground.wrashid.parkingChoice.infrastructure.api.Parking;

public class ParkingArrivalEvent {

	public ActivityStartEvent getActStartEvent() {
		return actStartEvent;
	}

	public Parking getParking() {
		return parking;
	}

	private ActivityStartEvent actStartEvent;
	private Parking parking;

	public ParkingArrivalEvent(final ActivityStartEvent actStartEvent, final Parking parking) {
		this.actStartEvent=actStartEvent;
		this.parking=parking;
	}
	
}
