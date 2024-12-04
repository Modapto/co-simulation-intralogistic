package org.adoxx.bpmn;

import java.util.HashMap;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;

public class BPUtils {
    
    //Ritorna tutti i places che abilitano l'oggetto BPIdObject
    public static String[] getPNIdsFromBPMNId(PetriNet pn, String bpIdObject){
        String[] pnIdObject = new String[0];
        for(P place:pn.getPlaceList_safe())
            if(place.description.equals(bpIdObject) && place.additionalInfoList.containsKey("isEntryPoint") && place.additionalInfoList.get("isEntryPoint").equals("true")){
                pnIdObject = new String[]{place.name};
                break;
            }

        if(pnIdObject.length==0)
            for(T transition:pn.getTransitionList_safe())
                if(transition.description.equals(bpIdObject) && transition.previousList.size()!=0  && transition.additionalInfoList.containsKey("isEntryPoint") && transition.additionalInfoList.get("isEntryPoint").equals("true")){
                    pnIdObject = new String[transition.previousList.size()];
                    for(int i=0;i<transition.previousList.size();i++)
                        pnIdObject[i] = transition.previousList.get(i).name;
                    break;
                }
        return pnIdObject;
    }
    
    public static boolean existBPMNObject(PetriNet pn, String bpIdObject){
        for(P place:pn.getPlaceList_safe())
            if(place.description.equals(bpIdObject) && place.additionalInfoList.containsKey("isEntryPoint") && place.additionalInfoList.get("isEntryPoint").equals("true"))
                return true;
        for(T transition:pn.getTransitionList_safe())
            if(transition.description.equals(bpIdObject) && transition.previousList.size()!=0  && transition.additionalInfoList.containsKey("isEntryPoint") && transition.additionalInfoList.get("isEntryPoint").equals("true"))
                return true;
        return false;
    }
    
    public static String getBPMNObjectNameById(PetriNet pn, String bpIdObject) throws Exception{
        for(P place:pn.getPlaceList_safe())
            if(place.description.equals(bpIdObject) && place.additionalInfoList.containsKey("isEntryPoint") && place.additionalInfoList.get("isEntryPoint").equals("true"))
                return place.additionalInfoList.get("name");
        for(T transition:pn.getTransitionList_safe())
            if(transition.description.equals(bpIdObject) && transition.previousList.size()!=0  && transition.additionalInfoList.containsKey("isEntryPoint") && transition.additionalInfoList.get("isEntryPoint").equals("true"))
                return transition.additionalInfoList.get("name");

        throw new Exception("Impossible to find an Object with id " + bpIdObject);
    }
    
    public static HashMap<String, String> getBPMNObjects(PetriNet pn){
        HashMap<String, String> ret = new HashMap<String, String>();
        for(P place:pn.getPlaceList_safe())
            if(place.additionalInfoList.containsKey("isEntryPoint") && place.additionalInfoList.get("isEntryPoint").equals("true"))
                ret.put(place.description, place.additionalInfoList.get("name"));
        for(T transition:pn.getTransitionList_safe())
            if(transition.previousList.size()!=0  && transition.additionalInfoList.containsKey("isEntryPoint") && transition.additionalInfoList.get("isEntryPoint").equals("true"))
                ret.put(transition.description, transition.additionalInfoList.get("name"));
        return ret;
    }
}
