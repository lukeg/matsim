/* *********************************************************************** *
 * project: org.matsim.*
 * PajekVisWriter.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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
package playground.johannes.snowball;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.plans.Person;
import org.matsim.utils.geometry.CoordI;
import org.matsim.utils.io.IOUtils;

import playground.johannes.socialnets.UserDataKeys;
import cern.colt.list.IntArrayList;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.utils.Pair;

/**
 * @author illenberger
 *
 */
public class PajekVisWriter {

	private static final Logger logger = Logger.getLogger(PajekVisWriter.class);
	
	private static final String NEW_LINE = "\r\n";
	
	private static final String WHITESPACE = " ";
	
	private static final String QUOTE = "\"";
	
	private static final String ZERO = "0";
	
	private static final String DEFAULT_EDGE_WHEIGHT = "1";
	
	private static final String COLOR = "c";
	
	private static final String FILL_COLOR = "ic";
	
//	private static final String BORDER_COLOR = "bc";
	
//	private static final String RED_COLOR = "Red";
	
	private static final String WHITE_COLOR = "White";
	
//	private static final String GREEN_COLOR = "Green";
	
	private static final String[] waveColor = new String[]{"Red","Orange","Yellow","Green"};
	
	public void write(Graph g, String filename) {
		try {
			BufferedWriter writer = IOUtils.getBufferedWriter(filename);
			/*
			 * Vertices...
			 */
			writer.write("*Vertices ");
			writer.write(String.valueOf(g.numVertices()));
			writer.write(NEW_LINE);
			
			Map<Vertex, String> vertexIds = new HashMap<Vertex, String>();
			int counter = 1;
			for(Object v : g.getVertices()) {
//				Person p = ((Person)((Vertex)v).getUserDatum(UserDataKeys.PERSON_KEY));
//				CoordI c = p.getSelectedPlan().getFirstActivity().getCoord();
				String id = (String)((Vertex)v).getUserDatum(UserDataKeys.ID);
				double x = ((Double)((Vertex)v).getUserDatum(UserDataKeys.X_COORD)).doubleValue();
				double y = ((Double)((Vertex)v).getUserDatum(UserDataKeys.Y_COORD)).doubleValue();
				IntArrayList waves = (IntArrayList)(((Vertex)v).getUserDatum(UserDataKeys.WAVE_KEY));
				
				writer.write(String.valueOf(counter));
				writer.write(WHITESPACE);
				writer.write(QUOTE);
//				writer.write(p.getId().toString());
				writer.write(id);
				writer.write(QUOTE);
				writer.write(WHITESPACE);
//				writer.write(String.valueOf(c.getX()));
				writer.write(String.valueOf(x));
				writer.write(WHITESPACE);
//				writer.write(String.valueOf(c.getY()));
				writer.write(String.valueOf(y));
				writer.write(WHITESPACE);
				writer.write(ZERO);
				writer.write(WHITESPACE);
				writer.write(FILL_COLOR);
				writer.write(WHITESPACE);
				writer.write(getColor(waves));
				
//				writer.write(WHITESPACE);
//				writer.write(BORDER_COLOR);
				
				writer.write(NEW_LINE);
				
				vertexIds.put((Vertex) v, String.valueOf(counter));
				counter++;
			}
			/*
			 * Edges...
			 */
			writer.write("*Edges ");
			writer.write(String.valueOf(g.numEdges()));
			writer.write(NEW_LINE);
			
			for(Object e : g.getEdges()) {
				IntArrayList waves = (IntArrayList)(((Edge)e).getUserDatum(UserDataKeys.WAVE_KEY));
				Pair pair = ((Edge)e).getEndpoints();
				Vertex source = (Vertex) pair.getFirst();
				Vertex target = (Vertex) pair.getSecond();
				writer.write(vertexIds.get(source));
				
				writer.write(WHITESPACE);
				writer.write(vertexIds.get(target));
				writer.write(WHITESPACE);
				writer.write(DEFAULT_EDGE_WHEIGHT);
				writer.write(WHITESPACE);
				
				writer.write(COLOR);
				writer.write(WHITESPACE);
				writer.write(getColor(waves));
				writer.write(NEW_LINE);
			}
			
			writer.close();
			
		} catch (IOException e) {
			logger.fatal("Error during writing graph!", e);
		}
	}
	
	private String getColor(IntArrayList waves) {
		if(waves == null)
			return WHITE_COLOR;
		else {
			int idx = waves.get(0);
			if(idx < waveColor.length)
				return waveColor[idx];
			else
				return waveColor[waveColor.length - 1];
		}
			
	}
}
