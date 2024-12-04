package org.adoxx.pn;

import java.util.ArrayList;
import java.util.HashMap;

public class PetriNet implements java.io.Serializable{

    private static final long serialVersionUID = 3426953988681102210L;

    private String name;
    private ArrayList<P> placeList = new ArrayList<P>();
    private ArrayList<T> transitionList = new ArrayList<T>();
    private ArrayList<P> startList = new ArrayList<P>();
    private ArrayList<P> endList = new ArrayList<P>();
    private ArrayList<PT> connectionPTList = new ArrayList<PT>();
    private ArrayList<TP> connectionTPList = new ArrayList<TP>();
    
    public HashMap<String, String> additionalInfoList = new HashMap<String, String>();
    
    public PetriNet(String name){
        this.name = name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    public void resetNet(){
        placeList.clear();
        transitionList.clear();
        startList.clear();
        endList.clear();
        connectionPTList.clear();
        connectionTPList.clear();
    }
    
    public PetriNet clonePN() throws Exception{
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(512);
        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bos);
        out.writeObject(this);
        byte[] bytes = bos.toByteArray();
        out.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bytes));
        PetriNet readObject = (PetriNet) ois.readObject();
        ois.close();
        return readObject;
    }
    
    public void importPN(PetriNet petriNet) throws Exception{
        petriNet = petriNet.clonePN();
        placeList.addAll(petriNet.placeList);
        transitionList.addAll(petriNet.transitionList);
        startList.addAll(petriNet.startList);
        endList.addAll(petriNet.endList);
        connectionPTList.addAll(petriNet.connectionPTList);
        connectionTPList.addAll(petriNet.connectionTPList);
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<P> getPlaceList_safe(){ return (ArrayList<P>) placeList.clone(); }
    public ArrayList<P> getPlaceList(){ return placeList; }
    @SuppressWarnings("unchecked")
    public ArrayList<T> getTransitionList_safe(){ return (ArrayList<T>) transitionList.clone(); }
    public ArrayList<T> getTransitionList(){ return transitionList; }
    @SuppressWarnings("unchecked")
    public ArrayList<P> getStartList_safe(){ return (ArrayList<P>) startList.clone(); }
    public ArrayList<P> getStartList(){ return startList; }
    @SuppressWarnings("unchecked")
    public ArrayList<P> getEndList_safe(){ return (ArrayList<P>) endList.clone(); }
    public ArrayList<P> getEndList(){ return endList; }
    @SuppressWarnings("unchecked")
    public ArrayList<PT> getConnectionPTList_safe(){ return (ArrayList<PT>) connectionPTList.clone(); }
    public ArrayList<PT> getConnectionPTList(){ return connectionPTList; }
    @SuppressWarnings("unchecked")
    public ArrayList<TP> getConnectionTPList_safe(){ return (ArrayList<TP>) connectionTPList.clone(); }
    public ArrayList<TP> getConnectionTPList(){ return connectionTPList; }
    
    public boolean isEmpty(){
        return placeList.isEmpty() && transitionList.isEmpty();
    }
    
    public P addPlace(String name) throws Exception{
        return addPlace(name, 0);
    }

    public P addPlace(String name, int numToken) throws Exception{
        if(name == null || name.isEmpty())
            throw new Exception("ERROR: Name is empty or null");
        for(P place:placeList)
            if(place.name.equals(name))
                throw new Exception("ERROR: A place with name " + name + " already exist");
        P ret = new P(name);
        ret.numToken = numToken;
        ret.petrinet = this;
        placeList.add(ret);
        return ret;
    }
    
    public void delPlace(String name) throws Exception{
        delPlace(getPlace(name));
    }
    public void delPlace(P place) throws Exception{
        placeList.remove(place);
        endList.remove(place);
        startList.remove(place);
        
        for(T tr0:place.getPreviousList_safe())
            delConnection(tr0, place);
        for(T tr0:place.getNextList_safe())
            delConnection(place, tr0);
    }
    
    public  ArrayList<T> getEnabledTransitions() throws Exception{
        return getEnabledTransitions(getCurrentMark());
    }

    public ArrayList<T> getEnabledTransitions(int[] startingMarkList) throws Exception{
        if(startingMarkList == null)
            throw new Exception("ERROR: starting mark can not be null");
        if(startingMarkList.length != placeList.size())
            throw new Exception("ERROR: you have to provide a mark for each place");
        ArrayList<T> enabledList = new ArrayList<T>();
        for(T transition: transitionList){
            boolean isEnabled=true;
            if(transition.previousList.size() == 0 && transition.numFire > transition.autoFireLimit)
                isEnabled = false;
            for(P place:transition.previousList)
                if(startingMarkList[placeList.indexOf(place)] - getConnection(place, transition).weight < 0)
                    isEnabled = false;
            for(P place:transition.nextList)
                if(startingMarkList[placeList.indexOf(place)] + getConnection(transition, place).weight > place.capacity && place.capacity > 0)
                    isEnabled = false;
            if(isEnabled)
                enabledList.add(transition);
        }
        return enabledList;
    }

    public void fireTransition(T transition) throws Exception{
        if(!getEnabledTransitions().contains(transition))
            throw new Exception("ERROR: transition " + transition.name + " can not be fired");
        transition.numFire++;
        for(P place: transition.previousList) {
            place.numToken -= getConnection(place, transition).weight;
            if(place.numToken < 0)
                throw new Exception("ERROR: Inconsistent state after fired transition " + transition.name + ": previous place " + place.name + " tokens are " + place.numToken);
        }
        for(P place: transition.nextList) {
            place.numToken += getConnection(transition, place).weight;
            if(place.numToken > place.capacity && place.capacity > 0)
                throw new Exception("ERROR: Inconsistent state after fired transition " + transition.name + ": next place " + place.name + " tokens are " + place.numToken + " while max capacity is " + place.capacity);
        }
    }
    
    public int[] getCurrentMark(){
        int[] mark = new int[placeList.size()];
        for(int i=0; i<placeList.size();i++)
            mark[i] = placeList.get(i).numToken;
        return mark;
    }
    
    public void setMark(int[] newMark) throws Exception{
        if(newMark == null)
            throw new Exception("ERROR: starting mark can not be null");
        if(newMark.length != placeList.size())
            throw new Exception("ERROR: you have to provide a mark for each place");
        for(int i=0; i<placeList.size();i++)
            placeList.get(i).numToken = newMark[i];

        for(int i=0; i<transitionList.size();i++)
            transitionList.get(i).numFire = 0;
    }
    
    public T addTransition(String name) throws Exception{
        if(name == null || name.isEmpty())
            throw new Exception("ERROR: Name is empty or null");
        for(T transition:transitionList)
            if(transition.name.equals(name))
                throw new Exception("ERROR: A transition with name " + name + " already exist");
        T ret = new T(name);
        transitionList.add(ret);
        ret.petrinet = this;
        return ret;
    }
    
    public void delTransition(String name) throws Exception{
        delTransition(getTransition(name));
    }
    public void delTransition(T transition) throws Exception{
        transitionList.remove(transition);
        for(P pl0:transition.getPreviousList_safe())
            delConnection(pl0, transition);
        for(P pl0:transition.getNextList_safe())
            delConnection(transition, pl0);
    }
    
    public PT connect(P place, T transition) throws Exception{
        PT conn = null;
        try {
            conn = getConnection(place, transition);
        } catch (Exception e) {}
        if(conn != null) {
            conn.weight += 1;
            return conn;
        }
        place.nextList.add(transition);
        transition.previousList.add(place);
        conn = new PT(place, transition);
        connectionPTList.add(conn);
        conn.petrinet = this;
        return conn;
    }
    public TP connect(T transition, P place) throws Exception{
        TP conn = null;
        try {
            conn = getConnection(transition, place);
        } catch (Exception e) {}
        if(conn != null) {
            conn.weight += 1;
            return conn;
        }
        transition.nextList.add(place);
        place.previousList.add(transition);
        conn = new TP(transition, place);
        connectionTPList.add(conn);
        conn.petrinet = this;
        return conn;
    }
    
    public boolean existConnection(P place, T transition){
        for(T trNext: place.nextList)
            if(transition.equals(trNext))
                return true;
        return false;
    }
    
    public boolean existConnection(T transition, P place){
        for(P plNext: transition.nextList)
            if(place.equals(plNext))
                return true;
        return false;
    }
    
    public void delConnection(P place, T transition) throws Exception{
        PT conn = getConnection(place, transition);
        connectionPTList.remove(conn);
        place.nextList.remove(transition);
        transition.previousList.remove(place);
        
    }
    public void delConnection(T transition, P place) throws Exception{
        TP conn = getConnection(transition, place);
        connectionTPList.remove(conn);
        transition.nextList.remove(place);
        place.previousList.remove(transition);
    }
    
    public P getPlace(String name) throws Exception{
        if(name == null || name.isEmpty())
            throw new Exception("ERROR: Name is empty or null");
        name = name.replaceAll("(\\W|_)+", "");
        for(P place:placeList)
            if(place.name.equals(name))
                return place;
        throw new Exception("ERROR: Can not find a Place with name " + name);
    }
    
    public T getTransition(String name) throws Exception{
        if(name == null || name.isEmpty())
            throw new Exception("ERROR: Name is empty or null");
        name = name.replaceAll("(\\W|_)+", "");
        for(T transition:transitionList)
            if(transition.name.equals(name))
                return transition;
        throw new Exception("ERROR: Can not find a Transition with name " + name);
    }
    
    public boolean existPlace(String name) throws Exception{
        if(name == null || name.isEmpty())
            throw new Exception("ERROR: Name is empty or null");
        name = name.replaceAll("(\\W|_)+", "");
        for(P place:placeList)
            if(place.name.equals(name))
                return true;
        return false;
    }
    
    public boolean existTransition(String name) throws Exception{
        if(name == null || name.isEmpty())
            throw new Exception("ERROR: Name is empty or null");
        name = name.replaceAll("(\\W|_)+", "");
        for(T transition:transitionList)
            if(transition.name.equals(name))
                return true;
        return false;
    }
    
    public TP getConnection(T transition, P place) throws Exception{
        for(TP tp:connectionTPList)
            if(tp.source == transition && tp.target == place)
                return tp;
        throw new Exception("ERROR: Can not find a connection between " + transition.name + " and " + place.name);
    }
    public PT getConnection(P place, T transition) throws Exception{
        for(PT pt:connectionPTList)
            if(pt.source == place && pt.target == transition)
                return pt;
        throw new Exception("ERROR: Can not find a connection between " + place.name + " and " + transition.name);
    }
    
    public void finalizeModel() throws Exception{
        correctDeadTransitions();
        updateEndList();
        updateStartList();
    }

    private void correctDeadTransitions() throws Exception{
        for(T transition:transitionList)
            if(transition.nextList.isEmpty()){
                P pl = this.addPlace("pEnd"+transition.name);
                this.connect(transition, pl);
            }
    }
    
    private void updateEndList(){
        ArrayList<P> newEndList = new ArrayList<P>();
        for(P place:placeList)
            if(place.nextList.isEmpty())
                newEndList.add(place);
        this.endList = newEndList;
    }
    
    private void updateStartList(){
        ArrayList<P> newStartList = new ArrayList<P>();
        for(P place:placeList)
            if(place.numToken>0)
                newStartList.add(place);
        this.startList = newStartList;
    }
    
    public void updateStartListCheckingFlow(){
        ArrayList<P> newStartList = new ArrayList<P>();
        for(P place:placeList)
            if(place.numToken>0)
                newStartList.add(place);
            else
                if(place.previousList.isEmpty()){
                    place.numToken = 1;
                    newStartList.add(place);
                }
        this.startList = newStartList;
    }
    /*
    public static void main(String[] args) {
        try {
            PetriNet pn = new PetriNet("test");
            
            P p0 = pn.addPlace("p0", 1);
            T t0 = pn.addTransition("t0");
            P p1 = pn.addPlace("p1");
            T t1 = pn.addTransition("t1");
            //PL p2 = pn.addPlace("p2");
            pn.connect(p0, t0);
            //pn.connect(p0, t1);
            pn.connect(t0, p1);
            pn.connect(p1, t1);
            //pn.connect(t1, p2);
            //pn.delTransition(t0);
            //pn.delPlace(p1);
            pn.finalizeModel();
            
            System.out.println(ExporterPNML.exportTo_PNML(pn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
