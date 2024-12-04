package org.adoxx.pn.simulation.handlers;

import java.util.ArrayList;

import org.adoxx.pn.T;

public interface TransitionSelectorI {
    public T chooseTransition(ArrayList<T> transitionEnabledList) throws Exception;
}
