/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package playground.michalm.taxi.ev;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule.ScheduleStatus;
import org.matsim.contrib.taxi.data.TaxiData;

import playground.michalm.ev.UnitConversionRatios;
import playground.michalm.ev.data.*;
import playground.michalm.ev.discharging.OhdeSlaskiDriveEnergyConsumption;
import playground.michalm.taxi.data.EvrpVehicle;
import playground.michalm.taxi.data.EvrpVehicle.Ev;


public class ETaxiUtils
{
    public static void initEvData(TaxiData taxiData, EvData evData)
    {
        // TODO variable AUX and charging speeds -- depend on weather etc...
        double auxPower = 1. * UnitConversionRatios.W_PER_kW; //1 kW
        double chargingSpeedFactor = 1.; //full speed

        for (Charger c : evData.getChargers().values()) {
            new ETaxiChargingLogic(c, chargingSpeedFactor, auxPower);
        }

        for (Vehicle v : taxiData.getVehicles().values()) {
            Ev ev = ((EvrpVehicle)v).getEv();
            ev.setDriveEnergyConsumption(new OhdeSlaskiDriveEnergyConsumption(ev));
            ev.setAuxEnergyConsumption(
                    (period) -> consumeFixedAuxEnergyWhenScheduleStarted(ev, v, auxPower, period));
            evData.addElectricVehicle(Id.createVehicleId(v.getId()), ev);
        }
    }


    public static void consumeFixedAuxEnergyWhenScheduleStarted(ElectricVehicle ev, Vehicle taxi,
            double auxPower, double period)
    {
        if (taxi.getSchedule().getStatus() == ScheduleStatus.STARTED) {
            ev.getBattery().discharge(auxPower * period);
        }
    }
}
