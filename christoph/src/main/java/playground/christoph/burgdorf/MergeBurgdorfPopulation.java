/* *********************************************************************** *
 * project: org.matsim.*
 * MergeBurgdorfPopulation.java
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

package playground.christoph.burgdorf;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

/*
 * Merging internal and external population together.
 */
public class MergeBurgdorfPopulation {
	
	private static final Logger log = Logger.getLogger(MergeBurgdorfPopulation.class);

	private String backgroundPopulationFile = "../../matsim/mysimulations/burgdorf/input/plans_background_samstag.xml.gz";
	private String visitorPopulationFile = "../../matsim/mysimulations/burgdorf/input/plans_visitors_samstag.xml.gz";
	private String campingPopulationFile = "../../matsim/mysimulations/burgdorf/input/plans_camping_samstag.xml.gz";
	
	private String networkFile = "../../matsim/mysimulations/burgdorf/input/network_burgdorf_cut.xml.gz";
	private String outFile = "../../matsim/mysimulations/burgdorf/input/plans_merged_samstag.xml.gz";
		
	public static void main(String[] args) {
		new MergeBurgdorfPopulation(((ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig())));
	}
	
	public MergeBurgdorfPopulation(Scenario scenario) {

		int popSize = 0;
		
		log.info("Read network...");
		new MatsimNetworkReader(scenario).readFile(networkFile);
		log.info("done.");
		
		log.info("Read visitor population...");
		new MatsimPopulationReader(scenario).readFile(visitorPopulationFile);
		log.info("Found " + scenario.getPopulation().getPersons().size() + " visitor persons.");
		popSize = scenario.getPopulation().getPersons().size();
		
		log.info("Read camping population...");
		new MatsimPopulationReader(scenario).readFile(campingPopulationFile);
		log.info("Found " + (scenario.getPopulation().getPersons().size() - popSize) + " camping persons.");	
		popSize = scenario.getPopulation().getPersons().size();
		
		log.info("Read background population...");
		new MatsimPopulationReader(scenario).readFile(backgroundPopulationFile);
		log.info("Found " + (scenario.getPopulation().getPersons().size() - popSize) + " background persons.");
		
		log.info("Writing Burgdorf population to file...");
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(outFile);
		log.info("done.");
	}
}
