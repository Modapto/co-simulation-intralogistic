package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class DeadlockIdentifier implements SimulationListenerI{
    
    private boolean isDeadlockPresent = false;
    
    public DeadlockIdentifier() {
    }

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception{
        
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception{
        for(P place : petriNet.getPlaceList())
            if(place.excludeFromDeadlockCheck==false && place.numToken>0 && !petriNet.getEndList().contains(place)){
                isDeadlockPresent = true;
                return;
            }
    }

    public boolean isDeadlocked(){
        return isDeadlockPresent;
    }
}
