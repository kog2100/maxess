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
package playground.agarwalamit.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.collections.Tuple;

/**
 * This calculates activity durations for each activity type and each persons. 
 * Repetition of activities for same person will not be summed up instead will 
 * be added to list of person's activities.
 * @author amit
 */
public class ActivityType2DurationHandler implements ActivityEndEventHandler, ActivityStartEventHandler {

	public ActivityType2DurationHandler(double simulationEndTime) {
		this.personId2ActInfo = new HashMap<>();
		this.actTyps = new HashSet<>();
		this.simulationEndTime = simulationEndTime;
	}

	public static Logger log = Logger.getLogger(ActivityType2DurationHandler.class);
	private Map<Id<Person>, PersonActivityInfo> personId2ActInfo;
	double simulationEndTime;
	private Set<String> actTyps;

	@Override
	public void reset(int iteration) {
		personId2ActInfo.clear();
		actTyps.clear();
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		if(personId2ActInfo.containsKey(event.getPersonId())){
			PersonActivityInfo perActInfo = personId2ActInfo.get(event.getPersonId());
			Tuple<String, Double> act2Time = new Tuple<String, Double>(event.getActType(), event.getTime());
			perActInfo.getActType2StartTimes().add(act2Time);
		} else throw new RuntimeException("Person"+event.getPersonId()+" must have ended home activity first and then"
				+ " must have started "+event.getActType()+" activity. It should have"
				+ "already registered in map. Aborting...");
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		actTyps.add(event.getActType());
		
		if(personId2ActInfo.containsKey(event.getPersonId())){
			PersonActivityInfo perActInfo = personId2ActInfo.get(event.getPersonId());
			Tuple<String, Double> actEndTime = new Tuple<String, Double>(event.getActType(), event.getTime());
			perActInfo.getActType2EndTimes().add(actEndTime);
		} else{
			PersonActivityInfo perActInfo =  new PersonActivityInfo(event.getPersonId());

			Tuple<String, Double> homeStartTime = new Tuple<String, Double>(event.getActType(), 0.0);
			perActInfo.getActType2StartTimes().add(homeStartTime);

			Tuple<String, Double> actEndTime = new Tuple<String, Double>(event.getActType(), event.getTime());
			perActInfo.getActType2EndTimes().add(actEndTime);

			personId2ActInfo.put(event.getPersonId(), perActInfo);
		}
	}

	public Map<Id<Person>, PersonActivityInfo> getActivityType2ActivityDurations(){
		return personId2ActInfo;
	}

	public Map<Id<Person>, List<Tuple<String, Double>>> getPersonId2ActStartTimes(){
		Map<Id<Person>, List<Tuple<String, Double>>> personId2ActStartTimes = new HashMap<Id<Person>, List<Tuple<String,Double>>>();
		for(Id<Person> personId : personId2ActInfo.keySet()){
			personId2ActStartTimes.put(personId, personId2ActInfo.get(personId).getActType2StartTimes());
		}
		return personId2ActStartTimes;
	}

	public Map<Id<Person>, List<Tuple<String, Double>>> getPersonId2ActEndTimes(){
		Map<Id<Person>, List<Tuple<String, Double>>> personId2ActEndTimes = new HashMap<Id<Person>, List<Tuple<String,Double>>>();
		for(Id<Person> personId : personId2ActInfo.keySet()){
			personId2ActEndTimes.put(personId, personId2ActInfo.get(personId).getActType2EndTimes());
		}
		return personId2ActEndTimes;
	}

	/**
	 * @return person id to activity duration for each activity while 
	 * reporting repetition of same activities differently. 
	 */
	public Map<Id<Person>, Map<String, List<Double>>> getPersonId2ActDurations(){
		Map<Id<Person>, Map<String, List<Double>>> personId2ActType2ActDurations =
				new HashMap<Id<Person>, Map<String,List<Double>>>();
		
		int warnCount =0;
		for(Id<Person> personId : personId2ActInfo.keySet()){
			List<Tuple<String, Double>> actEndTimes = personId2ActInfo.get(personId).getActType2EndTimes();
			List<Tuple<String, Double>> actStartTimes = personId2ActInfo.get(personId).getActType2StartTimes();
			
			//store endTime of last activity if last activity do not have end time.
			if(actStartTimes.size()!=actEndTimes.size()) {
				int noOfActivities = actStartTimes.size();
				String lastActType = actStartTimes.get(noOfActivities-1).getFirst();
				actEndTimes.add(new Tuple<String, Double>(lastActType, simulationEndTime));
			} else {
				if(warnCount==0){
					log.warn("Person "+personId+" do not have any open ended activity and simulation ends."
							+ "Possible explanation must be stuckAndAbort.");
					Gbl.ONLYONCE.toString();
				}
				warnCount++;
			}

			Map<String, List<Double>> actType2ActDurations = new HashMap<String, List<Double>>();
			for(int i=0;i<actEndTimes.size();i++){
				String actType = actEndTimes.get(i).getFirst();

				List<Double> actDurations;
				if(actType2ActDurations.containsKey(actType)){
					actDurations = actType2ActDurations.get(actType);
				} else {
					actDurations = new ArrayList<Double>();
				}
				double actStartTime = actStartTimes.get(i).getSecond();
				double actEndTime = actEndTimes.get(i).getSecond();
				double durationEndTime = actEndTime - actStartTime;
				actDurations.add(durationEndTime);
				actType2ActDurations.put(actType, actDurations);
			}
			personId2ActType2ActDurations.put(personId, actType2ActDurations);
		}
		return personId2ActType2ActDurations;
	}
	
	public Set<String> getActivityTypes(){
		return this.actTyps;
	}
}
