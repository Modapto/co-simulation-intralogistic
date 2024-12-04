package org.adoxx.pn.input.mapping;

import java.util.ArrayList;

import org.adoxx.pn.P;
import org.adoxx.pn.PT;
import org.adoxx.pn.PetriNet;
import org.adoxx.pn.T;
import org.adoxx.pn.TP;
import org.adoxx.pn.input.mapping.data.GeneratedElements;
import org.adoxx.pn.input.mapping.data.MapElement;
import org.adoxx.pn.input.mapping.data.MapElement.FromTo;
import org.adoxx.pn.input.mapping.data.MapElement.RelationElement;

public class MappingHelper {
    
    class ProcessedElement{
        String elementId = "";
        String elementType = "";
        String elementDescription = "";
        float x = 0, y = 0;
        public ProcessedElement(String elementId, String elementType, String elementDescription, float x, float y){
            this.elementId = elementId; this.elementType = elementType; this.elementDescription = elementDescription; this.x = x; this.y = y;
        } 
    }
    
    private ArrayList<MapElement> mapElementList = new ArrayList<MapElement>();
    private ArrayList<ProcessedElement> processedElementList = new ArrayList<ProcessedElement>();
    private PetriNet petriNet = new PetriNet("");
    
    public void resetProcessed(){
        processedElementList.clear();
        petriNet = new PetriNet("");
    }
    
    private MapElement getMapElement(String type) throws Exception{
        if(type==null || type.isEmpty())
            throw new Exception("ERROR: parameter type is empty");
        for(MapElement element:mapElementList)
            for(String typeEl: element.typeList)
                if(typeEl.equals(type))
                    return element;
        throw new Exception("ERROR: element " + type + " not defined.");
    }
    
    private String getFirstExistentMapElement(String[] typeList) throws Exception{
        if(typeList==null)
            throw new Exception("ERROR: parameter typeList is null");
        for(String type: typeList)
            if(existMapElement(type))
                return type;
        return null;
    }
    
    private boolean existMapElement(String[] typeList) throws Exception{
        if(typeList==null)
            throw new Exception("ERROR: parameter typeList is null");
        for(String type: typeList)
            if(existMapElement(type))
                return true;
        return false;
    }
    
    private boolean existMapElement(String type) throws Exception{
        if(type==null || type.isEmpty())
            throw new Exception("ERROR: parameter type is empty");
        for(MapElement element:mapElementList)
            for(String typeEl: element.typeList)
                if(typeEl.equals(type))
                    return true;
        return false;
    }
    
