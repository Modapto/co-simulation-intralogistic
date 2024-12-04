package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class TerminateEventCounter implements SimulationListenerI{

    private TerminateEventHandler terminateEventHandler = null;
    
    private int counter = 0;
    
    public TerminateEventCounter(TerminateEventHandler terminateEventHandler) {
        this.terminateEventHandler = terminateEventHandler;
    }

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        if(terminateEventHandler.isHappened())
            counter++;
    }

    public int getCounter(){
        return counter;
    }
}
