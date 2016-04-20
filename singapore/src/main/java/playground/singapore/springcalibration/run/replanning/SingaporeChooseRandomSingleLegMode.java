/* *********************************************************************** *
 * project: org.matsim.*
 * SingaporeChooseRandomSingleLegMode.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.singapore.springcalibration.run.replanning;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.locationchoice.utils.PlanUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.singapore.springcalibration.run.TaxiUtils;

/**
 * Changes the transportation mode of one leg in a plan to a randomly chosen
 * mode, given a list of possible modes. Insures that the newly chosen mode
 * is different from the existing mode (if possible).
 *
 * @author anhorni
 */
public class SingaporeChooseRandomSingleLegMode implements PlanAlgorithm {

	private final String[] possibleModes;
	private boolean ignoreCarAvailability = false;
	private double walkThreshold = 5000.0;
	private Population population;
	private static final Logger log = Logger.getLogger(SingaporeChooseRandomSingleLegMode.class);
	private TaxiUtils taxiUtils;

	private final Random rng;

	/**
	 * @param possibleModes
	 * @param rng The random number generator used to draw random numbers to select another mode.
	 * @see TransportMode
	 * @see MatsimRandom
	 */
	public SingaporeChooseRandomSingleLegMode(final String[] possibleModes, final Random rng, Population population, TaxiUtils taxiUtils) {
		this.possibleModes = possibleModes.clone();
		this.rng = rng;
		this.population = population;
		this.taxiUtils = taxiUtils;
		
		this.taxiUtils.getWaitingTime(null); // TODO: remove
		log.info("Replanning for population of size: " + population.getPersons().size());
	}

	public void setIgnoreCarAvailability(final boolean ignoreCarAvailability) {
		this.ignoreCarAvailability = ignoreCarAvailability;
	}

	@Override
	public void run(final Plan plan) {
		boolean forbidCar = false;
		boolean forbidPassenger = false;
		boolean forbidOther = false;
		boolean forbidSchoolbus = false;
		
		if (!this.ignoreCarAvailability) {
			String carAvail = (String) population.getPersonAttributes().getAttribute(plan.getPerson().getId().toString(), "car");
			String license = (String) population.getPersonAttributes().getAttribute(plan.getPerson().getId().toString(), "license");						
			// as defined only people with license and car are allowed to use car
			if ("never".equals(carAvail) || "no".equals(license)) {
				forbidCar = true;
			}
			if ("never".equals(carAvail)) {
				forbidPassenger = true;
			}
		}
		
		String ageStr = (String) population.getPersonAttributes().getAttribute(plan.getPerson().getId().toString(), "age");
		// if there is no age given, e.g., for freight agents
		int age = 25;	
		String cleanedAge = ageStr.replace("age", "");
		cleanedAge = cleanedAge.replace("up", "");
		if (ageStr != null) age = Integer.parseInt(cleanedAge);
		if (age < 20) forbidOther = true;
		if (age > 20) forbidSchoolbus = true;

		ArrayList<Leg> legs = new ArrayList<Leg>();
		int cnt = 0;
		for (PlanElement pe : plan.getPlanElements()) {
			if (pe instanceof Leg) {
				legs.add((Leg) pe);
				cnt++;
			}
		}
		if (cnt == 0) {
			return;
		}
		int rndIdx = this.rng.nextInt(cnt);
		
		Leg chosenLeg = legs.get(rndIdx);
		
		// just to speed up relaxation
		boolean forbidWalk= false;
		Activity nextAct = PlanUtils.getNextActivity(plan, chosenLeg);
		Activity previousAct = PlanUtils.getPreviousActivity(plan, chosenLeg);
		double distance = CoordUtils.calcEuclideanDistance(previousAct.getCoord(), nextAct.getCoord());		
		if (distance > walkThreshold) forbidWalk = true; 
			
		setRandomLegMode(plan, chosenLeg, forbidCar, forbidPassenger, forbidWalk, forbidOther, forbidSchoolbus);
	}

	private void setRandomLegMode(Plan plan, final Leg leg, final boolean forbidCar, final boolean forbidPassenger, final boolean forbidWalk, final boolean forbidOther, boolean forbidSchoolbus) {
		String previousActivity = PlanUtils.getPreviousActivity(plan, leg).getType();
		String nextActivity = PlanUtils.getNextActivity(plan, leg).getType();
		
		if (!(previousActivity.equals("home") && nextActivity.contains("school") ||
				(previousActivity.contains("school") && nextActivity.equals("home")))) {
				forbidSchoolbus = true;	
				}
		
		leg.setMode(chooseModeOtherThan(leg.getMode(), forbidCar, forbidPassenger, forbidWalk, forbidOther, forbidSchoolbus));
	}

	private String chooseModeOtherThan(final String currentMode, final boolean forbidCar, final boolean forbidPassenger, final boolean forbidWalk, final boolean forbidOther, final boolean forbidSchoolbus) {
		String newMode;
		while (true) {
			int newModeIdx = this.rng.nextInt(this.possibleModes.length - 1);
			for (int i = 0; i <= newModeIdx; i++) {
				if (this.possibleModes[i].equals(currentMode)) {
					/* if the new Mode is after the currentMode in the list of possible
					 * modes, go one further, as we have to ignore the current mode in
					 * the list of possible modes. */
					newModeIdx++;
					break;
				}
			}
			newMode = this.possibleModes[newModeIdx];
			if (!(forbidCar && TransportMode.car.equals(newMode))) {
				break;
			} else {
				if (this.possibleModes.length == 2) {
					newMode = currentMode; // there is no other mode available
					break;
				}
			}
			
			if (!(forbidPassenger && "passenger".equals(newMode))) {
				break;
			} else {
				if (this.possibleModes.length == 2) {
					newMode = currentMode; // there is no other mode available
					break;
				}
			}
			
			if (!(forbidWalk && TransportMode.walk.equals(newMode))) {
				break;
			} else {
				if (this.possibleModes.length == 2) {
					newMode = currentMode; // there is no other mode available
					break;
				}
			}
			
			if (!(forbidOther && TransportMode.other.equals(newMode))) {
				break;
			} else {
				if (this.possibleModes.length == 2) {
					newMode = currentMode; // there is no other mode available
					break;
				}
			}
			
			if (!(forbidSchoolbus && "schoolbus".equals(newMode))) {
				break;
			} else {
				if (this.possibleModes.length == 2) {
					newMode = currentMode; // there is no other mode available
					break;
				}
			}
		}
		return newMode;
	}

}
