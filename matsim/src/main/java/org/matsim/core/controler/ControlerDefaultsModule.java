/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * DefaultControlerModules.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2014 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.core.controler;

import org.matsim.analysis.LegTimesModule;
import org.matsim.analysis.ScoreStatsModule;
import org.matsim.analysis.VolumesAnalyzerModule;
import org.matsim.core.controler.corelisteners.LegHistogramModule;
import org.matsim.core.controler.corelisteners.LinkStatsModule;
import org.matsim.core.trafficmonitoring.TravelTimeCalculatorModule;
import org.matsim.counts.CountsModule;

public class ControlerDefaultsModule extends AbstractModule {
    @Override
    public void install() {
        include(new TravelTimeCalculatorModule());
        include(new LinkStatsModule());
        include(new VolumesAnalyzerModule());
        include(new LegHistogramModule());
        include(new LegTimesModule());
        include(new ScoreStatsModule());
        include(new CountsModule());
    }
}