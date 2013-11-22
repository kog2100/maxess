/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,     *
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

package playground.southafrica.projects.complexNetworks.pathDependence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestUtils;

import playground.southafrica.freight.digicore.containers.DigicoreActivity;
import playground.southafrica.freight.digicore.containers.DigicoreChain;
import playground.southafrica.projects.complexNetworks.pathDependence.PathDependentNetwork.PathDependentNode;

public class PathDependentNetworkTest {
	
	@Rule 
	public MatsimTestUtils utils = new MatsimTestUtils();

	@Test
	public void testSetupListOfChains(){
		List<DigicoreChain> chains = setupListOfChains();
		Assert.assertEquals("Wrong number of chains.", 4, chains.size());
		Assert.assertEquals("Wrong chain length - chain 1.", 3, chains.get(0).getAllActivities().size());
		Assert.assertEquals("Wrong chain length - chain 2.", 3, chains.get(1).getAllActivities().size());
		Assert.assertEquals("Wrong chain length - chain 3.", 3, chains.get(2).getAllActivities().size());
		Assert.assertEquals("Wrong chain length - chain 4.", 5, chains.get(3).getAllActivities().size());
	}
	
	
	@Test
	public void testConstructorNoSeed() {
		PathDependentNetwork pdn = new PathDependentNetwork();
		Assert.assertNotNull("Should have a random number generator.", pdn.getRandom());
	}
	
	
	@Test
	public void testConstructorSeed(){
		long l = 12345;
		long next = new Random(l).nextLong();
		PathDependentNetwork pdn = new PathDependentNetwork(l);
		Assert.assertEquals("Wrong next random long value.", next, pdn.getRandom().nextLong());
	}

	
	@Test 
	public void testProcessChain(){
		List<DigicoreChain> chains = setupListOfChains();
		
		PathDependentNetwork pdn = new PathDependentNetwork(12345);
		for(DigicoreChain chain : chains){
			pdn.processActivityChain(chain);
		}
		
		Assert.assertEquals("Wrong number of nodes.", 5, pdn.getNumberOfNodes());
		
		/* Node 'A' */
		PathDependentNode A = pdn.getPathDependentNode(new IdImpl("A"));
		Assert.assertEquals("Wrong in-degree for 'A'.", 0, A.getInDegree() );
		Assert.assertEquals("Wrong out-degree for 'A'.", 1, A.getOutDegree());
		Assert.assertEquals("Wrong path-dependent out-degree for 'A'.", 1, A.getPathDependentOutDegree(null));
		Assert.assertEquals("Wrong weight '(source) A -> C'", 2, pdn.getPathDependentWeight(null, new IdImpl("A"), new IdImpl("C")), 0.001);
		Assert.assertEquals("Wrong weight '(from C) A -> C'", 0, pdn.getPathDependentWeight(new IdImpl("C"), new IdImpl("A"), new IdImpl("C")), 0.001);
		
		/* Node 'B' */
		PathDependentNode B = pdn.getPathDependentNode(new IdImpl("B"));
		Assert.assertEquals("Wrong in-degree for 'B'", 0, B.getInDegree() );
		Assert.assertEquals("Wrong out-degree for 'B'.", 1, B.getOutDegree());
		Assert.assertEquals("Wrong path-dependent out-degree for 'B'.", 1, B.getPathDependentOutDegree(null));
		Assert.assertEquals("Wrong weight '(source) B -> C'", 1, pdn.getPathDependentWeight(null, new IdImpl("B"), new IdImpl("C")), 0.001);
		Assert.assertEquals("Wrong weight '(from C) B -> C'", 0, pdn.getPathDependentWeight(new IdImpl("C"), new IdImpl("B"), new IdImpl("C")), 0.001);
		
		/* Node 'C' */
		PathDependentNode C = pdn.getPathDependentNode(new IdImpl("C"));
		Assert.assertEquals("Wrong in-degree for 'C'.", 2, C.getInDegree());
		Assert.assertEquals("Wrong path-dependent ('A') out-degree for 'C'.", 2, C.getPathDependentOutDegree(A.getId()));
		Assert.assertEquals("Wrong path-dependent ('B') out-degree for 'C'.", 1, C.getPathDependentOutDegree(B.getId()));
		Assert.assertEquals("Wrong out-degree for 'C'.", 2, C.getOutDegree());
		Assert.assertEquals("Wrong weight '(A) C -> D'", 1, pdn.getPathDependentWeight(new IdImpl("A"), new IdImpl("C"), new IdImpl("D")), 0.001);
		Assert.assertEquals("Wrong weight '(A) C -> E'", 1, pdn.getPathDependentWeight(new IdImpl("A"), new IdImpl("C"), new IdImpl("E")), 0.001);
		Assert.assertEquals("Wrong weight '(B) C -> D'", 1, pdn.getPathDependentWeight(new IdImpl("B"), new IdImpl("C"), new IdImpl("D")), 0.001);
		
		/* Node 'D' */
		PathDependentNode D = pdn.getPathDependentNode(new IdImpl("D"));
		Assert.assertEquals("Wrong in-degree for 'D'.", 1, D.getInDegree());
		Assert.assertEquals("Wrong out-degree for 'D'.", 0, D.getOutDegree());
		Assert.assertEquals("Wrong path-dependent ('C') out-degree for 'D'.", 0, D.getPathDependentOutDegree(new IdImpl("C")));
		
		/* Node 'E' */
		PathDependentNode E = pdn.getPathDependentNode(new IdImpl("E"));
		Assert.assertEquals("Wrong in-degree for 'E'.", 1, E.getInDegree());
		Assert.assertEquals("Wrong out-degree for 'E'.", 0, E.getOutDegree());
		Assert.assertEquals("Wrong path-dependent ('C') out-degree for 'E'.", 0, E.getPathDependentOutDegree(new IdImpl("C")));
		
		/* Test edge weights. */
		Assert.assertEquals("Wrong edge weight: A-B", 0, pdn.getWeight(new IdImpl("A"), new IdImpl("B")), 0.001);
		Assert.assertEquals("Wrong edge weight: A-C", 2, pdn.getWeight(new IdImpl("A"), new IdImpl("C")), 0.001);
		Assert.assertEquals("Wrong edge weight: B-C", 2, pdn.getWeight(new IdImpl("B"), new IdImpl("C")), 0.001);
		Assert.assertEquals("Wrong edge weight: C-D", 2, pdn.getWeight(new IdImpl("C"), new IdImpl("D")), 0.001);
		Assert.assertEquals("Wrong edge weight: C-E", 1, pdn.getWeight(new IdImpl("C"), new IdImpl("E")), 0.001);
	}
	
