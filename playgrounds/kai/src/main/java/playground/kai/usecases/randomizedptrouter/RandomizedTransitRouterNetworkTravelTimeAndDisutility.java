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

/**
 * 
 */
package playground.kai.usecases.randomizedptrouter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.router.CustomDataManager;
import org.matsim.pt.router.PreparedTransitSchedule;
import org.matsim.pt.router.TransitRouter;
import org.matsim.pt.router.TransitRouterConfig;
import org.matsim.pt.router.TransitRouterFactory;
import org.matsim.pt.router.TransitRouterImpl;
import org.matsim.pt.router.TransitRouterNetwork;
import org.matsim.pt.router.TransitRouterNetwork.TransitRouterNetworkLink;
import org.matsim.pt.router.TransitRouterNetworkTravelTimeAndDisutility;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicle;

public class RandomizedTransitRouterNetworkTravelTimeAndDisutility extends TransitRouterNetworkTravelTimeAndDisutility {
	
	Id cachedPersonId = null ;
	final TransitRouterConfig originalTransitRouterConfig ;
//	TransitRouterConfig localConfig ;
	private double localMarginalUtilityOfTravelTimeWalk_utl_s = Double.NaN ;
	private double localMarginalUtilityOfWaitingPt_utl_s = Double.NaN ;
	private double localUtilityOfLineSwitch_utl = Double.NaN ;
	private double localMarginalUtilityOfTravelTimePt_utl_s = Double.NaN ;
	private double localMarginalUtilityOfTravelDistancePt_utl_m = Double.NaN ;
	
	public enum DataCollection {randomizedParameters, additionInformation}
	private Map<DataCollection,Boolean> dataCollectionConfig = new HashMap<DataCollection,Boolean>() ;
	private Map<DataCollection,StringBuffer> dataCollectionStrings = new HashMap<DataCollection,StringBuffer>() ;
	
	public void setDataCollection( DataCollection item, Boolean bbb ) {
		Logger.getLogger(this.getClass()).info( " settin data collection of " + item.toString() + " to " + bbb.toString() ) ;
		dataCollectionConfig.put( item, bbb ) ;
	}
	public String getDataCollectionString( DataCollection item ) {
		return dataCollectionStrings.get(item).toString() ;
	}
	
	public RandomizedTransitRouterNetworkTravelTimeAndDisutility(TransitRouterConfig routerConfig) {
		super(routerConfig);
		
		for ( DataCollection dataCollection : DataCollection.values() ) {
			switch ( dataCollection ) {
			case randomizedParameters:
				dataCollectionConfig.put( dataCollection, false ) ;
				dataCollectionStrings.put( dataCollection, new StringBuffer() ) ;
				break;
			case additionInformation:
				dataCollectionConfig.put( dataCollection, false ) ;
				dataCollectionStrings.put( dataCollection, new StringBuffer() ) ;
				break;
			}
		}
		
		// make sure that some parameters are not zero since otherwise the randomization will not work:

		// marg utl time wlk should be around -3/h or -(3/3600)/sec.  Give warning if not at least 1/3600:
		if ( -routerConfig.getMarginalUtilityOfTravelTimeWalk_utl_s() < 1./3600. ) {
			Logger.getLogger(this.getClass()).warn( "marg utl of walk rather close to zero; randomization may not work") ;
		}
		// utl of line switch should be around -300sec or -0.5u.  Give warning if not at least 0.1u:
		if ( -routerConfig.getUtilityOfLineSwitch_utl() < 0.1 ) {
			Logger.getLogger(this.getClass()).warn( "utl of line switch rather close to zero; randomization may not work") ;
		}

			
			this.originalTransitRouterConfig = routerConfig ;
//			this.localConfig = config ;
			this.localMarginalUtilityOfTravelDistancePt_utl_m = routerConfig.getMarginalUtilityOfTravelDistancePt_utl_m();
			this.localMarginalUtilityOfTravelTimePt_utl_s = routerConfig.getMarginalUtilityOfTravelTimePt_utl_s() ;
			this.localMarginalUtilityOfTravelTimeWalk_utl_s = routerConfig.getMarginalUtilityOfTravelTimeWalk_utl_s() ;
			this.localMarginalUtilityOfWaitingPt_utl_s = routerConfig.getMarginalUtilityOfTravelTimePt_utl_s() ;
			this.localUtilityOfLineSwitch_utl = routerConfig.getUtilityOfLineSwitch_utl() ;
	}
	
