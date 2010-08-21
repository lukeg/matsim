package playground.wrashid.parkingSearch.planLevel.strc2010;

import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.controler.Controler;
import org.matsim.core.trafficmonitoring.PessimisticTravelTimeAggregator;

import playground.wrashid.lib.GlobalRegistry;
import playground.wrashid.lib.obj.plan.PersonGroups;
import playground.wrashid.parkingSearch.planLevel.init.ParkingRoot;
import playground.wrashid.parkingSearch.planLevel.scenario.BaseControlerScenario;
import playground.wrashid.parkingSearch.planLevel.scenario.BaseControlerScenarioOneLiner;

/**
 * 
 * @author wrashid
 * 
 */
public class Run2 {
		public static void main(String[] args) {
		Controler controler;
		String configFilePath = "H:/data/experiments/STRC2010/input/config2.xml";
		controler = new Controler(configFilePath);

		new BaseControlerScenario(controler);

		initPersonGroupsForStatistics();

		controler.run();
	}

	private static void initPersonGroupsForStatistics() {
		PersonGroups personGroupsForStatistics = new PersonGroups();

		for (int i = 0; i <= 999; i++) {
			personGroupsForStatistics.addPersonToGroup(
					"Group-" + Integer.toString(i / 500 + 1), new IdImpl(i));
		}

		ParkingRoot.setPersonGroupsForStatistics(personGroupsForStatistics);
	}
	
	
}
