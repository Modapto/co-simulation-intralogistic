package org.adoxx.pn.simulation.handlers.impl;

import java.util.HashMap;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.data.Path;
import org.adoxx.pn.simulation.handlers.SimulationListenerI;

public class PathGeneration implements SimulationListenerI {
    
    private Path path = null;
    private HashMap<Path, Path> pathSet = new HashMap<Path, Path>();

    @Override
    public void simulationStarted(PetriNet petriNet) throws Exception {
        path = new Path();
    }

    @Override
    public void transitionFiredEvent(T transitionFired) throws Exception {
        path.addTransition(transitionFired);
    }

    @Override
    public void simulationEnded(PetriNet petriNet) throws Exception {
        if(!pathSet.containsKey(path)){
            path.id = pathSet.size();
            pathSet.put(path, path);
        }
    }
    
    public Path getPath() throws Exception{
        if(path == null)
            throw new Exception("Simulation has not been started. The path is currently null");
        return path;
    }
    
    public Path getUniqueEndedPath() throws Exception{
        Path uniquePath = pathSet.get(getPath());
        if(uniquePath == null)
            throw new Exception("Simulation has not been terminated. The path is currently still in progress");
        
        return uniquePath;
    }
}
