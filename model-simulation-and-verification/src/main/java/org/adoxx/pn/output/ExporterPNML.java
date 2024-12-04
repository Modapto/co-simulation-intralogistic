package org.adoxx.pn.output;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.adoxx.pn.P;
import org.adoxx.pn.PT;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.TP;
import org.adoxx.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExporterPNML {

    //FIXME: longs id does not conforms to the PNML standard. check and fix
    public static String exportTo_PNML(PetriNet pn) throws Exception{
        if(pn.isEmpty())
            throw new Exception("ERROR: The provided petri net is empty");
        
        String pnName = pn.getName().replaceAll("(\\W|_)+", "");
        
        Document xmlDoc = XMLUtils.createNewDocument();
        
        Element pnmlEl = xmlDoc.createElementNS("http://www.pnml.org/version-2009/grammar/pnml", "pnml");
        xmlDoc.appendChild(pnmlEl);
        Element netEl = xmlDoc.createElement("net");
        pnmlEl.appendChild(netEl);
        netEl.setAttribute("type", "http://www.pnml.org/version-2009/grammar/ptnet");
        netEl.setAttribute("id", pnName);
        
        Element netNameEl = xmlDoc.createElement("name");
        netEl.appendChild(netNameEl);
        Element netNameTextEl = xmlDoc.createElement("text");
        netNameEl.appendChild(netNameTextEl);
        netNameTextEl.setTextContent(pnName);
        
        Element netPageEl = xmlDoc.createElement("page");
        netEl.appendChild(netPageEl);
        netPageEl.setAttribute("id", "page0");
        
        for(P place:pn.getPlaceList_safe()){
            Element placeEl = xmlDoc.createElement("place");
            netPageEl.appendChild(placeEl);
            placeEl.setAttribute("id", place.name);
            
            Element toolSpecificEl = xmlDoc.createElement("toolspecific");
            placeEl.appendChild(toolSpecificEl);
            toolSpecificEl.setAttribute("tool", "org.adoxx");
            toolSpecificEl.setAttribute("version", "1");
            for(Entry<String, String> entry:place.additionalInfoList.entrySet()){
                Element additionalAttrEl = xmlDoc.createElement(entry.getKey());
                toolSpecificEl.appendChild(additionalAttrEl);
                additionalAttrEl.setTextContent(entry.getValue());
            }
            
            Element nameEl = xmlDoc.createElement("name");
            placeEl.appendChild(nameEl);
            Element textEl = xmlDoc.createElement("text");
            nameEl.appendChild(textEl);
            textEl.setTextContent(place.name);
            Element graph1El = xmlDoc.createElement("graphics");
            nameEl.appendChild(graph1El);
            Element offsetEl = xmlDoc.createElement("offset");
            graph1El.appendChild(offsetEl);
            offsetEl.setAttribute("x", (Float.valueOf(place.x).intValue())+"");
            offsetEl.setAttribute("y", (Float.valueOf(place.y).intValue()+Float.valueOf(place.h).intValue()+5)+"");
            Element graph2El = xmlDoc.createElement("graphics");
            placeEl.appendChild(graph2El);
            Element positionEl = xmlDoc.createElement("position");
            graph2El.appendChild(positionEl);
            positionEl.setAttribute("x", (Float.valueOf(place.x).intValue())+"");
            positionEl.setAttribute("y", (Float.valueOf(place.y).intValue())+"");
            Element dimensionEl = xmlDoc.createElement("dimension");
            graph2El.appendChild(dimensionEl);
            dimensionEl.setAttribute("x", (Float.valueOf(place.w).intValue())+"");
            dimensionEl.setAttribute("y", (Float.valueOf(place.h).intValue())+"");
            if(place.numToken>0){
                Element initialMarkingEl = xmlDoc.createElement("initialMarking");
                placeEl.appendChild(initialMarkingEl);
                Element text1El = xmlDoc.createElement("text");
                initialMarkingEl.appendChild(text1El);
                text1El.setTextContent(place.numToken+"");
            }
        }
        
        for(T transition:pn.getTransitionList_safe()){
            Element transitionEl = xmlDoc.createElement("transition");
            netPageEl.appendChild(transitionEl);
            transitionEl.setAttribute("id", transition.name);
            
            Element toolSpecificEl = xmlDoc.createElement("toolspecific");
            transitionEl.appendChild(toolSpecificEl);
            toolSpecificEl.setAttribute("tool", "org.adoxx");
            toolSpecificEl.setAttribute("version", "1");
            for(Entry<String, String> entry:transition.additionalInfoList.entrySet()){
                Element additionalAttrEl = xmlDoc.createElement(entry.getKey());
                toolSpecificEl.appendChild(additionalAttrEl);
                additionalAttrEl.setTextContent(entry.getValue());
            }
            
            Element nameEl = xmlDoc.createElement("name");
            transitionEl.appendChild(nameEl);
            Element textEl = xmlDoc.createElement("text");
            nameEl.appendChild(textEl);
            textEl.setTextContent(transition.name);
            Element graph1El = xmlDoc.createElement("graphics");
            nameEl.appendChild(graph1El);
            Element offsetEl = xmlDoc.createElement("offset");
            graph1El.appendChild(offsetEl);
            offsetEl.setAttribute("x", (Float.valueOf(transition.x).intValue())+"");
            offsetEl.setAttribute("y", (Float.valueOf(transition.y).intValue()+Float.valueOf(transition.h).intValue()+5)+"");
            Element graph2El = xmlDoc.createElement("graphics");
            transitionEl.appendChild(graph2El);
            Element positionEl = xmlDoc.createElement("position");
            graph2El.appendChild(positionEl);
            positionEl.setAttribute("x", (Float.valueOf(transition.x).intValue())+"");
            positionEl.setAttribute("y", (Float.valueOf(transition.y).intValue())+"");
            Element dimensionEl = xmlDoc.createElement("dimension");
            graph2El.appendChild(dimensionEl);
            dimensionEl.setAttribute("x", (Float.valueOf(transition.w).intValue())+"");
            dimensionEl.setAttribute("y", (Float.valueOf(transition.h).intValue())+"");
        }
        ArrayList<TP> connTP = pn.getConnectionTPList_safe();
        for(int i=0;i<connTP.size();i++){
            TP arc = connTP.get(i);
            Element arcEl = xmlDoc.createElement("arc");
            netPageEl.appendChild(arcEl);
            arcEl.setAttribute("id", "arctp"+i);
            arcEl.setAttribute("source", arc.source.name);
            arcEl.setAttribute("target", arc.target.name);
            
            Element toolSpecificEl = xmlDoc.createElement("toolspecific");
            arcEl.appendChild(toolSpecificEl);
            toolSpecificEl.setAttribute("tool", "org.adoxx");
            toolSpecificEl.setAttribute("version", "1");
            for(Entry<String, String> entry:arc.additionalInfoList.entrySet()){
                Element additionalAttrEl = xmlDoc.createElement(entry.getKey());
                toolSpecificEl.appendChild(additionalAttrEl);
                additionalAttrEl.setTextContent(entry.getValue());
            }
            
            Element inscriptionEl = xmlDoc.createElement("inscription");
            arcEl.appendChild(inscriptionEl);
            Element textEl = xmlDoc.createElement("text");
            inscriptionEl.appendChild(textEl);
            textEl.setTextContent(arc.weight+"");
        }
        
        ArrayList<PT> connPT = pn.getConnectionPTList_safe();
        for(int i=0;i<connPT.size();i++){
            PT arc = connPT.get(i);
            Element arcEl = xmlDoc.createElement("arc");
            netPageEl.appendChild(arcEl);
            arcEl.setAttribute("id", "arcpt"+i);
            arcEl.setAttribute("source", arc.source.name);
            arcEl.setAttribute("target", arc.target.name);
            
            Element toolSpecificEl = xmlDoc.createElement("toolspecific");
            arcEl.appendChild(toolSpecificEl);
            toolSpecificEl.setAttribute("tool", "org.adoxx");
            toolSpecificEl.setAttribute("version", "1");
            for(Entry<String, String> entry:arc.additionalInfoList.entrySet()){
                Element additionalAttrEl = xmlDoc.createElement(entry.getKey());
                toolSpecificEl.appendChild(additionalAttrEl);
                additionalAttrEl.setTextContent(entry.getValue());
            }
            
            Element inscriptionEl = xmlDoc.createElement("inscription");
            arcEl.appendChild(inscriptionEl);
            Element textEl = xmlDoc.createElement("text");
            inscriptionEl.appendChild(textEl);
            textEl.setTextContent(arc.weight+"");
            Element graphEl = xmlDoc.createElement("graphics");
            arcEl.appendChild(graphEl);
        }
        
        return XMLUtils.getStringFromXmlDoc(xmlDoc);
    }
}
