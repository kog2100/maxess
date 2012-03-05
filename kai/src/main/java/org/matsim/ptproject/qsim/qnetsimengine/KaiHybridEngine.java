/* *********************************************************************** *
 * project: kai
 * KaiHybridEngine.java
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

package org.matsim.ptproject.qsim.qnetsimengine;

import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.ptproject.qsim.InternalInterface;
import org.matsim.ptproject.qsim.interfaces.MobsimEngine;
import org.matsim.ptproject.qsim.interfaces.Netsim;

public class KaiHybridEngine implements MobsimEngine {

	private InternalInterface internalInterface;
	private Map<Id,KaiHiResLink> hiResLinks = new TreeMap<Id,KaiHiResLink>() ;

	@Override
	public void afterSim() {
	}

	@Override
	public Netsim getMobsim() {
		return this.internalInterface.getMobsim() ;
	}

	@Override
	public void onPrepareSim() {
	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		this.internalInterface = internalInterface ;
	}

	@Override
	public void doSimStep(double time) {
		for ( KaiHiResLink hiResLink : hiResLinks.values() ) {
			hiResLink.doSimStep(time) ;
		}
	}

	public void registerHiResLink(KaiHiResLink hiResLink) {
		this.hiResLinks.put( hiResLink.getLink().getId(), hiResLink ) ;
	}
	
}