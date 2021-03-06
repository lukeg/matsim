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

package playground.dziemke.cemdapMatsimCadyts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


/**
 * @author dziemke
 */
public class ZoneAndLOSGeneratorV2 {
	private static final Logger LOG = Logger.getLogger(ZoneAndLOSGeneratorV2.class);

	
	//
	private Set<String> municipalities = new HashSet<>();
	//

	private Map <Integer, Zone> zonesMap = new HashMap <Integer, Zone>();
	private Map <String, Zone2Zone> zone2ZoneMap = new HashMap <String, Zone2Zone>();
	
//	private String shapeFile = new String("D:/Workspace/container/demand/input/shapefiles/gemeindenLOR_DHDN_GK4.shp");
//	
//	private String outputFileZone2Zone = new String("D:/Workspace/container/demand/input/cemdap_berlin/18/zone2zone.dat");
//	private String outputFileZones = new String("D:/Workspace/container/demand/input/cemdap_berlin/18/zones.dat");
//	private String outputFileLosOffPkAM = new String("D:/Workspace/container/demand/input/cemdap_berlin/18/losoffpkam.dat");
	
	private String shapeFile = "../../../shared-svn/projects/cemdapMatsimCadyts/scenario/shapefiles/gemeindenLOR_DHDN_GK4.shp";
//	private String shapeFile = "../../../shared-svn/projects/cemdapMatsimCadyts/scenario/shapefiles/gemeinden.shp";
//	private String shapeFile = "../../../shared-svn/projects/cemdapMatsimCadyts/scenario/shapefiles/Bezirksregion_EPSG_25833.shp";
	
	private String outputFileZone2Zone = "../../../shared-svn/projects/cemdapMatsimCadyts/scenario/cemdap_berlin/2016-11-29/zone2zone.dat";
	private String outputFileZones = "../../../shared-svn/projects/cemdapMatsimCadyts/scenario/cemdap_berlin/2016-11-29/zones.dat";
	private String outputFileLosOffPkAM = "../../../shared-svn/projects/cemdapMatsimCadyts/scenario/cemdap_berlin/2016-11-29/losoffpkam.dat";
	
//	private String outputBase;
	
	
	public static void main(String[] args) {
		
		String commuterFileOutgoing1 = "../../../shared-svn/studies/countries/de/berlin_scenario_2016/input/pendlerstatistik_2009/Berlin_2009/B2009Ga.txt";
		String commuterFileOutgoing2 = "../../../shared-svn/studies/countries/de/berlin_scenario_2016/input/pendlerstatistik_2009/Brandenburg_2009/Teil1BR2009Ga.txt";
		String commuterFileOutgoing3 = "../../../shared-svn/studies/countries/de/berlin_scenario_2016/input/pendlerstatistik_2009/Brandenburg_2009/Teil2BR2009Ga.txt";
		String commuterFileOutgoing4 = "../../../shared-svn/studies/countries/de/berlin_scenario_2016/input/pendlerstatistik_2009/Brandenburg_2009/Teil3BR2009Ga.txt";
		
		String[] commuterFilesOutgoing = {commuterFileOutgoing1, commuterFileOutgoing2, commuterFileOutgoing3, commuterFileOutgoing4};
		ZoneAndLOSGeneratorV2 zoneFilesGenerator = new ZoneAndLOSGeneratorV2(commuterFilesOutgoing);
//		zoneFilesGenerator.run();
	}

	
	public ZoneAndLOSGeneratorV2(String[] commuterFilesOutgoing) {
//		LogToOutputSaver.setOutputDirectory(outputBase);
		readMunicipalities(commuterFilesOutgoing);
		readShape();
		compare();
		generateZone2Zone();
		writeZone2ZoneFile();
		writeZonesFile();
		writeLosFile();
	}
	
	
	private void readMunicipalities(String[] commuterFilesOutgoing) {
		for (String commuterFileOutgoing : commuterFilesOutgoing) {
			CommuterFileReaderV2 commuterFileReader = new CommuterFileReaderV2(commuterFileOutgoing, "\t");
			Set<String> currentMunicipalities = commuterFileReader.getMunicipalities();
			this.municipalities.addAll(currentMunicipalities);
		}
	}

	
	private void readShape() {
		Collection <SimpleFeature> features = ShapeFileReader.getAllFeatures(this.shapeFile);
		for (SimpleFeature feature : features) {
			Integer key = Integer.parseInt((String) feature.getAttribute("NR"));
//			Integer key = Integer.parseInt((String) feature.getAttribute("SCHLUESSEL"));
			
			Geometry geo = (Geometry) feature.getDefaultGeometry();
			
			// Point point = getRandomPointInFeature(new Random(), geo);
			// switched to centroid instead of a random point before producing "cemdap_berlin/16" (on 2013-10-14)
			// 1) makes more sense, 2) eliminates randomness
			Point point = geo.getCentroid();
			Coordinate coordinate = point.getCoordinate();
			Double xcoordinate = coordinate.x;
			Double ycoordinate = coordinate.y;
			Coord coord = new Coord(Double.parseDouble(xcoordinate.toString()), Double.parseDouble(ycoordinate.toString()));
			Zone zone = new Zone(key, coord);
			zonesMap.put(key, zone);
		}
	}
	
