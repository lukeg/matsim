/* *********************************************************************** *
 * project: org.matsim.*
 * JointDepartureWriter.java
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

package org.matsim.core.mobsim.qsim.qnetsimengine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.io.IOUtils;

public class JointDepartureWriter implements AfterMobsimListener, BeforeMobsimListener {

	private static final String newLine = "\n";
	private static String scheduledJointDeparturesFile = "ScheduledJointDepartures.txt.gz";
	private static String missedJointDeparturesFile = "MissedJointDepartures.txt.gz";
		
	private final JointDepartureOrganizer jointDepartureOrganizer;
	private BufferedWriter bufferedWriter;
	
	public JointDepartureWriter(JointDepartureOrganizer jointDepartureOrganizer) {
		this.jointDepartureOrganizer = jointDepartureOrganizer;
	}
	
	public void writeDeparture(double time, JointDeparture jointDeparture) {
		try {
			bufferedWriter.write("[time=");
			bufferedWriter.write(String.valueOf(time));
			bufferedWriter.write("]");
			bufferedWriter.write(jointDeparture.toString());
			bufferedWriter.write(newLine);
		} catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}
	
	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		String file = event.getControler().getControlerIO().getIterationFilename(event.getIteration(), 
				scheduledJointDeparturesFile);
		bufferedWriter = IOUtils.getBufferedWriter(file);
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		try {
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			Gbl.errorMsg(e);
		}
		
		// Write second file containing all departures which have not been processed.
		Set<JointDeparture> missedDepartures = new LinkedHashSet<JointDeparture>();
		for (List<JointDeparture> jointDepartures : this.jointDepartureOrganizer.scheduledDepartures.values()) {
			missedDepartures.addAll(jointDepartures);
		}
		try {
			String file = event.getControler().getControlerIO().getIterationFilename(event.getIteration(),
					missedJointDeparturesFile);
			bufferedWriter = IOUtils.getBufferedWriter(file);
			for(JointDeparture jointDeparture : missedDepartures) {
				bufferedWriter.write(jointDeparture.toString());
				bufferedWriter.write(newLine);
			}
			bufferedWriter.flush();
			bufferedWriter.close();
		} catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}
}