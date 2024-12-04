package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashSet;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Path;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class DeadlockCounter implements SimulationListenerI{

    private PathGeneration pathGeneration = null;
    private DeadlockIdentifier deadlockIdentifier = null;
    private int counter = 0;
    private HashSet<Path> deadlockedPaths = new HashSet<Path>();
    
    public DeadlockCounter(DeadlockIdentifier deadlockIdentifier, PathGeneration pathGeneration) {
        this.deadlockIdentifier = deadlockIdentifier;
        this.pathGeneration = pathGeneration;
    }

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception{
        
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception{
        if(deadlockIdentifier.isDeadlocked()){
            counter++;
            deadlockedPaths.add(pathGeneration.getUniqueEndedPath());
        }
    }
    
    public int getTraceCounter(){
        return counter;
    }
    
    public int getPathCounter(){
        return deadlockedPaths.size();
    }
}