	@Override
	public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle, 
			final CustomDataManager dataManager) {
		
		if ( !person.getId().equals(this.cachedPersonId)) {
			// person has changed, so ...
			
			// ... memorize new person id:
			this.cachedPersonId = person.getId() ;
			
			// ... generate new random parameters.  One param can remain fixed.
			// Since the marginal utility of walk time is used in the
			// multimodal dijkstra outside travel disutility, we keep that one fixed and vary the other ones. 
//			{
//				double tmp = this.originalTransitRouterConfig.getMarginalUtilityOfTravelTimeWalk_utl_s() ;
//				tmp *= 5. * MatsimRandom.getRandom().nextDouble() ;
//				localMarginalUtilityOfTravelTimeWalk_utl_s = tmp ;
//			}
			{
				double tmp = this.originalTransitRouterConfig.getUtilityOfLineSwitch_utl() ;
				tmp *= 5. * MatsimRandom.getRandom().nextDouble() ;
				localUtilityOfLineSwitch_utl = tmp ;
			}
			{
				double tmp = this.originalTransitRouterConfig.getMarginalUtilityOfTravelTimePt_utl_s() ;
				tmp *= 5. * MatsimRandom.getRandom().nextDouble() ;
				localMarginalUtilityOfTravelTimePt_utl_s = tmp ;
			}
			
			
			if ( this.dataCollectionConfig.get(DataCollection.randomizedParameters) ) {
				StringBuffer strb = this.dataCollectionStrings.get(DataCollection.randomizedParameters) ;
				strb.append("to be modfied by Manuel") ;
			}
		}
		
		
		double disutl;
		if (((TransitRouterNetworkLink) link).getRoute() == null) {
			// this means that it is a transfer link (walk)

			double transfertime = getLinkTravelTime(link, time, person, vehicle);
			double waittime = this.originalTransitRouterConfig.additionalTransferTime;
			
			// say that the effective walk time is the transfer time minus some "buffer"
			double walktime = transfertime - waittime;
			
			disutl = -walktime * localMarginalUtilityOfTravelTimeWalk_utl_s
			       -waittime * localMarginalUtilityOfWaitingPt_utl_s
			       - localUtilityOfLineSwitch_utl;
			
		} else {
			// this means that it is a travel link.  With this version, we cannot differentiate between in-vehicle
			// wait and out-of-vehicle wait, but my current intuition is that this will not matter that much (despite what is
			// said in the literature).  kai, sep'12
			
			disutl = - getLinkTravelTime(link, time, person, vehicle) * localMarginalUtilityOfTravelTimePt_utl_s
			       - link.getLength() * localMarginalUtilityOfTravelDistancePt_utl_m;
		}
		if ( this.dataCollectionConfig.get(DataCollection.additionInformation )) {
			StringBuffer strb = this.dataCollectionStrings.get(DataCollection.additionInformation ) ;
			strb.append("also collecting additional information") ;
		}
		return disutl;
	}


	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig(args[0]);
		Scenario sc = ScenarioUtils.loadScenario(config);
		final Controler ctrl = new Controler(sc) ;
		
		ctrl.setOverwriteFiles(true) ;
		ctrl.getConfig().vspExperimental().setUsingOpportunityCostOfTimeInPtRouting(true) ;
		
		final TransitSchedule schedule = ctrl.getScenario().getTransitSchedule() ;
		
		final TransitRouterConfig trConfig = new TransitRouterConfig( ctrl.getScenario().getConfig() ) ; 
		
		final TransitRouterNetwork routerNetwork = TransitRouterNetwork.createFromSchedule(schedule, trConfig.beelineWalkConnectionDistance);
		
		ctrl.setTransitRouterFactory( new TransitRouterFactory() {

			@Override
			public TransitRouter createTransitRouter() {
				RandomizedTransitRouterNetworkTravelTimeAndDisutility ttCalculator = new RandomizedTransitRouterNetworkTravelTimeAndDisutility(trConfig);
				ttCalculator.setDataCollection(DataCollection.randomizedParameters, true) ;
				ttCalculator.setDataCollection(DataCollection.additionInformation, false) ;
				return new TransitRouterImpl(trConfig, new PreparedTransitSchedule(schedule), routerNetwork, ttCalculator, ttCalculator);
			}
			
		}) ;
		
		ctrl.run() ;

	}

}