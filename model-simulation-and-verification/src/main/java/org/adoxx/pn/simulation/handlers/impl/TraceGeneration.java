package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashMap;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Trace;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class TraceGeneration implements SimulationListenerI {

    private Trace trace = null;
    private HashMap<Trace, Trace> traceSet = new HashMap<Trace, Trace>();
    private PathGeneration pathGeneration = null;
    
    public TraceGeneration(PathGeneration pathGeneration){
        this.pathGeneration = pathGeneration;
    }
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        trace = new Trace();
    }
    
    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        trace.addTransition(transitionFired);
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {

        if(!traceSet.containsKey(trace)){
            trace.path = pathGeneration.getUniqueEndedPath();
            trace.id = traceSet.size();
            traceSet.put(trace, trace);
        }
    }
    
    public Trace getOngoingTrace() throws Exception{
        if(trace == null)
            throw new Exception("Simulation has not been started. The trace is currently null");
        return trace;
    }
    
    public Trace getUniqueEndedTrace() throws Exception{
        Trace uniqueTrace = traceSet.get(getOngoingTrace());
        if(uniqueTrace == null)
            throw new Exception("Simulation has not been terminated. The trace is currently still in progress");
        
        return uniqueTrace;
    }
}