    public void addMapping(String mappingFormula) throws Exception{
        /*
         *  Formula Syntax: 
         *  Formula = MapRules ";" InputRelations ";" OutputRelations
         *  MapRules = elementTypeList ":" RuleList
         *  InputRelations = "in :" relationList
         *  OutputRelations = "out :" relationList
         *  elementTypeList = ElementTypeName [ "|" elementTypeList ]
         *  ElementTypeName: the type name of the object you want to map
         *  RuleList = Rule [ "," RuleList ]
         *  Rule = from ">" to | from
         *  from: name of the PN element you want to create/use for connection with the "to" element; this name have to start with "p" in order to refer to a place or with "t" in order to refer to a transition. In case of place is it possible to specify the number of token between parenthesis "(numtoken)"
         *  to: name of the PN element you want to create/use for connection with the "from" element; this name have to start with "p" in order to refer to a place or with "t" in order to refer to a transition. In case of place is it possible to specify the number of token between parenthesis "(numtoken)"
         *  relationList = relation [ "," relationList]
         *  relation = RelationType "=" PNElementName
         *  RelationType: name of the relation type you want to map
         *  PNElementName: name of the PN element defined in the from/to you want to use for the relation
         *  
         *  es: 
         *  Start: p(1)>t ; in:message=p ; out: sequence=t
         *  Task|Intermediate: p>t ; in: sequence=p, message=t ; out: sequence=t, message=t
         *  End: p ; in: sequence=p ; out:
         *  msgEnd: p>t,t>p1 ; in: sequence=p ; out: message=t
         * */
        
        if(mappingFormula==null)
            throw new Exception("ERROR: Formula is null");
        
        MapElement element = new MapElement();
        mappingFormula = mappingFormula.replaceAll("( |-)+", "");
        
        String[] formulaSentenceList = mappingFormula.split(";");
        
        if(formulaSentenceList.length!=3)
            throw new Exception("ERROR: Formula Syntax Error; Expected 3 \";\" in the formula");
        
        element.typeList = formulaSentenceList[0].split(":")[0].split("\\|");
        
        String[] ruleList = new String[0];
        if(formulaSentenceList[0].split(":").length>1)
            ruleList = formulaSentenceList[0].split(":")[1].split(",");
        
        ArrayList<FromTo> fromToListA = new ArrayList<FromTo>();
        for(String rule:ruleList){
            FromTo fromTo = element.new FromTo();
            fromTo.from = rule.split(">")[0];
            if(fromTo.from.contains("(")){
                fromTo.placeToken = Integer.parseInt(fromTo.from.substring(fromTo.from.indexOf("(")+1, fromTo.from.indexOf(")")));
                fromTo.from = fromTo.from.split("\\(")[0];
            }
            if(rule.split(">").length>1){
                fromTo.to = rule.split(">")[1];
                if(fromTo.to.contains("(")){
                    fromTo.placeToken = Integer.parseInt(fromTo.to.substring(fromTo.to.indexOf("(")+1, fromTo.to.indexOf(")")));
                    fromTo.to = fromTo.to.split("\\(")[0];
                }
            }
            fromToListA.add(fromTo);
        }
        element.mappingFromToList = new FromTo[fromToListA.size()];
        fromToListA.toArray(element.mappingFromToList);
        
        
        ArrayList<RelationElement> inRelationElementListA = new ArrayList<RelationElement>();
        String[] inRelElRowList = new String[0];
        if(formulaSentenceList[1].split(":").length>1)
            inRelElRowList = formulaSentenceList[1].split(":")[1].split(",");
        for(String relElRow: inRelElRowList){
            RelationElement relationElement = element.new RelationElement();
            if(relElRow.split("=").length!=2)
                throw new Exception("ERROR: Formula Syntax Error; Expected 1 \"=\" in the formula: " + relElRow);
            relationElement.relationType = relElRow.split("=")[0];
            relationElement.element = relElRow.split("=")[1];
            inRelationElementListA.add(relationElement);
        }
        element.inRelationElementList = new RelationElement[inRelationElementListA.size()];
        inRelationElementListA.toArray(element.inRelationElementList);
        
        ArrayList<RelationElement> outRelationElementListA = new ArrayList<RelationElement>();
        String[] outRelElRowList = new String[0];
        if(formulaSentenceList[2].split(":").length>1)
            outRelElRowList = formulaSentenceList[2].split(":")[1].split(",");
        for(String relElRow: outRelElRowList){
            RelationElement relationElement = element.new RelationElement();
            if(relElRow.split("=").length!=2)
                throw new Exception("ERROR: Formula Syntax Error; Expected 1 \"=\" in the formula: " + relElRow);
            relationElement.relationType = relElRow.split("=")[0];
            relationElement.element = relElRow.split("=")[1];
            outRelationElementListA.add(relationElement);
        }
        element.outRelationElementList = new RelationElement[outRelationElementListA.size()];
        outRelationElementListA.toArray(element.outRelationElementList);
        
        addMapping(element);
    }
    
    public void addMapping(MapElement element) throws Exception{
        if(element==null)
            throw new Exception("ERROR: parameter element is null");
        if(existMapElement(element.typeList))
            throw new Exception("ERROR: Type " + getFirstExistentMapElement(element.typeList) + " defined more than once.");
        mapElementList.add(element);
    }
    
    public GeneratedElements processElement(String elementId, String elementType, String elementDescription, float x, float y) throws Exception{
        if(elementId==null || elementId.isEmpty() || elementType==null || elementType.isEmpty())
            throw new Exception("ERROR: incorrect data provided for processing element:\nelementId: "+elementId+"\nelementType: "+elementType+"\nelementDescription: "+elementDescription);
        return processElement(new ProcessedElement(elementId, elementType, elementDescription, x, y));
    }
    
