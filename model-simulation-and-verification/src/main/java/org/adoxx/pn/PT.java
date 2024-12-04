package org.adoxx.pn;

import java.util.HashMap;

public class PT implements java.io.Serializable{
    private static final long serialVersionUID = -33230958480957514L;
    public P source;
    public T target;
    public int weight=1;
    public HashMap<String, String> additionalInfoList = new HashMap<String, String>();
    public PetriNet petrinet = null;
    public PT(P place, T transition){
        this.source = place;
        this.target = transition;
    }
    public void addInfo(String name, String value){ additionalInfoList.put(name, value); }
}