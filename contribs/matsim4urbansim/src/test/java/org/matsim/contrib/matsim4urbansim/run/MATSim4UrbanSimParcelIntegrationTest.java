/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.contrib.matsim4urbansim.run;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.contrib.matrixbasedptrouter.utils.CreateTestNetwork;
import org.matsim.contrib.matsim4urbansim.config.CreateTestM4UConfig;
import org.matsim.contrib.matsim4urbansim.utils.CreateTestUrbansimPopulation;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

/**
 * @author nagel
 *
 */
public class MATSim4UrbanSimParcelIntegrationTest {
	
	private static final Logger log = Logger.getLogger(MATSim4UrbanSimParcelIntegrationTest.class);
	
	@Rule public MatsimTestUtils utils = new MatsimTestUtils();
	
	
	/**
	 * This test makes sure that five csv files used as input for UrbanSim are written correctly
	 */
	@Test
	public void test() {
		String path = utils.getOutputDirectory() ;
		
		CreateTestUrbansimPopulation.createUrbanSimTestPopulation(path, 1) ;
		
		NetworkWriter writer = new NetworkWriter( CreateTestNetwork.createTestNetwork() ) ;
		final String networkFilename = path + "/network.xml.gz";
		writer.write( networkFilename);
		
		CreateTestM4UConfig creator = new CreateTestM4UConfig(path, networkFilename ) ;
		String filename = creator.generateConfigV3() ;
		
		String[] args = { filename } ;
		MATSim4UrbanSimParcel.main( args ); 
				
		final String ACCESSIBILITY_INDICATORS = "accessibility_indicators.csv" ;
		final String TRAVEL_DATA = "travel_data.csv" ;
		final String PERSONS = "persons.csv" ;
		final String ZONES = "zones.csv" ;
		final String PARCELS = "parcels.csv" ;
		
		log.info("comparing travel data ...");
		compareFiles(TRAVEL_DATA) ;
		log.info("... done.");
		log.info("comparing persons data ...");
		compareFiles(PERSONS) ;
		log.info("... done.");
		log.info("comparing zones data ...");
		compareFiles(ZONES) ;
		log.info("... done.");
		log.info("comparing parcels data ...");
		compareFiles(PARCELS) ;
		log.info("... done.");
		log.info("comparing accessibility indicators ...");
		compareFiles(ACCESSIBILITY_INDICATORS) ;
		log.info("... done.");
	}


	private void compareFiles(String fileName) {
		String originalFileName = utils.getClassInputDirectory() + fileName ;
		log.info( "old: " + originalFileName ) ;
		final long originalCheckSum = CRCChecksum.getCRCFromFile(originalFileName);
	
		String revisedFileName = utils.getOutputDirectory() + fileName ;
		log.info( "new: " + revisedFileName ) ;
		final long revisedCheckSum = CRCChecksum.getCRCFromFile(revisedFileName) ;

		if ( revisedCheckSum != originalCheckSum ) {
	
			List<String> original = fileToLines(originalFileName);
			List<String> revised  = fileToLines(revisedFileName);
	
			Patch patch = DiffUtils.diff(original, revised);
	
			for (Delta delta: patch.getDeltas()) {
				System.out.flush() ;
				System.err.println("===");
				System.err.println(delta.getOriginal());
				System.err.println(delta.getRevised());
				System.err.println("===");
				System.err.flush() ;
			}
		}
		Assert.assertEquals( originalCheckSum,  revisedCheckSum );
		log.info("done") ;
	}
	
	
	/**
	 * Helper method for get the file content
	 * taken from ConfigReadWriteOverwriteTest.java
	 */	
	private static List<String> fileToLines(String filename) {
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			in.close() ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
}