package playground.balac.parking.parkingChoice.events;

import org.matsim.api.core.v01.events.ActivityStartEvent;

import playground.balac.parking.infrastructure.PParking;


public class ParkingArrivalEvent {

	public ActivityStartEvent getActStartEvent() {
		return actStartEvent;
	}

	public PParking getParking() {
		return parking;
	}

	private ActivityStartEvent actStartEvent;
	private PParking parking;

	public ParkingArrivalEvent(final ActivityStartEvent actStartEvent, final PParking parking) {
		this.actStartEvent=actStartEvent;
		this.parking=parking;
	}
	
}
