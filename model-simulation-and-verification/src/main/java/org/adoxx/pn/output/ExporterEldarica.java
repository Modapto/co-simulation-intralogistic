package org.adoxx.pn.output;

import java.util.ArrayList;

import org.adoxx.pn.P;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;

public class ExporterEldarica {

    
    public static String exportTo_EldaricaP(PetriNet pn) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        String ret = "net {\""+pn.getName()+"\"}\n";
        
        for(T tr:pn.getTransitionList_safe()){
            ret += "\ntr " + tr.name + " [] ";
            for(P pl:tr.previousList)
                ret += pl.name + " ";
            ret += "->";
            for(P pl:tr.nextList)
                ret += " " + pl.name;
        }
        
        ret += "\n";
        for(P pl:pn.getStartList_safe())
            ret += "\npl " + pl.name + " (" + pl.numToken + ")";
        
        return ret;
    }
    
    public static String exportTo_EldaricaP_property_EndReachability(PetriNet pn) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        String finalConfig = "";
        for(P pl:pn.getEndList_safe())
            finalConfig += "\nfinal " + pl.name + " (1)";
        return finalConfig;
    }
    
    public static String exportTo_EldaricaP_property_DeadlockPresence(PetriNet pn) throws Exception{
        //C'e' deadlock se tutte le transizioni non sono attivabili e c'e' almeno un place non finale che contiene uno o piu token
        
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        String finalConfig = "finalConfiguration (";
        
        for(T tr:pn.getTransitionList_safe()){
            finalConfig += "\n (";
            for(P pl:tr.previousList)
                finalConfig += pl.name + " = 0 | ";
            finalConfig = finalConfig.substring(0, finalConfig.length()-2) + ")&";
        }
        
        finalConfig += "\n\n (";
        ArrayList<P> endList = pn.getEndList_safe();
        for(P pl:pn.getPlaceList_safe())
            if(!endList.contains(pl))
                if(!pl.excludeFromDeadlockCheck)
                    finalConfig += pl.name + " > 0 | ";
        if(finalConfig.endsWith("| "))
            finalConfig = finalConfig.substring(0, finalConfig.length()-2) + ")\n)";
        else
            finalConfig = finalConfig.substring(0, finalConfig.length()-5) + ")";
        
        return finalConfig;
    }
}
