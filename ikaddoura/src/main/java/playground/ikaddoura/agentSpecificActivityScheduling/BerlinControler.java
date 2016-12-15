/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package playground.ikaddoura.agentSpecificActivityScheduling;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import playground.ikaddoura.analysis.detailedPersonTripAnalysis.PersonTripCongestionNoiseAnalysisMain;
import playground.ikaddoura.analysis.pngSequence2Video.MATSimVideoUtils;
import playground.ikaddoura.decongestion.Decongestion;
import playground.ikaddoura.decongestion.DecongestionConfigGroup;
import playground.ikaddoura.decongestion.DecongestionConfigGroup.TollingApproach;
import playground.ikaddoura.decongestion.data.DecongestionInfo;
import playground.ikaddoura.integrationCNE.CNEIntegration;
import playground.ikaddoura.integrationCNE.CNEIntegration.CongestionTollingApproach;

/**
* @author ikaddoura
*/

public class BerlinControler {
	
	private static final Logger log = Logger.getLogger(BerlinControler.class);
	
	private static String configFile;
	private static String outputBaseDirectory;

	private static boolean agentSpecificActivityScheduling;
	private static double activityDurationBin;
	private static double tolerance;
	
	private static PricingApproach pricingApproach;
	
	private enum PricingApproach {
		NoPricing, PID, QBPV3, QBPV9
	}
		
	public static void main(String[] args) throws IOException {

		if (args.length > 0) {
		
			configFile = args[0];		
			log.info("configFile: "+ configFile);
			
			outputBaseDirectory = args[1];
			log.info("outputBaseDirectory: "+ outputBaseDirectory);
			
			agentSpecificActivityScheduling = Boolean.parseBoolean(args[2]);
			log.info("addModifiedActivities: "+ agentSpecificActivityScheduling);

			activityDurationBin = Double.parseDouble(args[3]);
			log.info("activityDurationBin: "+ activityDurationBin);
			
			tolerance = Double.parseDouble(args[4]);
			log.info("tolerance: "+ tolerance);

			String congestionTollingApproachString = args[5];
			if (congestionTollingApproachString.equals(PricingApproach.NoPricing.toString())) {
				pricingApproach = PricingApproach.NoPricing;
			} else if (congestionTollingApproachString.equals(PricingApproach.QBPV3.toString())) {
				pricingApproach = PricingApproach.QBPV3;
			} else if (congestionTollingApproachString.equals(PricingApproach.QBPV9.toString())) {
				pricingApproach = PricingApproach.QBPV9;
			} else if (congestionTollingApproachString.equals(PricingApproach.PID.toString())) {
				pricingApproach = PricingApproach.PID;
			} else {
				throw new RuntimeException("Unknown congestion pricing approach. Aborting...");
			}
			log.info("pricingApproach: " + pricingApproach);
			 
		} else {
			
			configFile = "../../../runs-svn/berlin-dz-time/input/config.xml";
			outputBaseDirectory = "../../../runs-svn/berlin-dz-time/output/";
			
			agentSpecificActivityScheduling = true;
			activityDurationBin = 3600.;
			tolerance = 0.;
			
			pricingApproach = PricingApproach.NoPricing;
		}
		
		BerlinControler berlin = new BerlinControler();
		berlin.run();
	}

	private void run() throws IOException {
		
		Config config = ConfigUtils.loadConfig(configFile);
		
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd_HH-mm-ss");
		Date currentTime = new Date();
		String dateTime = formatter.format(currentTime);
		
		String outputDirectory = outputBaseDirectory + "perf" + config.planCalcScore().getPerforming_utils_hr()
					+ "_lateArrival" + config.planCalcScore().getLateArrival_utils_hr() + "_asas-" + String.valueOf(agentSpecificActivityScheduling) 
					+ "_actDurBin" +  activityDurationBin + "_tolerance" + tolerance + "_pricing-" + pricingApproach.toString() + "_" + dateTime + "/";
		
		config.controler().setOutputDirectory(outputDirectory);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
				
		if (agentSpecificActivityScheduling) {		
			AgentSpecificActivityScheduling aa = new AgentSpecificActivityScheduling(controler);
			aa.setActivityDurationBin(activityDurationBin);
			aa.setTolerance(tolerance);
			controler = aa.prepareControler();			
		}
				
		if (pricingApproach.toString().equals(PricingApproach.NoPricing.toString())) {
		
		} else if (pricingApproach.toString().equals(PricingApproach.PID.toString())) {
					
			double kp = 2 *
					((config.planCalcScore().getPerforming_utils_hr() - config.planCalcScore().getModes().get(TransportMode.car).getMarginalUtilityOfTraveling())
							/ config.planCalcScore().getMarginalUtilityOfMoney()) / 3600.;
			log.info("kp: " + kp);
			
			final DecongestionConfigGroup decongestionSettings = new DecongestionConfigGroup();
			decongestionSettings.setTOLLING_APPROACH(TollingApproach.PID);
			decongestionSettings.setKp(kp);
			decongestionSettings.setKi(0.);
			decongestionSettings.setKd(0.);
			decongestionSettings.setTOLL_BLEND_FACTOR(0.1);
			decongestionSettings.setMsa(true);
			decongestionSettings.setRUN_FINAL_ANALYSIS(false);
			decongestionSettings.setWRITE_LINK_INFO_CHARTS(false);
			
			final DecongestionInfo info = new DecongestionInfo(controler.getScenario(), decongestionSettings);
			final Decongestion decongestion = new Decongestion(controler, info);
			controler = decongestion.getControler();
				
		} else if (pricingApproach.toString().equals(PricingApproach.QBPV3.toString())) {
			CNEIntegration cne = new CNEIntegration(controler);
			cne.setCongestionTollingApproach(CongestionTollingApproach.QBPV3);
			cne.setCongestionPricing(true);
			controler = cne.prepareControler();
			
		} else if (pricingApproach.toString().equals(PricingApproach.QBPV9.toString())) {
			CNEIntegration cne = new CNEIntegration(controler);
			cne.setCongestionTollingApproach(CongestionTollingApproach.QBPV9);
			cne.setCongestionPricing(true);
			controler = cne.prepareControler();
		} else {
			throw new RuntimeException("Unknown congestion pricing approach. Aborting...");
		}
			
		controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
		controler.run();
		
		// analysis
		
		PersonTripCongestionNoiseAnalysisMain analysis = new PersonTripCongestionNoiseAnalysisMain(controler.getConfig().controler().getOutputDirectory());
		analysis.run();
		
		MATSimVideoUtils.createLegHistogramVideo(controler.getConfig().controler().getOutputDirectory());
	}

}
