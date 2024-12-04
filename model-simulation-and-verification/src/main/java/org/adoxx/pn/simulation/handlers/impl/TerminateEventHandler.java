package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class TerminateEventHandler implements SimulationListenerI{
    
    PetriNet petriNet = null;
    boolean isHappened = false;
    
    public TerminateEventHandler() {
    }

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        this.petriNet = petriNet;
        isHappened = false;
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        for(P nextPlace:transitionFired.nextList)
            if(nextPlace.terminateAll){
                for(P place:petriNet.getPlaceList())
                    if(place.additionalInfoList.get("poolId") == nextPlace.additionalInfoList.get("poolId"))
                        if(!place.equals(nextPlace))
                            place.numToken = 0;
                isHappened = true;
                return;
            }
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {

    }
    
    public boolean isHappened(){
        return isHappened;
    }

}
