package org.adoxx.pn.simulation.handlers.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.adoxx.pn.P;
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
        //HashMap<P, ArrayList<T>> transitionEnabledGroupList = this.calculateRelatedSets(transitionEnabledList);
        //TransitionGroupInfos transitionEnabledGroup = this.chooseTransitionEnabledGroup(transitionEnabledGroupList);
        //T transitionToFire = this.chooseTransitionToFire(transitionEnabledGroup);
        //return transitionToFire;
        return transitionEnabledList.get(this.chooseFairPlay(0, transitionEnabledList.size()));
    }

}
