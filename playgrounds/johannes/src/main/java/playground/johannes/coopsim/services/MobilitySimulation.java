/* *********************************************************************** *
 * project: org.matsim.*
 * MobilitySimulation.java
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
package playground.johannes.coopsim.services;

import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.api.experimental.events.EventsManager;

import java.util.Collection;

/**
 * @author illenberger
 *
 */
public interface MobilitySimulation {

	public void run(Collection<Plan> plans, EventsManager eventsManager);
	
}
