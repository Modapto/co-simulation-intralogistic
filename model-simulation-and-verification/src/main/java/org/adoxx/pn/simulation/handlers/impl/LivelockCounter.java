package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashSet;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Path;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class LivelockCounter implements SimulationListenerI{

    private PathGeneration pathGeneration = null;
    private LivelockIdentifier livelockIdentifier = null;
    private int counter = 0;
    private HashSet<Path> livelockedPaths = new HashSet<Path>();
    
    public LivelockCounter(LivelockIdentifier livelockIdentifier, PathGeneration pathGeneration) {
        this.livelockIdentifier = livelockIdentifier;
        this.pathGeneration = pathGeneration;
    }
    
    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        if(livelockIdentifier.isLivelocked()){
            counter++;
            livelockedPaths.add(pathGeneration.getUniqueEndedPath());
        }
    }
    
    public int getTraceCounter(){
        return counter;
    }
    
    public int getPathCounter(){
        return livelockedPaths.size();
    }
}
