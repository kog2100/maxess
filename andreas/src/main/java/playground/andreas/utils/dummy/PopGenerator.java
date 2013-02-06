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

package playground.andreas.utils.dummy;

import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * 
 * Creates some simple plans files in a straight forward way.
 * 
 * @author aneumann
 *
 */
public class PopGenerator {
	
	
	public static void main(String[] args) {
		
		String outputDir = "F:/temp/";
		String networkFilename = outputDir + "network_corridor.xml";
		int nPersonsPerHour = 1000;
		
		PopGenerator.createPopT1(networkFilename, nPersonsPerHour, outputDir + "pop_corr_t_1.xml.gz");
		PopGenerator.createPopT2(networkFilename, nPersonsPerHour, outputDir + "pop_corr_t_2.xml.gz");
		PopGenerator.createPopT3(networkFilename, nPersonsPerHour, outputDir + "pop_corr_t_3.xml.gz");
		PopGenerator.createPopT4(networkFilename, nPersonsPerHour, outputDir + "pop_corr_t_4.xml.gz");
		
		PopGenerator.createPopS1(networkFilename, nPersonsPerHour, outputDir + "pop_corr_s_1.xml.gz");
		PopGenerator.createPopS2(networkFilename, nPersonsPerHour, outputDir + "pop_corr_s_2.xml.gz");
		PopGenerator.createPopS3(networkFilename, nPersonsPerHour, outputDir + "pop_corr_s_3.xml.gz");
		PopGenerator.createPopS4(networkFilename, nPersonsPerHour, outputDir + "pop_corr_s_4.xml.gz");
		
		networkFilename = outputDir + "network_cross.xml";
		nPersonsPerHour = 1000;
		PopGenerator.createPopCross(networkFilename, nPersonsPerHour, outputDir + "pop_cross.xml.gz");
	}

	private static void createPopCross(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord nodeACoord = sc.getNetwork().getNodes().get(new IdImpl("A")).getCoord();
		Coord nodeBCoord = sc.getNetwork().getNodes().get(new IdImpl("B")).getCoord();
		
		// create trips from node A to node B and trips from node A to node B, 6-10
		createPersons(rnd, pop, nPersonsPerHour, nodeACoord, nodeBCoord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, nodeBCoord, nodeACoord, 6, 10);
		
		Coord nodeCCoord = sc.getNetwork().getNodes().get(new IdImpl("C")).getCoord();
		Coord nodeDCoord = sc.getNetwork().getNodes().get(new IdImpl("D")).getCoord();
		
		// create trips from node C to node D and trips from node C to node D, 6-10
		createPersons(rnd, pop, nPersonsPerHour, nodeCCoord, nodeDCoord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, nodeDCoord, nodeCCoord, 6, 10);
		
		// create trips from node A to node C and trips from node C to node A, 6-10
		createPersons(rnd, pop, nPersonsPerHour / 10, nodeACoord, nodeCCoord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour / 10, nodeCCoord, nodeACoord, 6, 10);
						
		new PopulationWriter(pop, null).write(outFilename);		
	}

	private static void createPopT1(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 6 and trips from node 6 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 6, 10);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopT2(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 6 and trips from node 6 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 6, 10);
		
