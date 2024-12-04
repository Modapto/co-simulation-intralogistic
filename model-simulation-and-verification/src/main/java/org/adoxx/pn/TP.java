package org.adoxx.pn;

import java.util.HashMap;

public class TP implements java.io.Serializable{
    private static final long serialVersionUID = 5691467102614286878L;
    public T source;
    public P target;
    public int weight=1;
    public HashMap<String, String> additionalInfoList = new HashMap<String, String>();
    public PetriNet petrinet = null;
    public TP(T transition, P place){
        this.source = transition;
        this.target = place;
    }
    public void addInfo(String name, String value){ additionalInfoList.put(name, value); }
}