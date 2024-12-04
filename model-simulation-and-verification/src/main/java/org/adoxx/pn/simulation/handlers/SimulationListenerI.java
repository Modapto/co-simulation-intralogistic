package org.adoxx.pn.simulation.handlers;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;

public interface SimulationListenerI {
    void simulationStarted(PetriNet petriNet) throws Exception;
    void transitionFiredEvent(T transitionFired) throws Exception;
    void simulationEnded(PetriNet petriNet) throws Exception;
}
