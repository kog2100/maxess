package playground.sergioo.passivePlanning2012.core.population.decisionMakers.types;

import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.collections.Tuple;

public interface TypeOfActivityFacilityDecisionMaker extends DecisionMaker {

	//Methods
	public Tuple<String, Id> decideTypeOfActivityFacility(double time, Id startFacilityId);

}
