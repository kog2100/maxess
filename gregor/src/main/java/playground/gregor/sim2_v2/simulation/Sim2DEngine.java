/* *********************************************************************** *
 * project: org.matsim.*
 * Sim2DEngine.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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
package playground.gregor.sim2_v2.simulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.Steppable;
import org.matsim.ptproject.qsim.interfaces.QSimI;
import org.matsim.ptproject.qsim.interfaces.SimEngine;

import com.vividsolutions.jts.geom.MultiPolygon;

import playground.gregor.sim2_v2.scenario.Scenario2DImpl;
import playground.gregor.sim2_v2.simulation.floor.Floor;

/**
 * @author laemmel
 * 
 */
public class Sim2DEngine implements SimEngine, Steppable {

	private List<Floor> floors;
	private PhantomManager phantomMgr;
	private final Scenario2DImpl scenario;
	private final Random random;
	private final Sim2D sim;

	private final Map<Id, Floor> linkIdFloorMapping = new HashMap<Id, Floor>();

	/**
	 * @param sim
	 * @param random
	 */
	public Sim2DEngine(Sim2D sim, Random random) {
		this.scenario = (Scenario2DImpl) sim.getScenario();
		this.random = random;
		this.sim = sim;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.core.mobsim.framework.Steppable#doSimStep(double)
	 */
	@Override
	public void doSimStep(double time) {
		if (this.phantomMgr != null) {
			this.phantomMgr.update(time);
		}

		for (Floor floor : this.floors) {
			floor.move(time);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.ptproject.qsim.interfaces.SimEngine#afterSim()
	 */
	@Override
	public void afterSim() {
		throw new RuntimeException("not (yet) implemented!");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.ptproject.qsim.interfaces.SimEngine#onPrepareSim()
	 */
	@Override
	public void onPrepareSim() {
		for (Entry<MultiPolygon, List<Link>> e : this.scenario.getFloorLinkMapping().entrySet()) {
			Floor f = new Floor(this.scenario, e.getValue());
			this.floors.add(f);
			for (Link l : e.getValue()) {
				if (this.linkIdFloorMapping.get(l.getId()) != null) {
					throw new RuntimeException("Multiple floors per link not allowed! Link with Id: " + l.getId() + " already belongs to floor" + this.linkIdFloorMapping.get(l.getId()));
				}
				this.linkIdFloorMapping.put(l.getId(), f);
			}
			f.init();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.ptproject.qsim.interfaces.SimEngine#getQSim()
	 */
	@Override
	public QSimI getQSim() {
		return this.sim;
	}

	/**
	 * @param currentLinkId
	 * @return
	 */
	public Floor getFloor(Id currentLinkId) {
		return this.linkIdFloorMapping.get(currentLinkId);
	}

}
