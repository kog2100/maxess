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

package playground.johannes.gsv.synPop.sim;

import org.apache.log4j.Logger;

import playground.johannes.gsv.synPop.ProxyPerson;
import playground.johannes.gsv.synPop.io.XMLWriter;

/**
 * @author johannes
 *
 */
public class PopulationWriter implements SamplerListener {
	
	private static final Logger logger = Logger.getLogger(PopulationWriter.class);

	private Sampler sampler;
	
	private String outputDir;
	
	private XMLWriter writer;
	
	private int dumpInterval = 100000;
	
	private int iteration = 0;
	
	public PopulationWriter(String outputDir, Sampler sampler) {
		this.outputDir = outputDir;
		writer = new XMLWriter();
		this.sampler = sampler;
	}
	
	public void setDumpInterval(int interval) {
		dumpInterval = interval;
	}
	
	@Override
	public void afterStep(ProxyPerson original, ProxyPerson mutation, boolean accepted) {
		iteration++;
		if(iteration % dumpInterval == 0) {
			logger.info("Dumping population...");
			writer.write(String.format("%s/%s.pop.xml", outputDir, iteration), sampler.getPopulation());
		}

	}

}