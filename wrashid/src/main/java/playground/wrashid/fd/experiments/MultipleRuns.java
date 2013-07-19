package playground.wrashid.fd.experiments;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class MultipleRuns {

	public static void main(String[] args) {
		
		int runId=0;
		
		
		for (int binSizeInSeconds=30;binSizeInSeconds<300;binSizeInSeconds+=30){
			for (int initialAgents=300;initialAgents<1000;initialAgents+=100){
				for (int agentIncrementPerHour=10;agentIncrementPerHour<200;agentIncrementPerHour+=20){
					System.out.println("runId: " +runId + ", binSizeInSeconds: " + binSizeInSeconds + ", initialAgents:" + initialAgents + ", agentIncrementPerHour: " + agentIncrementPerHour);
					Config config = ConfigUtils.createConfig();
					Scenario scenario = ScenarioUtils.createScenario(config);
					MiniScenarioDualSim.createNetwork(scenario);
					MiniScenarioDualSim.createPopulation(scenario,initialAgents,agentIncrementPerHour);
					MiniScenarioDualSim.runSimulation(scenario,binSizeInSeconds,runId);
					
					runId++;
				}
			}
		}
		
	}
	
}
