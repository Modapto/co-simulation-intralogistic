package org.adoxx.pn;

import java.util.ArrayList;
import java.util.HashMap;

public class T implements java.io.Serializable{
    private static final long serialVersionUID = 6525281606058042758L;
    public String name = "";
    public String description = "";
    public int autoFireLimit = 100;
    public int numFire = 0;
    public String x="0",y="0",w="40.0",h="40.0";
    public ArrayList<P> previousList = new ArrayList<P>();
    public ArrayList<P> nextList = new ArrayList<P>();
    public HashMap<String, String> additionalInfoList = new HashMap<String, String>();
    public PetriNet petrinet = null;
    public T(String name){
        this.name = name.replaceAll("(\\W|_)+", "");
    }
    @SuppressWarnings("unchecked")
    public ArrayList<P> getPreviousList_safe(){ return (ArrayList<P>) previousList.clone(); }
    @SuppressWarnings("unchecked")
    public ArrayList<P> getNextList_safe(){ return (ArrayList<P>) nextList.clone(); }
    public void addInfo(String name, String value){ additionalInfoList.put(name, value); }
}