		// create additional trips from node 2 to node 6 and trips from node 6 to node 2, 16-20
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 16, 20);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 16, 20);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopT3(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 6 and trips from node 6 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 6, 10);
		
		// create additional trips from node 2 to node 6 and trips from node 6 to node 2, 16-20
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 16, 20);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 16, 20);
		
		// create additional trips from node 2 to node 6 and trips from node 6 to node 2, 6-20
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 6, 20);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 6, 20);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopT4(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 6 and trips from node 6 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 6, 10);
		
		// create additional trips from node 2 to node 6 and trips from node 6 to node 2, 16-20
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 16, 20);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 16, 20);
		
		// create additional trips from node 2 to node 6 and trips from node 6 to node 2, 6-16
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node6Coord, 6, 16);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node2Coord, 6, 16);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopS1(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node3Coord = sc.getNetwork().getNodes().get(new IdImpl(3)).getCoord();
		
		// create trips from node 2 to node 3 and trips from node 3 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node3Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node3Coord, node2Coord, 6, 10);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopS2(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node3Coord = sc.getNetwork().getNodes().get(new IdImpl(3)).getCoord();
		Coord node5Coord = sc.getNetwork().getNodes().get(new IdImpl(5)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 3 and trips from node 3 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node3Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node3Coord, node2Coord, 6, 10);
		
		// create additional trips from node 5 to node 6 and trips from node 6 to node 5, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node5Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node5Coord, 6, 10);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopS3(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node3Coord = sc.getNetwork().getNodes().get(new IdImpl(3)).getCoord();
		Coord node5Coord = sc.getNetwork().getNodes().get(new IdImpl(5)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 3 and trips from node 3 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node3Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node3Coord, node2Coord, 6, 10);
		
		// create additional trips from node 5 to node 6 and trips from node 6 to node 5, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node5Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node5Coord, 6, 10);
		
		// create additional trips from node 3 to node 5 and trips from node 5 to node 3, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node3Coord, node5Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node5Coord, node3Coord, 6, 10);
						
		new PopulationWriter(pop, null).write(outFilename);
	}
	
	private static void createPopS4(String networkFilename, int nPersonsPerHour, String outFilename) {
		Scenario sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(networkFilename);
		Population pop = sc.getPopulation();
		
		MatsimRandom.reset(4711);
		Random rnd = MatsimRandom.getLocalInstance();
		
		Coord node2Coord = sc.getNetwork().getNodes().get(new IdImpl(2)).getCoord();
		Coord node3Coord = sc.getNetwork().getNodes().get(new IdImpl(3)).getCoord();
		Coord node5Coord = sc.getNetwork().getNodes().get(new IdImpl(5)).getCoord();
		Coord node6Coord = sc.getNetwork().getNodes().get(new IdImpl(6)).getCoord();
		
		// create trips from node 2 to node 3 and trips from node 3 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node3Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node3Coord, node2Coord, 6, 10);
		
		// create additional trips from node 5 to node 6 and trips from node 6 to node 5, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node5Coord, node6Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node6Coord, node5Coord, 6, 10);
		
		// create additional trips from node 2 to node 5 and trips from node 5 to node 2, 6-10
		createPersons(rnd, pop, nPersonsPerHour, node2Coord, node5Coord, 6, 10);
		createPersons(rnd, pop, nPersonsPerHour, node5Coord, node2Coord, 6, 10);
						
		new PopulationWriter(pop, null).write(outFilename);
	}

	private static void createPersons(Random rnd, Population pop, int nPersonsPerHour, Coord fromCoord, Coord toCoord, int departureIntervalStart, int departureIntervalEnd) {
		int nPersonsCreated = pop.getPersons().size();
		int nPersonsToBeCreated = (departureIntervalEnd - departureIntervalStart) * nPersonsPerHour;
		
		for (int i = 0; i < nPersonsToBeCreated; i++) {
			nPersonsCreated++;
			Person person = pop.getFactory().createPerson(new IdImpl(nPersonsCreated));
			Plan plan = pop.getFactory().createPlan();
			
			Activity h = pop.getFactory().createActivityFromCoord("h", fromCoord);
			h.setEndTime(departureIntervalStart * 3600.0 + rnd.nextDouble() * (departureIntervalEnd - departureIntervalStart) * 3600.0);
			plan.addActivity(h);

			Leg leg = pop.getFactory().createLeg("pt");
			plan.addLeg(leg);

			Activity w = pop.getFactory().createActivityFromCoord("w", toCoord);
			plan.addActivity(w);

			person.addPlan(plan);
			pop.addPerson(person);
		}
	}
}
