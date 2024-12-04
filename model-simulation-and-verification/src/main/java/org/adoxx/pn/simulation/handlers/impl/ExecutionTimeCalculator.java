package org.adoxx.pn.simulation.handlers.impl;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class ExecutionTimeCalculator implements SimulationListenerI {
    private long executionTime = 0;
    private long executionTimeBeforeTransition = 0;
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        executionTime = 0;
        executionTimeBeforeTransition = 0;
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {//FIXME: arc weight
        executionTimeBeforeTransition = executionTime;
        for(P place: transitionFired.previousList)
            if(place.additionalInfoList.containsKey("executionTime"))
                executionTime += Long.parseLong(place.additionalInfoList.get("executionTime"));
    }

    public long getExecutionTime(){
        return executionTime;
    }
    
    public long getExecutionTimeBeforeTransition(){
        return executionTimeBeforeTransition;
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
      //Nothing to do here
    }
}
