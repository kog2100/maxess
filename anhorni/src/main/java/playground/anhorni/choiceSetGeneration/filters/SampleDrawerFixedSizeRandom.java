/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.anhorni.choiceSetGeneration.filters;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import playground.anhorni.choiceSetGeneration.helper.ChoiceSet;


public class SampleDrawerFixedSizeRandom extends SampleDrawer {
	
	int maxSizeOfChoiceSets = 1;
	private final static Logger log = Logger.getLogger(SampleDrawerFixedSizeRandom.class);
	
	public SampleDrawerFixedSizeRandom(int maxSizeOfChoiceSets) {
		this.maxSizeOfChoiceSets = maxSizeOfChoiceSets;
	}
	
	@Override
	public void drawSample(List<ChoiceSet> choiceSets) {
		
		log.info("Sample choice sets to the size : " + this.maxSizeOfChoiceSets);
				
		Iterator<ChoiceSet> choiceSets_it = choiceSets.iterator();
		while (choiceSets_it.hasNext()) {
			ChoiceSet choiceSet = choiceSets_it.next();
			while (choiceSet.choiceSetSize() >  this.maxSizeOfChoiceSets) {
//				choiceSet.removeFacilityRandomly();
			}
		}
	}
}
