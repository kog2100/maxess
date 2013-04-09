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

package playground.michalm.demand.poznan;

import java.io.*;
import java.util.*;

import javax.naming.ConfigurationException;
import javax.xml.parsers.ParserConfigurationException;

import org.matsim.api.core.v01.*;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.xml.sax.SAXException;

import pl.poznan.put.util.random.*;
import playground.michalm.demand.*;
import playground.michalm.demand.DefaultActivityGenerator.GeometryProvider;
import playground.michalm.demand.DefaultActivityGenerator.PointAcceptor;
import playground.michalm.util.visum.VisumODMatrixReader;
import playground.michalm.vrp.run.VrpConfigUtils;

import com.vividsolutions.jts.geom.*;


public class PoznanLanduseDemandGeneration
{
    private static enum ActivityType
    {
        HOME("H"), WORK("W"), EDUCATION("E"), SHOPPING("S"), OTHER("O");

        private final String id;


        private ActivityType(String id)
        {
            this.id = id;
        }
    }


    private static enum ActivityPair
    {
        D_I(ActivityType.HOME, ActivityType.OTHER), //
        D_N(ActivityType.HOME, ActivityType.EDUCATION), //
        D_P(ActivityType.HOME, ActivityType.WORK), //
        D_Z(ActivityType.HOME, ActivityType.SHOPPING), //
        I_D(ActivityType.OTHER, ActivityType.HOME), //
        N_D(ActivityType.EDUCATION, ActivityType.HOME), //
        P_D(ActivityType.WORK, ActivityType.HOME), //
        Z_D(ActivityType.SHOPPING, ActivityType.HOME), //
        NZD(ActivityType.OTHER, ActivityType.OTHER);//
        // actually, O_O is not Other-Other but notHome-notHome
        // However, for PoznanLanduseDemandGeneration generator this simplification is OK
        // because location of Other is not constrained in any way

        private final ActivityType from, to;


        private ActivityPair(ActivityType from, ActivityType to)
        {
            this.from = from;
            this.to = to;
        }
    }


    private static final double RESIDENTIAL_TO_INDUSTRIAL_WORK = 0.33;
    private static final double RESIDENTIAL_TO_SHOP_SHOPPING = 0.1;


    public static class LanduseLocationGeneratorStrategy
        implements GeometryProvider, PointAcceptor
    {
        private final Map<String, Map<Id, WeightedRandomSelection<Geometry>>> selectionByZoneByActType;
        private final Map<Id, List<Geometry>> forestByZone;
        private final Set<Id> validatedZones;


        public LanduseLocationGeneratorStrategy(
                Map<String, Map<Id, WeightedRandomSelection<Geometry>>> selectionByZoneByActType,
                Map<Id, List<Geometry>> forestByZone, Set<Id> validatedZones)
        {
            this.selectionByZoneByActType = selectionByZoneByActType;
            this.forestByZone = forestByZone;
            this.validatedZones = validatedZones;
        }


        @Override
        public Geometry getGeometry(Zone zone, String actType)
        {
            if (ActivityType.valueOf(actType) != ActivityType.OTHER
                    && validatedZones.contains(zone.getId())) {
                return selectionByZoneByActType.get(actType).get(zone.getId()).select();
            }

            return (Geometry)zone.getZonePolygon().getDefaultGeometry();
        }


        @Override
        public boolean acceptPoint(Zone zone, String actType, Point point)
        {
            if (ActivityType.valueOf(actType) == ActivityType.OTHER) {
                return true;
            }

            for (Geometry g : forestByZone.get(zone.getId())) {
                if (g.contains(point)) {
                    return false; // inside forest
                }
            }

            return true;// outside forest
        }
    }


    private Scenario scenario;
    private Map<Id, Zone> zones;
    private Set<Id> validatedZones;
    private EnumMap<ActivityPair, Double> put2PrtRatios;
    private Map<String, Map<Id, WeightedRandomSelection<Geometry>>> selectionByZoneByActType;
    private ODDemandGenerator dg;


