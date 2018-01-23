/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package playground.balac.parking.priceoptimization.scoring;

import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.multimodal.router.util.WalkTravelTime;
import org.matsim.contrib.parking.parkingchoice.PC2.infrastructure.PC2Parking;
import org.matsim.contrib.parking.parkingchoice.PC2.scoring.AbstractParkingBetas;
import org.matsim.contrib.parking.parkingchoice.PC2.scoring.ParkingScore;
import org.matsim.contrib.parking.parkingchoice.PC2.scoring.RandomErrorTermManager;
import org.matsim.contrib.parking.parkingchoice.lib.DebugLib;
import org.matsim.contrib.parking.parkingchoice.lib.obj.DoubleValueHashMap;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import playground.balac.parking.priceoptimization.infrastracture.OptimizableParking;


public final class ParkingScoreManager implements ParkingScore{
	
	private Double beelineDistanceFactor = 1.3 ;

	private AbstractParkingBetas parkingBetas;
	private double parkingScoreScalingFactor;
	private double randomErrorTermScalingFactor;
	private DoubleValueHashMap<Id<Person>> scores;
	private final Scenario scenario;
	private final WalkTravelTime walkTravelTime;
	private RandomErrorTermManager randomErrorTermManager;
	private Map<Id<Person>, Set<String>> permitsPerPerson;

	public ParkingScoreManager(WalkTravelTime walkTravelTime, Scenario scenario, 
			Map<Id<Person>, Set<String>> permitsPerPerson) {
		this.walkTravelTime = walkTravelTime;
		this.scenario = scenario;
		this.permitsPerPerson = permitsPerPerson;
	}
	@Override
	public double calcWalkScore(Coord destCoord, PC2Parking parking, Id<Person> personId, double parkingDurationInSeconds) {
		Map<Id<Person>, ? extends Person> persons = scenario.getPopulation().getPersons();
		Person person = persons.get(personId);

		double parkingWalkBeta = getParkingBetas().getParkingWalkBeta(person, parkingDurationInSeconds);

		Link link = NetworkUtils.getNearestLink((scenario.getNetwork()), destCoord);
		
		double linkLength = link.getLength();		
		
		double walkDistance = CoordUtils.calcEuclideanDistance(destCoord, parking.getCoordinate()) 
				* scenario.getConfig().plansCalcRoute().getBeelineDistanceFactors().get("walk")* beelineDistanceFactor;
		
		double walkSpeed = linkLength / this.walkTravelTime.getLinkTravelTime(link, 0, person, null);

		double walkDurationInSeconds = walkDistance / walkSpeed;

		double walkingTimeTotalInMinutes = walkDurationInSeconds / 60;

		if (parking.getId().toString().contains("stp")){
			DebugLib.emptyFunctionForSettingBreakPoint();
		}
	
		return (parkingWalkBeta * walkingTimeTotalInMinutes) * parkingScoreScalingFactor;
	}
	@Override
	public double calcCostScore(double arrivalTime, double parkingDurationInSeconds, PC2Parking parking, Id<Person> personId) {
		Person person = scenario.getPopulation().getPersons().get(personId);
		double parkingCostBeta = getParkingBetas().getParkingCostBeta(person);
		double parkingCost = parking.getCost(personId, arrivalTime, parkingDurationInSeconds);
		return (parkingCostBeta * parkingCost) * parkingScoreScalingFactor;
	}
	
	public double calcScore(Coord destCoord, double arrivalTime, double parkingDurationInSeconds, PC2Parking parking, 
			Id<Person> personId, int legIndex, boolean setCostToZero, String actType) {
		
		double walkScore = calcWalkScore(destCoord, parking, personId, parkingDurationInSeconds);
		double costScore = 0.0;
		if (actType.equals("home")) {
			
			if (parking instanceof OptimizableParking) {
				
				if (((OptimizableParking) parking).isBlueZone()) {
					if (this.permitsPerPerson.containsKey(personId) &&
							this.permitsPerPerson.get(personId).contains("blue")){
						costScore = 0;						
					}
					else 
						costScore = calcCostScore(arrivalTime, parkingDurationInSeconds, parking, personId);						
										
				}
				else if (parking.getGroupName().equals("garageParking") && 
						this.permitsPerPerson.containsKey(personId) &&
						this.permitsPerPerson.get(personId).contains("garage")){
					costScore = 0;	
					
				}
				else
					costScore = calcCostScore(arrivalTime, parkingDurationInSeconds, parking, personId);

			}
			else 				
				costScore = calcCostScore(arrivalTime, parkingDurationInSeconds, parking, personId);
						
		}
		else {
			//TODO: currently no permits for parking at work outside of private facilities
			costScore = calcCostScore(arrivalTime, parkingDurationInSeconds, parking, personId);

			
			
		}
		
		
		if (setCostToZero){
			costScore=0;
		}
		
		double randomError=0;

		if (randomErrorTermManager!=null){
			randomError= randomErrorTermManager.getEpsilonAlternative(parking.getId(),personId,legIndex)*randomErrorTermScalingFactor*parkingScoreScalingFactor;
		}
	return costScore + walkScore + randomError;
	}
	@Override
	public double getScore(Id<Person> id) {
		return scores.get(id);
	}
	@Override
	public synchronized void addScore(Id<Person> id, double incValue) {
		scores.incrementBy(id, incValue);
	}
	@Override
	public synchronized void prepareForNewIteration() {
		scores = new DoubleValueHashMap<>();
	}
	@Override
	public double getParkingScoreScalingFactor() {
		return parkingScoreScalingFactor;
	}
	@Override
	public void setParkingScoreScalingFactor(double parkingScoreScalingFactor) {
		this.parkingScoreScalingFactor = parkingScoreScalingFactor;
	}
	@Override
	public double getRandomErrorTermScalingFactor() {
		return randomErrorTermScalingFactor;
	}
	@Override
	public void setRandomErrorTermScalingFactor(double randomErrorTermScalingFactor) {
		this.randomErrorTermScalingFactor = randomErrorTermScalingFactor;
	}
	@Override
	public AbstractParkingBetas getParkingBetas() {
		return parkingBetas;
	}
	@Override
	public void setParkingBetas(AbstractParkingBetas parkingBetas) {
		this.parkingBetas = parkingBetas;
	}
	@Override
	public void setRandomErrorTermManger(RandomErrorTermManager randomErrorTermManager) {
		this.randomErrorTermManager = randomErrorTermManager;
	}
	@Override
	public double calcScore(Coord destCoord, double arrivalTime, double parkingDurationInSeconds, PC2Parking parking,
			Id<Person> personId, int legIndex, boolean setCostToZero) {
		double walkScore = calcWalkScore(destCoord, parking, personId, parkingDurationInSeconds);
		double costScore = calcCostScore(arrivalTime, parkingDurationInSeconds, parking, personId);
		
		if (setCostToZero){
			costScore=0;
		}
		
		double randomError=0;

		if (randomErrorTermManager!=null){
			randomError= randomErrorTermManager.getEpsilonAlternative(parking.getId(),personId,legIndex)*randomErrorTermScalingFactor*parkingScoreScalingFactor;
		}
		return costScore + walkScore + randomError;
	}

}