    public GeneratedElements processElement(ProcessedElement element) throws Exception{
        if(element==null)
            throw new Exception("ERROR: parameter element is null");
        GeneratedElements ret = new GeneratedElements();
        
        MapElement map = getMapElement(element.elementType);
        
        ArrayList<P> retPLListA = new ArrayList<P>();
        ArrayList<T> retTRListA = new ArrayList<T>();
        ArrayList<PT> retPTListA = new ArrayList<PT>();
        ArrayList<TP> retTPListA = new ArrayList<TP>();
        int index = 0;
        for(FromTo rule:map.mappingFromToList){

            String fromName = rule.from + element.elementId;
            String toName = "";
            if(!rule.to.isEmpty())
                toName = rule.to + element.elementId;
            
            if(fromName.toLowerCase().startsWith("p")){
                P from = null;
                T to = null;
                if(!petriNet.existPlace(fromName)){
                    from = petriNet.addPlace(fromName, rule.placeToken);
                    from.description = element.elementDescription;
                    from.x = element.x+"";
                    from.y = (element.y+(Float.valueOf(from.h)+20)*index) + "";
                    from.addInfo("elementType", element.elementType);
                    from.addInfo("elementId", element.elementId);
                    index++;
                }else
                    from = petriNet.getPlace(fromName);
                
                retPLListA.add(from);
                
                if(!rule.to.isEmpty()){
                    if(!petriNet.existTransition(toName)){
                        to = petriNet.addTransition(toName);
                        to.description = element.elementDescription;
                        to.x = element.x+"";
                        to.y = (element.y+(Float.valueOf(to.h)+20)*index) + "";
                        to.addInfo("elementType", element.elementType);
                        to.addInfo("elementId", element.elementId);
                        index++;
                    }else
                        to = petriNet.getTransition(toName);
                    retTRListA.add(to);
                    PT conn = petriNet.connect(from, to);
                    conn.addInfo("elementType", element.elementType);
                    conn.addInfo("elementId", element.elementId);
                    retPTListA.add(conn);
                }
            } else {
                T from = null;
                P to = null;
                if(!petriNet.existTransition(fromName)){
                    from = petriNet.addTransition(fromName);
                    from.description = element.elementDescription;
                    from.x = element.x+"";
                    from.y = (element.y+(Float.valueOf(from.h)+20)*index) + "";
                    from.addInfo("elementType", element.elementType);
                    from.addInfo("elementId", element.elementId);
                    index++;
                }else
                    from = petriNet.getTransition(fromName);
                retTRListA.add(from);
                
                if(!rule.to.isEmpty()){
                    if(!petriNet.existPlace(toName)){
                        to = petriNet.addPlace(toName, rule.placeToken);
                        to.description = element.elementDescription;
                        to.x = element.x+"";
                        to.y = (element.y+(Float.valueOf(to.h)+20)*index) + "";
                        to.addInfo("elementType", element.elementType);
                        to.addInfo("elementId", element.elementId);
                        index++;
                    }else
                        to = petriNet.getPlace(toName);
                    retPLListA.add(to);
                    TP conn = petriNet.connect(from, to);
                    conn.addInfo("elementType", element.elementType);
                    conn.addInfo("elementId", element.elementId);
                    retTPListA.add(conn);
                }
            }
        }
        ret.placeList = new P[retPLListA.size()];
        retPLListA.toArray(ret.placeList);
        ret.transitionList = new T[retTRListA.size()];
        retTRListA.toArray(ret.transitionList);
        ret.ptList = new PT[retPTListA.size()];
        retPTListA.toArray(ret.ptList);
        ret.tpList = new TP[retTPListA.size()];
        retTPListA.toArray(ret.tpList);
        processedElementList.add(element);
        return ret;
    }
    
    private ProcessedElement getProcessedElement(String elementId) throws Exception{
        if(elementId==null || elementId.isEmpty())
            throw new Exception("ERROR: parameter elementId is empty");
        for(ProcessedElement processedElement:processedElementList)
            if(processedElement.elementId.equals(elementId))
                return processedElement;
        throw new Exception("ERROR: element " + elementId + " is not been processed");
    }
    
