/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

/**
 * 
 */
package playground.ikaddoura.optimization.handler;

import org.apache.log4j.Logger;
import org.matsim.core.api.experimental.events.AgentMoneyEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.scenario.ScenarioImpl;

import playground.ikaddoura.optimization.externalDelayEffects.InVehicleDelayEvent;
import playground.ikaddoura.optimization.externalDelayEffects.InVehicleDelayEventHandler;
import playground.ikaddoura.optimization.externalDelayEffects.WaitingDelayEvent;
import playground.ikaddoura.optimization.externalDelayEffects.WaitingDelayEventHandler;

/**
 * @author ikaddoura
 *
 */
public class MarginalCostPricingHandler implements InVehicleDelayEventHandler, WaitingDelayEventHandler {

	private final static Logger log = Logger.getLogger(MarginalCostPricingHandler.class);

	private final EventsManager events;
	private final ScenarioImpl scenario;
	private final double vtts_inVehicle;
	private final double vtts_waiting;

	public MarginalCostPricingHandler(EventsManager eventsManager, ScenarioImpl scenario) {
		this.events = eventsManager;
		this.scenario = scenario;
		this.vtts_inVehicle = (this.scenario.getConfig().planCalcScore().getTravelingPt_utils_hr() - this.scenario.getConfig().planCalcScore().getPerforming_utils_hr()) / this.scenario.getConfig().planCalcScore().getMarginalUtilityOfMoney();	
		this.vtts_waiting = (this.scenario.getConfig().planCalcScore().getMarginalUtlOfWaitingPt_utils_hr() - this.scenario.getConfig().planCalcScore().getPerforming_utils_hr()) / this.scenario.getConfig().planCalcScore().getMarginalUtilityOfMoney();
		
		log.info("VTTS_inVehicleTime: " + vtts_inVehicle);
		log.info("VTTS_waiting: " + vtts_waiting);
	}

	@Override
	public void reset(int iteration) {
	}

	@Override
	public void handleEvent(InVehicleDelayEvent event) {
		double amount = (event.getDelay() * event.getAffectedAgents() / 3600) * this.vtts_inVehicle;
		AgentMoneyEvent moneyEvent = new AgentMoneyEvent(event.getTime(), event.getPersonId(), amount);
		this.events.processEvent(moneyEvent);
	}

	@Override
	public void handleEvent(WaitingDelayEvent event) {
		double amount = (event.getDelay() * event.getAffectedAgents() / 3600 ) * this.vtts_waiting;
		AgentMoneyEvent moneyEvent = new AgentMoneyEvent(event.getTime(), event.getPersonId(), amount);
		this.events.processEvent(moneyEvent);		
	}

}