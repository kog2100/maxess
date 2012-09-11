/* *********************************************************************** *
 * project: org.matsim.*
 * VisumLinksRowHandle.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package playground.acmarmol.matsim2030.network;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.visum.VisumNetwork;
import org.matsim.visum.VisumNetwork.EdgeType;

import playground.mzilske.bvg09.VisumNetworkRowHandler;

public class MyVisumLinksRowHandler implements VisumNetworkRowHandler {


	private NetworkImpl network;
	private MyVisumNetwork visumNetwork;
	private int language;
	
	private final String[] NUMBER = {"NO", "NR"};
	private final String[] FROMNODENO = {"FROMNODENO", "VONKNOTNR"};
	private final String[] TONODENO = {"TONODENO", "NACHKNOTNR"};
	private final String[] TYPENO = {"TYPENO", "TYPNR"};
	private final String[] TSYSSET = {"TSYSSET", "VSYSSET"};
	private final String[] LENGTH = {"LENGTH", "LAENGE"};
	private final String[] CAPACITY = {"CAPRT", "KAPIV"};
	private final String[] FREESPEED = {"V0PRT", "V0IV"};
	
	
	public MyVisumLinksRowHandler(NetworkImpl network, MyVisumNetwork visumNetwork) {
		this.network = network;
		this.visumNetwork = visumNetwork;
		this.language = visumNetwork.getLanguage();
	}

	@Override
	public void handleRow(Map<String, String> row) {
		String nr = row.get(NUMBER[this.language]);
		IdImpl id = new IdImpl(nr);
		IdImpl fromNodeId = new IdImpl(row.get(FROMNODENO[this.language]));
		IdImpl toNodeId = new IdImpl(row.get(TONODENO[this.language]));
		Node fromNode = network.getNodes().get(fromNodeId);
		Node toNode = network.getNodes().get(toNodeId);
		Link lastEdge = network.getLinks().get(id);
		if (lastEdge != null) {
			if (lastEdge.getFromNode().getId().equals(toNodeId)
					&& lastEdge.getToNode().getId().equals(fromNodeId)) {
				id = new IdImpl(nr + 'R');
			} else {
				throw new RuntimeException("Duplicate edge.");
			}
		}
		
		double length = Double.parseDouble(row.get(LENGTH[this.language]).replace(',', '.').substring(0, row.get(LENGTH[this.language]).indexOf('k'))) * 1000;
		// double freespeed = 0.0;
		String edgeTypeIdString = row.get(TYPENO[this.language]);
		IdImpl edgeTypeId = new IdImpl(edgeTypeIdString);

		EdgeType edgeType = visumNetwork.edgeTypes.get(edgeTypeId);
		// double capacity = getCapacity(edgeTypeId);

		String VSYSSET_String = row.get(TSYSSET[this.language]);
		String[] vsyss = VSYSSET_String.split(",");
		Set<String> modes = new TreeSet<String>();
		String mode = null;
	
		for (String vsys : vsyss) {
			try{mode = visumNetwork.getModeTypes().get(new IdImpl(vsys)).name;
				modes.add(mode);}
			catch(java.lang.NullPointerException e){}
		}

		double capacity = Double.parseDouble(row.get(CAPACITY[this.language]));

		
		// edgeType.kapIV
				//);

		// // kick out all irrelevant edge types
		// if (isEdgeTypeRelevant(edgeTypeId)) {
		// // take all edges in detailed area
		// if(isEdgeInDetailedArea(fromNode, featuresInShape)){
		//
		// if(innerCity30to40KmhIdsNeedingVmaxChange.contains(edgeTypeIdString)){
		// freespeed = getFreespeedTravelTime(edgeTypeId) / 2;
		// }
		// if(innerCity45to60KmhIdsNeedingVmaxChange.contains(edgeTypeIdString)){
		// freespeed = getFreespeedTravelTime(edgeTypeId) / 1.5;
		// }
		// else{
		// freespeed = getFreespeedTravelTime(edgeTypeId);
		// }
		// network.createAndAddLink(id, fromNode, toNode, length, freespeed,
		// capacity, 1, null, edgeTypeIdString);
		// usedIds.add(edgeTypeIdString);
		// }
		// // kick out all edges in periphery that are irrelevant only there
		// else {
		// if(isEdgeTypeRelevantForPeriphery(edgeTypeId)){
		// freespeed = getFreespeedTravelTime(edgeTypeId);
		
		double freespeed = Double.parseDouble(row.get(FREESPEED[this.language]).substring(0, row.get(FREESPEED[this.language]).indexOf('k'))
		// edgeType.v0IV
				) / 3.6;


		network.createAndAddLink(id, fromNode, toNode, length, freespeed,
				capacity, 1, null, edgeTypeIdString);
		
		//modes.clear();
		modes.add(TransportMode.car);

		this.network.getLinks().get(id).setAllowedModes(modes);
		// usedIds.add(edgeTypeIdString);
		// }
		// }

		// }
	}
}