	@Test
	public void testSampleBiasedNextPathDependentNode(){
		List<DigicoreChain> chains = setupListOfChains();
		
		PathDependentNetwork pdn = new PathDependentNetwork(12345);
		for(DigicoreChain chain : chains){
			pdn.processActivityChain(chain);
		}

		PathDependentNode C = pdn.getPathDependentNode(new IdImpl("C"));
		Assert.assertEquals("Wrong next Id.", new IdImpl("D"), C.sampleBiasedNextPathDependentNode(new IdImpl("A"), 0.25));
		Assert.assertEquals("Wrong next Id.", new IdImpl("E"), C.sampleBiasedNextPathDependentNode(new IdImpl("A"), 0.75));

		Assert.assertEquals("Wrong next Id.", new IdImpl("D"), C.sampleBiasedNextPathDependentNode(new IdImpl("B"), 0.1));
		Assert.assertEquals("Wrong next Id.", new IdImpl("D"), C.sampleBiasedNextPathDependentNode(new IdImpl("B"), 0.25));
		Assert.assertEquals("Wrong next Id.", new IdImpl("D"), C.sampleBiasedNextPathDependentNode(new IdImpl("B"), 0.5));
		Assert.assertEquals("Wrong next Id.", new IdImpl("D"), C.sampleBiasedNextPathDependentNode(new IdImpl("B"), 0.75));
		Assert.assertEquals("Wrong next Id.", new IdImpl("D"), C.sampleBiasedNextPathDependentNode(new IdImpl("B"), 0.9));
		
		PathDependentNode D = pdn.getPathDependentNode(new IdImpl("D"));
		Assert.assertNull("Wrong next Id.", D.sampleBiasedNextPathDependentNode(new IdImpl("C"), 0.5));
		
		PathDependentNode E = pdn.getPathDependentNode(new IdImpl("E"));
		Assert.assertNull("Wrong next Id.", E.sampleBiasedNextPathDependentNode(new IdImpl("C"), 0.5));
	}
	
	@Test
	public void testGetSourceWeight(){
		List<DigicoreChain> chains = setupListOfChains();
		
		PathDependentNetwork pdn = new PathDependentNetwork(12345);
		for(DigicoreChain chain : chains){
			pdn.processActivityChain(chain);
		}
		
		Assert.assertEquals("Wrong source weight for 'A'.", 2, pdn.getSourceWeight(new IdImpl("A")), 0.001);
		Assert.assertEquals("Wrong source weight for 'B'.", 1, pdn.getSourceWeight(new IdImpl("B")), 0.001);
		Assert.assertEquals("Wrong source weight for 'C'.", 0, pdn.getSourceWeight(new IdImpl("C")), 0.001);
		Assert.assertEquals("Wrong source weight for 'D'.", 0, pdn.getSourceWeight(new IdImpl("D")), 0.001);
		Assert.assertEquals("Wrong source weight for 'E'.", 0, pdn.getSourceWeight(new IdImpl("E")), 0.001);
	}
	
