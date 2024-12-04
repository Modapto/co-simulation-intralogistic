package org.adoxx.pn;

import java.util.ArrayList;
import java.util.HashMap;

public class P implements java.io.Serializable{
    private static final long serialVersionUID = 5172052386953796272L;
    public String name = "";
    public String description = "";
    public int numToken = 0;
    public int capacity = -1;
    public boolean excludeFromDeadlockCheck = false;
    public boolean terminateAll = false;
    public String x="0",y="0",w="40.0",h="40.0";
    public ArrayList<T> previousList = new ArrayList<T>();
    public ArrayList<T> nextList = new ArrayList<T>();
    public HashMap<String, String> additionalInfoList = new HashMap<String, String>();
    public PetriNet petrinet = null;
    public P(String name){
        this.name = name.replaceAll("(\\W|_)+", "");
    }
    @SuppressWarnings("unchecked")
    public ArrayList<T> getPreviousList_safe(){ return (ArrayList<T>) previousList.clone(); }
    @SuppressWarnings("unchecked")
    public ArrayList<T> getNextList_safe(){ return (ArrayList<T>) nextList.clone(); }
    public void addInfo(String name, String value){ additionalInfoList.put(name, value); }
}