	private void compare() {
		LOG.error("########## Municipality set has " + this.municipalities.size() + " elements.");
		LOG.error("########## Zones set has " + this.zonesMap.size() + " elements.");
		
		for (Integer key : zonesMap.keySet()) {
			if (!this.municipalities.contains(key.toString())) {
				LOG.error("Zone from shapefile not in commuter relations; zone = " + key);
			}
		}
		
		for (String key : municipalities) {
			if (!this.zonesMap.keySet().contains(Integer.parseInt(key))) {
				LOG.error("Zone from commuter relations not in shapes; zone = " + key);
			}
		}
	}
	
	
	private void generateZone2Zone() {
		System.out.println("======================" + "\n"
				   + "Start generating zone2zone" + "\n"
				   + "======================" + "\n");
		
		List <Integer> keys = new ArrayList <Integer> (this.zonesMap.keySet());
		
		for (Integer keySource : keys) {
			for (Integer keySink : keys) {
				
				double distance = 0.0;
				double temp = 0.0;
				
				if (keySource == keySink) {
					// placeholder value; later use something more meaningful
					//TODO needs conceptual revision 
					distance = 1.72;
					//distance = 3.0;
				} else {
					// Coordinates appear to be in "DHDN_GK4"
					// has Rechts- and Hochwert in meter
					// this should make calculating the beeline distanace easier
					Coord sourceCoord = this.zonesMap.get(keySource).getCoord();
					double sourceXCoord = sourceCoord.getX();
					double sourceYCoord = sourceCoord.getY();
					
					Coord sinkCoord = this.zonesMap.get(keySink).getCoord();
					double sinkXCoord = sinkCoord.getX();
					double sinkYCoord = sinkCoord.getY();
					
					double distanceX = Math.abs(sourceXCoord - sinkXCoord);
					double distanceY = Math.abs(sourceYCoord - sinkYCoord);
					
					double distanceInMeter = Math.sqrt(distanceX * distanceX + distanceY * distanceY);
					
					// input needs to be in miles
					distance = distanceInMeter / 1609.344;
					
					// round to two decimal places
					temp = distance * 100;
    				temp = Math.round(temp);
    				distance = temp / 100;
				}
				
				Zone2Zone zone2Zone = new Zone2Zone(keySource, keySink, distance);
				this.zone2ZoneMap.put(keySource + "_" + keySink, zone2Zone);
    		}
		}
	}
		
	
	public void writeZone2ZoneFile() {
		BufferedWriter bufferedWriterZone2Zone = null;
		
		try {
            File zone2ZoneFile = new File(this.outputFileZone2Zone);
    		FileWriter fileWriterZone2Zone = new FileWriter(zone2ZoneFile);
    		bufferedWriterZone2Zone = new BufferedWriter(fileWriterZone2Zone);
    		
    		for (Integer keySource : this.zonesMap.keySet()) {
    			for (Integer keySink : this.zonesMap.keySet()) {
    				
    				int source = this.zone2ZoneMap.get(keySource + "_" + keySink).getSource();
    				int sink = this.zone2ZoneMap.get(keySource + "_" + keySink).getSink();
    				double distance = this.zone2ZoneMap.get(keySource + "_" + keySink).getDistance();
    				
    				// altogether this creates 4 columns = number in query file
    				bufferedWriterZone2Zone.write(source + "\t" + sink + "\t" + 0  + "\t" + distance );
        			bufferedWriterZone2Zone.newLine();
        		}
    		}
                        
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedWriter
            try {
                if (bufferedWriterZone2Zone != null) {
                    bufferedWriterZone2Zone.flush();
                    bufferedWriterZone2Zone.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		System.out.println("Zone2Zone geschrieben.");
    }
	
	
	public void writeZonesFile() {
		BufferedWriter bufferedWriterZones = null;
		
		try {
            File zonesFile = new File(this.outputFileZones);
    		FileWriter fileWriterZones = new FileWriter(zonesFile);
    		bufferedWriterZones = new BufferedWriter(fileWriterZones);
    		
    		for (Integer keySource : this.zonesMap.keySet()) {
    			
    			// altogether this creates 45 columns = number in query file
    			bufferedWriterZones.write(keySource + "\t" + 0 + "\t" + 0  + "\t" + 0
    					+ "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0
    					+ "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0
    					+ "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0
    					+ "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0
    					+ "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0);
    			bufferedWriterZones.newLine();
    		}
                        
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedWriter
            try {
                if (bufferedWriterZones != null) {
                    bufferedWriterZones.flush();
                    bufferedWriterZones.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		System.out.println("Zones geschrieben.");
    }
	
	
	public void writeLosFile() {
		BufferedWriter bufferedWriterLos = null;
		
		try {
            File losFile = new File(this.outputFileLosOffPkAM);
            FileWriter fileWriterLos = new FileWriter(losFile);
    		bufferedWriterLos = new BufferedWriter(fileWriterLos);
    		
    		double temp = 0.0;
    		
    		for (Integer keySource : this.zonesMap.keySet()) {
    			for (Integer keySink : this.zonesMap.keySet()) {
    				int source = this.zone2ZoneMap.get(keySource + "_" + keySink).getSource();
    				int sink = this.zone2ZoneMap.get(keySource + "_" + keySink).getSink();
    				
    				int inSameZone = 0;
    				if (keySource == keySink) {
    					inSameZone = 1;
    				}
    				
    				double distance = this.zone2ZoneMap.get(keySource + "_" + keySink).getDistance();
    				
    				//TODO need conceptual revision
    				double driveAloneIVTT = distance * 1.2;
    				// round to two decimal places
					temp = driveAloneIVTT * 100;
    				temp = Math.round(temp);
    				driveAloneIVTT = temp / 100;
    				
    				//TODO needs conceptual revision
    				double driveAloneCost = distance / 15.0;
    				// round to two decimal places
					temp = driveAloneCost * 100;
    				temp = Math.round(temp);
    				driveAloneCost = temp / 100;
    				
    				// altogether this creates 14 columns = number in query file
    				bufferedWriterLos.write(source + "\t" + sink + "\t" + inSameZone  + "\t" + 0
    						+ "\t" + distance + "\t" + driveAloneIVTT + "\t" + 3.1
    						+ "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + 0 + "\t" + driveAloneCost
    						+ "\t" + driveAloneIVTT + "\t" + driveAloneCost);
        			bufferedWriterLos.newLine();
        		}
    		}
    	} catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedWriter
            try {
                if (bufferedWriterLos != null) {
                    bufferedWriterLos.flush();
                    bufferedWriterLos.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		System.out.println("LOS geschrieben.");
	}
	
}
