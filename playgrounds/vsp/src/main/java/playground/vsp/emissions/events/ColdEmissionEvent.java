/* *********************************************************************** *
 * project: org.matsim.*
 * ColdEmissionEvent.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.vsp.emissions.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;

import playground.vsp.emissions.types.ColdPollutant;

/**
 * Event to indicate that cold emissions were produced.
 * @author benjamin
 *
 */
public interface ColdEmissionEvent {

	public final static String EVENT_TYPE = "coldEmissionEvent";
	
	public final static String ATTRIBUTE_LINK_ID = "linkId";
	public final static String ATTRIBUTE_VEHICLE_ID = "vehicleId";
	
	public Id getLinkId();
	
	public Id getVehicleId();

	public Map<ColdPollutant, Double> getColdEmissions();
	
	public double getTime();
	
}