    public void generate()
        throws ConfigurationException, SAXException, ParserConfigurationException, IOException
    {
        String dirName = "D:\\eTaxi\\Poznan_MATSim\\";
        String networkFile = dirName + "network.xml";
        String zonesXmlFile = dirName + "zones.xml";
        String zonesShpFile = dirName + "GIS\\zones_with_no_zone.SHP";

        String forestShpFile = dirName + "GIS\\forests.SHP";
        String industrialShpFile = dirName + "GIS\\industrial.SHP";
        String residentialShpFile = dirName + "GIS\\residential.SHP";
        String schoolShpFile = dirName + "GIS\\school.SHP";
        String shopShpFile = dirName + "GIS\\shop.SHP";

        String validatedZonesFile = dirName + "GIS\\zones_with_correct_landuse";

        String put2PrtRatiosFile = dirName + "PuT_PrT_ratios";

        String odMatrixFilePrefix = dirName + "odMatricesByType\\";
        String plansFile = dirName + "plans.xml.gz";
        String idField = "NO";
        int randomSeed = RandomUtils.DEFAULT_SEED;

        RandomUtils.reset(randomSeed);

        scenario = ScenarioUtils.createScenario(VrpConfigUtils.createConfig());
        new MatsimNetworkReader(scenario).readFile(networkFile);
        zones = Zone.readZones(scenario, zonesXmlFile, zonesShpFile, idField);

        readValidatedZones(validatedZonesFile);

        System.out.println("getLanduseByZone(forestShpFile)");
        Map<Id, List<Geometry>> forestByZone = getLanduseByZone(forestShpFile);
        System.out.println("getLanduseByZone(industrialShpFile)");
        Map<Id, List<Geometry>> industrialByZone = getLanduseByZone(industrialShpFile);
        System.out.println("getLanduseByZone(residentialShpFile)");
        Map<Id, List<Geometry>> residentialByZone = getLanduseByZone(residentialShpFile);
        System.out.println("getLanduseByZone(schoolShpFile)");
        Map<Id, List<Geometry>> schoolByZone = getLanduseByZone(schoolShpFile);
        System.out.println("getLanduseByZone(shopShpFile)");
        Map<Id, List<Geometry>> shopByZone = getLanduseByZone(shopShpFile);

        selectionByZoneByActType = new HashMap<String, Map<Id, WeightedRandomSelection<Geometry>>>();

        Map<Id, WeightedRandomSelection<Geometry>> homeSelectionByZone = initSelectionByZone(ActivityType.HOME);
        Map<Id, WeightedRandomSelection<Geometry>> workSelectionByZone = initSelectionByZone(ActivityType.WORK);
        Map<Id, WeightedRandomSelection<Geometry>> educationSelectionByZone = initSelectionByZone(ActivityType.EDUCATION);
        Map<Id, WeightedRandomSelection<Geometry>> shoppingSelectionByZone = initSelectionByZone(ActivityType.SHOPPING);

        for (Id zoneId : validatedZones) {
            WeightedRandomSelection<Geometry> homeSelection = initSelection(zoneId,
                    homeSelectionByZone);
            WeightedRandomSelection<Geometry> workSelection = initSelection(zoneId,
                    workSelectionByZone);
            WeightedRandomSelection<Geometry> educationSelection = initSelection(zoneId,
                    educationSelectionByZone);
            WeightedRandomSelection<Geometry> shoppingSelection = initSelection(zoneId,
                    shoppingSelectionByZone);

            for (Geometry g : industrialByZone.get(zoneId)) {
                workSelection.add(g, g.getArea());
            }

            for (Geometry g : residentialByZone.get(zoneId)) {
                double area = g.getArea();
                homeSelection.add(g, area);
                workSelection.add(g, RESIDENTIAL_TO_INDUSTRIAL_WORK * area);
                shoppingSelection.add(g, RESIDENTIAL_TO_SHOP_SHOPPING * area);
            }

            for (Geometry g : schoolByZone.get(zoneId)) {
                educationSelection.add(g, g.getArea());
            }

            for (Geometry g : shopByZone.get(zoneId)) {
                shoppingSelection.add(g, g.getArea());
            }
        }

        readPut2PrtRatios(put2PrtRatiosFile);

        LanduseLocationGeneratorStrategy strategy = new LanduseLocationGeneratorStrategy(
                selectionByZoneByActType, forestByZone, validatedZones);
        ActivityGenerator lg = new DefaultActivityGenerator(scenario, strategy, strategy);
        dg = new ODDemandGenerator(scenario, lg, zones);

        for (ActivityPair ap : ActivityPair.values()) {
            generate(odMatrixFilePrefix, ap);
        }

        dg.write(plansFile);
        // dg.writeTaxiCustomers(taxiFile);
    }