	@Test
	public void testSampleChainStartNode(){
		List<DigicoreChain> chains = setupListOfChains();
		
		PathDependentNetwork pdn = new PathDependentNetwork(12345);
		for(DigicoreChain chain : chains){
			pdn.processActivityChain(chain);
		}
		
		Assert.assertEquals("Wrong source node: ratio A:B should be 2:1.", new IdImpl("A"), pdn.sampleChainStartNode(0.25));
		Assert.assertEquals("Wrong source node: ratio A:B should be 2:1.", new IdImpl("A"), pdn.sampleChainStartNode(0.5));
		Assert.assertEquals("Wrong source node: ratio A:B should be 2:1.", new IdImpl("A"), pdn.sampleChainStartNode(0.65));
		Assert.assertEquals("Wrong source node: ratio A:B should be 2:1.", new IdImpl("B"), pdn.sampleChainStartNode(0.67));
		Assert.assertEquals("Wrong source node: ratio A:B should be 2:1.", new IdImpl("B"), pdn.sampleChainStartNode(0.90));
	}
	
	
	/**
	 * Consider the following example: build a network on the following nodes
	 * 
	 *    (0,10)       (10,10)        (20,10)
	 *      A             D              ??
	 *           (5,5)
	 *             C    
	 *      B             E
	 *    (0,0)        (10,0)
	 *    
	 *    from the following activity chains:
	 *    
	 *      A -> C -> D
	 *      A -> C -> E
	 *      B -> C -> D
	 *      A -> ?? -> B -> C -> ??
	 *      
	 * The idea is that when at `C', given the former node was `A', it should be
	 * equally likely to choose `D' and `E'. Conversely, if the former node was
	 * `B', there should only be one likely next destination, namely `E'. 
	 */
	private List<DigicoreChain> setupListOfChains(){
		List<DigicoreChain> list = new ArrayList<DigicoreChain>();
		/* Chain A -> C -> D */
		DigicoreActivity da1_A = new DigicoreActivity("test", TimeZone.getTimeZone("GMT+2"), Locale.ENGLISH); 
		da1_A.setFacilityId(new IdImpl("A"));
		da1_A.setCoord(new CoordImpl(0, 10));
		DigicoreActivity da1_C = new DigicoreActivity("test", TimeZone.getTimeZone("GMT+2"), Locale.ENGLISH); 
		da1_C.setFacilityId(new IdImpl("C"));
		da1_C.setCoord(new CoordImpl(5, 5));
		DigicoreActivity da1_D = new DigicoreActivity("test", TimeZone.getTimeZone("GMT+2"), Locale.ENGLISH); 
		da1_D.setFacilityId(new IdImpl("D"));
		da1_D.setCoord(new CoordImpl(10, 10));
		DigicoreChain c1 = new DigicoreChain();
		c1.add(da1_A);
		c1.add(da1_C);
		c1.add(da1_D);
		list.add(c1);
		
		/* Chain A -> C -> E */
		DigicoreActivity da2_A = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da2_A.setFacilityId(new IdImpl("A"));
		da2_A.setCoord(new CoordImpl(0, 10));
		DigicoreActivity da2_C = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da2_C.setFacilityId(new IdImpl("C"));
		da2_C.setCoord(new CoordImpl(5, 5));
		DigicoreActivity da2_E = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da2_E.setFacilityId(new IdImpl("E"));
		da2_E.setCoord(new CoordImpl(10, 0));
		DigicoreChain c2 = new DigicoreChain();
		c2.add(da2_A);
		c2.add(da2_C);
		c2.add(da2_E);
		list.add(c2);
		
		/* Chain B -> C -> D */
		DigicoreActivity da3_B = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da3_B.setFacilityId(new IdImpl("B"));
		da3_B.setCoord(new CoordImpl(0, 0));
		DigicoreActivity da3_C = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da3_C.setFacilityId(new IdImpl("C"));
		da3_C.setCoord(new CoordImpl(5, 5));
		DigicoreActivity da3_D = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da3_D.setFacilityId(new IdImpl("D"));
		da3_D.setCoord(new CoordImpl(10, 10));
		DigicoreChain c3 = new DigicoreChain();
		c3.add(da3_B);
		c3.add(da3_C);
		c3.add(da3_D);
		list.add(c3);
		
		/* A -> ?? -> B -> C -> ?? */
		DigicoreActivity da4_A = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da4_A.setFacilityId(new IdImpl("A"));
		da4_A.setCoord(new CoordImpl(0, 10));
		DigicoreActivity da4_dummy1 = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da4_dummy1.setCoord(new CoordImpl(20, 10));
		DigicoreActivity da4_B = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da4_B.setFacilityId(new IdImpl("B"));
		da4_B.setCoord(new CoordImpl(0, 0));
		DigicoreActivity da4_C = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da4_C.setFacilityId(new IdImpl("C"));
		da4_C.setCoord(new CoordImpl(5, 5));
		DigicoreActivity da4_dummy2 = new DigicoreActivity("test", TimeZone.getDefault(), Locale.ENGLISH); 
		da4_dummy2.setCoord(new CoordImpl(20, 10));
		DigicoreChain c4 = new DigicoreChain();
		c4.add(da4_A);
		c4.add(da4_dummy1);
		c4.add(da4_B);
		c4.add(da4_C);
		c4.add(da4_dummy2);
		list.add(c4);
		
		return list;
	}
	
}
