package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class LivelockIdentifier implements SimulationListenerI{

    PetriNet petriNet = null;
    boolean isHappened = false;
    long numTransitionFired = 0;
    long maxNumTransitionFired = 10000; //in 10s circa 5051240 transition fired
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        this.petriNet = petriNet;
        isHappened = false;
        numTransitionFired = 0;
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        if(numTransitionFired > maxNumTransitionFired){
            for(P place:petriNet.getPlaceList())
                place.numToken = 0;
            isHappened = true;
        }
        
        numTransitionFired++;
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
    }

    public boolean isLivelocked(){
        return isHappened;
    }
}
