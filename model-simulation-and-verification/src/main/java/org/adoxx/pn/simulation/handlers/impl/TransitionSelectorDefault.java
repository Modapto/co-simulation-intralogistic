package org.adoxx.pn.simulation.handlers.impl;

import java.util.ArrayList;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.simulation.handlers.TransitionSelectorA;

public class TransitionSelectorDefault extends TransitionSelectorA{

    public TransitionSelectorDefault(PetriNet pn) {
        super(pn);
    }
    
    public TransitionSelectorDefault() {
        super();
    }

    @Override
    public T chooseTransition(ArrayList<T> transitionEnabledList) throws Exception {
        return this.chooseTransitionToFire(this.chooseTransitionEnabledGroup(this.calculateRelatedSets(transitionEnabledList)));
    }

}