    public GeneratedElements processRelation(String relationId, String relationType, String elementFromId, String elementToId) throws Exception{
        if(relationId==null || relationId.isEmpty() || relationType==null || relationType.isEmpty() || elementFromId==null || elementFromId.isEmpty() || elementToId==null || elementToId.isEmpty())
            throw new Exception("ERROR: incorrect data provided for processing relation:\nrelationId: "+relationId+"\nrelationType: " +relationType+"\nelementFromId: "+elementFromId+"\nelementToId: "+elementToId);
        
        GeneratedElements ret = new GeneratedElements();
        ArrayList<P> retPLListA = new ArrayList<P>();
        ArrayList<T> retTRListA = new ArrayList<T>();
        ArrayList<PT> retPTListA = new ArrayList<PT>();
        ArrayList<TP> retTPListA = new ArrayList<TP>();
        
        MapElement mapFrom = getMapElement(getProcessedElement(elementFromId).elementType);
        MapElement mapTo = getMapElement(getProcessedElement(elementToId).elementType);
        
        String mapFromElementPrefix = mapFrom.getOutRelationElement(relationType);
        String mapToElementPrefix = mapTo.getInRelationElement(relationType);
        
        String mapFromElementName = mapFromElementPrefix + elementFromId;
        String mapToElementName = mapToElementPrefix + elementToId;
        
        if(mapFromElementName.toLowerCase().startsWith("p")){
            if(mapToElementName.toLowerCase().startsWith("p")){
                P from = petriNet.getPlace(mapFromElementName);
                P to = petriNet.getPlace(mapToElementName);
                T intermediate = petriNet.addTransition("t" + relationId);
                PT conn1 = petriNet.connect(from, intermediate);
                TP conn2 = petriNet.connect(intermediate, to);
                intermediate.x = from.x;
                intermediate.y = (Float.valueOf(from.y) + Float.valueOf(from.h) + 20) + "";
                intermediate.description = from.description;
                intermediate.addInfo("relationType", relationType);
                intermediate.addInfo("relationId", relationId);
                conn1.addInfo("relationType", relationType);
                conn1.addInfo("relationId", relationId);
                conn2.addInfo("relationType", relationType);
                conn2.addInfo("relationId", relationId);
                retTRListA.add(intermediate);
                retPTListA.add(conn1);
                retTPListA.add(conn2);
            } else {
                P from = petriNet.getPlace(mapFromElementName);
                T to = petriNet.getTransition(mapToElementName);
                PT conn = petriNet.connect(from, to);
                conn.addInfo("relationType", relationType);
                conn.addInfo("relationId", relationId);
                retPTListA.add(conn);
            }
        } else {
            if(mapToElementName.toLowerCase().startsWith("p")){
                T from = petriNet.getTransition(mapFromElementName);
                P to = petriNet.getPlace(mapToElementName);
                TP conn = petriNet.connect(from, to);
                conn.addInfo("relationType", relationType);
                conn.addInfo("relationId", relationId);
                retTPListA.add(conn);
            } else {
                T from = petriNet.getTransition(mapFromElementName);
                T to = petriNet.getTransition(mapToElementName);
                P intermediate = petriNet.addPlace("p" + relationId);
                TP conn1 = petriNet.connect(from, intermediate);
                PT conn2 = petriNet.connect(intermediate, to);
                intermediate.x = from.x;
                intermediate.y = (Float.valueOf(from.y) + Float.valueOf(from.h) + 20) + "";
                intermediate.description = from.description;
                intermediate.addInfo("relationType", relationType);
                intermediate.addInfo("relationId", relationId);
                conn1.addInfo("relationType", relationType);
                conn1.addInfo("relationId", relationId);
                conn2.addInfo("relationType", relationType);
                conn2.addInfo("relationId", relationId);
                retPLListA.add(intermediate);
                retTPListA.add(conn1);
                retPTListA.add(conn2);
            }
        }
        
        ret.placeList = new P[retPLListA.size()];
        retPLListA.toArray(ret.placeList);
        ret.transitionList = new T[retTRListA.size()];
        retTRListA.toArray(ret.transitionList);
        ret.ptList = new PT[retPTListA.size()];
        retPTListA.toArray(ret.ptList);
        ret.tpList = new TP[retTPListA.size()];
        retTPListA.toArray(ret.tpList);
        
        return ret;
    }
    
    public PetriNet generatePN(String name) throws Exception{
        petriNet.setName(name);
        petriNet.finalizeModel();
        return petriNet;
    }
    
}