    private void generate(String filePrefix, ActivityPair actPair)
        throws FileNotFoundException
    {
        double flowCoeff = put2PrtRatios.get(actPair);
        String actTypeFrom = actPair.from.id;
        String actTypeTo = actPair.to.id;

        for (int i = 0; i < 24; i++) {
            String odMatrixFile = filePrefix + actPair.name() + "_" + i + "-" + (i + 1);
            System.out.println("Generation for " + odMatrixFile);
            double[][] odMatrix = VisumODMatrixReader.readMatrixFile(new File(odMatrixFile));
            dg.generateSinglePeriod(odMatrix, actTypeFrom, actTypeTo, 1, flowCoeff, 0, i * 3600);
        }
    }


    private void readValidatedZones(String validatedZonesFile)
        throws FileNotFoundException
    {
        validatedZones = new HashSet<Id>();
        Scanner scanner = new Scanner(new File(validatedZonesFile));

        while (scanner.hasNext()) {
            validatedZones.add(scenario.createId(scanner.next()));
        }

        scanner.close();
    }


    private void readPut2PrtRatios(String put2PrtRatiosFile)
        throws FileNotFoundException
    {
        put2PrtRatios = new EnumMap<ActivityPair, Double>(ActivityPair.class);
        Scanner scanner = new Scanner(new File(put2PrtRatiosFile));

        while (scanner.hasNext()) {
            ActivityPair pair = ActivityPair.valueOf(scanner.next());
            double putShare = scanner.nextDouble();
            double prtShare = scanner.nextDouble();
            double ratio = putShare / (putShare + prtShare);

            if (put2PrtRatios.put(pair, ratio) == null) {
                throw new RuntimeException("Pair respecified: " + pair);
            }
        }

        scanner.close();

        if (put2PrtRatios.size() != ActivityPair.values().length) {
            throw new RuntimeException("Not all pairs have been specified");
        }
    }


    private WeightedRandomSelection<Geometry> initSelection(Id zoneId,
            Map<Id, WeightedRandomSelection<Geometry>> selectionByZone)
    {
        WeightedRandomSelection<Geometry> selection = new WeightedRandomSelection<Geometry>();
        selectionByZone.put(zoneId, selection);
        return selection;
    }


    private Map<Id, WeightedRandomSelection<Geometry>> initSelectionByZone(ActivityType actType)
    {
        Map<Id, WeightedRandomSelection<Geometry>> selectionByZone = new HashMap<Id, WeightedRandomSelection<Geometry>>();
        selectionByZoneByActType.put(actType.id, selectionByZone);
        return selectionByZone;
    }


    private Map<Id, List<Geometry>> getLanduseByZone(String shpFile)
    {
        return LanduseByZoneUtils.buildMap(zones, ShapeFileReader.getAllFeatures(shpFile));
    }


    public static void main(String[] args)
        throws ConfigurationException, IOException, SAXException, ParserConfigurationException
    {
        new PoznanLanduseDemandGeneration().generate();
    }
}
