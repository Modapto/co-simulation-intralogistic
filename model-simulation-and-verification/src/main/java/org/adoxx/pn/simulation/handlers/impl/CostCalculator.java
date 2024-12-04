package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class CostCalculator implements SimulationListenerI {
    private double cost = 0;
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception{
        cost = 0;
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception { //FIXME: arc weight
        for(P place: transitionFired.previousList)
            if(place.additionalInfoList.containsKey("cost"))
                cost += Double.parseDouble(place.additionalInfoList.get("cost"));
    }

    public double getCost(){
        return cost;
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception{
        //Nothing to do here
    }
}

