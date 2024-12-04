package org.adoxx.pn.output;

import java.util.ArrayList;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;

public class ExporterLOLA {

    public static String exportTo_LOLA(PetriNet pn) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        String ret = "PLACE ";
        for(P place:pn.getPlaceList_safe())
            ret += place.name + ", ";
        if(ret.endsWith(", "))
            ret = ret.substring(0, ret.length()-2) + ";";
        else
            ret = ret + ";";
        ret += "\nMARKING ";
        for(P place:pn.getStartList_safe())
            ret += place.name + ": " + place.numToken + ", ";
        if(ret.endsWith(", "))
            ret = ret.substring(0, ret.length()-2) + ";";
        else
            ret = ret + ";";
        for(T transition:pn.getTransitionList_safe()){
            ret += "\nTRANSITION " + transition.name;
            ret += " CONSUME ";
            for(P pl:transition.getPreviousList_safe())
                ret += pl.name + ": " + pn.getConnection(pl, transition).weight + ", ";
            if(ret.endsWith(", "))
                ret = ret.substring(0, ret.length()-2) + ";";
            else
                ret = ret + ";";
            ret += " PRODUCE ";
            for(P pl:transition.getNextList_safe())
                ret += pl.name + ": " + pn.getConnection(transition, pl).weight + ", ";
            if(ret.endsWith(", "))
                ret = ret.substring(0, ret.length()-2) + ";";
            else
                ret = ret + ";";
        }

        return ret;
    }
    
    
    public static String exportTo_LOLA_property_DeadlockPresence(PetriNet pn) throws Exception{
        // C'e' deadlock se tutte le transizioni non sono attivabili (DEADLOCK) e c'e' almeno un place non finale che contiene uno o piu token
        
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        String finalConfig = "EF (DEADLOCK AND ( ";
        ArrayList<P> endList = pn.getEndList_safe();
        for(P pl:pn.getPlaceList_safe())
            if(!endList.contains(pl))
                if(!pl.excludeFromDeadlockCheck)
                    finalConfig += pl.name + " > 0 OR ";
        if(finalConfig.endsWith("OR "))
            finalConfig = finalConfig.substring(0, finalConfig.length()-3) + "))";
        else
            finalConfig = "EF DEADLOCK";
        return finalConfig;
    }
    
    public static String[] exportTo_LOLA_property_UnboundednessPresence(PetriNet pn, boolean onlyEndPlaces) throws Exception{
        // La rete e' unbounded se almeno un place e' unbounded. LOLA consiglia di controllare tutti i places separatamente
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        ArrayList<P> placeList = null;
        if(onlyEndPlaces){
            placeList = pn.getEndList_safe();
            if(placeList.size()==0)
                placeList = pn.getPlaceList_safe();
        } else
            placeList = pn.getPlaceList_safe();
        
        ArrayList<String> retA = new ArrayList<String>();
        for(int i=0;i<placeList.size();i++)
            if(!placeList.get(i).excludeFromDeadlockCheck)
                retA.add("EF( "+placeList.get(i).name+" >= oo )");
        
        String[] ret = new String[retA.size()];
        retA.toArray(ret);
        return ret;
    }
    
    public static String exportTo_LOLA_property_State2FollowState1(PetriNet pn, String[] pnIdObject1List, String[] pnIdObject2List, boolean always, boolean negateFrom, boolean negateTo) throws Exception{

        if(pnIdObject1List.length==0)
            throw new Exception("ERROR: At least one PetriNet Object Id for the first state is required");
        if(pnIdObject2List.length==0)
            throw new Exception("ERROR: At least one PetriNet Object Id for the last state is required");
        
        for(String pnIdObject1:pnIdObject1List)
            if(!pn.existPlace(pnIdObject1))
                throw new Exception("ERROR: Place " + pnIdObject1 + " not found");
        for(String pnIdObject2:pnIdObject2List)
            if(!pn.existPlace(pnIdObject2))
                throw new Exception("ERROR: Place " + pnIdObject2 + " not found");
        
        String pnIdObject1S = "";
        for(String pnIdObject1:pnIdObject1List)
            pnIdObject1S += pnIdObject1 + " > 0 AND ";
        if(pnIdObject1S.endsWith("AND "))
            pnIdObject1S = pnIdObject1S.substring(0, pnIdObject1S.length()-5);
        
        String pnIdObject2S = "";
        for(String pnIdObject2:pnIdObject2List)
            pnIdObject2S += pnIdObject2 + " > 0 AND ";
        if(pnIdObject2S.endsWith("AND "))
            pnIdObject2S = pnIdObject2S.substring(0, pnIdObject2S.length()-5);
        
        String alwaysFollow = (always)?"A":"E";
        String notFrom = (negateFrom)?"NOT":"";
        String notTo = (negateTo)?"G NOT":"F";
        
        return "AG ("+notFrom+"("+pnIdObject1S+") -> "+alwaysFollow+notTo+"("+pnIdObject2S+"))";
    }
    
    public static String exportTo_LOLA_property_StateReachable(PetriNet pn, String[] pnIdObjectList, boolean always, boolean negate) throws Exception{
        if(pnIdObjectList.length==0)
            throw new Exception("ERROR: At least one PetriNet Object Id for the first state is required");

        String pnIdObjectS = "";
        for(String pnIdObject:pnIdObjectList)
            pnIdObjectS += pnIdObject + " > 0 AND ";
        if(pnIdObjectS.endsWith("AND "))
            pnIdObjectS = pnIdObjectS.substring(0, pnIdObjectS.length()-5);
        
        String alwaysFollow = (always)?"A":"E";
        String not = (negate)?"G NOT":"F";
        
        return alwaysFollow+not+"("+pnIdObjectS+")";
    }
}
