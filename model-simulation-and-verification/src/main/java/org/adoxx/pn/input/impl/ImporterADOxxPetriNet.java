package org.adoxx.pn.input.impl;

import javax.xml.xpath.XPathConstants;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.input.ImporterXmlI;
import org.adoxx.pn.input.mapping.MappingHelper;
import org.adoxx.pn.input.mapping.data.GeneratedElements;
import org.adoxx.utils.Utils;
import org.adoxx.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ImporterADOxxPetriNet implements ImporterXmlI{

    @Override
    public String getName() {
        return "ADOXX-PETRINET";
    }
    
    @Override
    public boolean isCompliant(Document modelXml) {
        try{
            String queryRoot = "/*/*/*[@modeltype='Petri Net']";
            Node bpmnRootNode =  (Node) XMLUtils.execXPath(modelXml.getDocumentElement(), queryRoot, XPathConstants.NODE);
            if(bpmnRootNode!=null)
                return true;
        }catch(Exception e){}
        return false;
    }

    @Override
    public PetriNet[] generatePetriNet(Document adoxxXml) throws Exception {
        
        if(!isCompliant(adoxxXml))
            throw new Exception("The model is not a Petri Net in ADOxx XML format!");
                
        MappingHelper pnm = new MappingHelper();
        
        pnm.addMapping("p : p ; in:connection=p ; out:connection=p");
        pnm.addMapping("t : t ; in:connection=t ; out:connection=t");
        
        String placeQuery = "./*[@class='Place' or @class='Place (PN)']";
        String transitionQuery = "./*[@class='Transition' or @class='Transition (PN)']";
        String relationQuery = "./*[@class='P2TRelation' or @class='T2PRelation' or @class='Arc (PN)']";
        
        NodeList pnModelElList =  (NodeList) XMLUtils.execXPath(adoxxXml.getDocumentElement(), "/*/*/*[@modeltype='Petri Net']", XPathConstants.NODESET);
        PetriNet[] ret = new PetriNet[pnModelElList.getLength()];
        
        for(int modelIndex=0;modelIndex<pnModelElList.getLength();modelIndex++) {
            
            pnm.resetProcessed();
            Node pnModelEl = pnModelElList.item(modelIndex);
            String modelId = pnModelEl.getAttributes().getNamedItem("id").getNodeValue();
            String modelName = pnModelEl.getAttributes().getNamedItem("name").getNodeValue();
            
            NodeList placeNodeList =  (NodeList) XMLUtils.execXPath(pnModelEl, placeQuery, XPathConstants.NODESET);
            for(int i=0;i<placeNodeList.getLength();i++){
                String id = placeNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = placeNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                float[] xy = getAdoxxElementCoordinatesXY(pnModelEl, id);
                String nTokenS = (String) XMLUtils.execXPath(placeNodeList.item(i), "./*[@name='NumOfTokens' or @name='Tokens']", XPathConstants.STRING);
                int nToken = 0;
                try{
                    nToken = Integer.parseInt(nTokenS);
                }catch(Exception ex){}
                String nCapacityS = (String) XMLUtils.execXPath(placeNodeList.item(i), "./*[@name='Capacity']", XPathConstants.STRING);
                int nCapacity = -1;
                try{
                    nCapacity = Integer.parseInt(nCapacityS);
                }catch(Exception ex){}
                GeneratedElements genList = pnm.processElement(id, "p", id, xy[0], xy[1]);
                
                String costsString = (String) XMLUtils.execXPath(placeNodeList.item(i), ".//*[@name='Property' and text()='cost']/../*[@name='Value']", XPathConstants.STRING);
                String executionTimeString = (String) XMLUtils.execXPath(placeNodeList.item(i), ".//*[@name='Property' and text()='executionTime']/../*[@name='Value']", XPathConstants.STRING);
                if(costsString.isEmpty()) 
                    costsString = "0";
                if(executionTimeString.isEmpty()) 
                    executionTimeString = "0";
                else if(Utils.isAdoxxDateTime(executionTimeString))
                    executionTimeString = "" + Utils.convertAdoxxDateTimeToMilliseconds(executionTimeString);

                if(genList.placeList.length>0){
                    genList.placeList[0].numToken = nToken;
                    genList.placeList[0].capacity = nCapacity;

                    genList.placeList[0].addInfo("name", name);
                    genList.placeList[0].addInfo("isEntryPoint", "true");

                    genList.placeList[0].addInfo("executionTime", executionTimeString);
                    genList.placeList[0].addInfo("cost", costsString);
                }
            }
            
            NodeList transitionNodeList =  (NodeList) XMLUtils.execXPath(pnModelEl, transitionQuery, XPathConstants.NODESET);
            for(int i=0;i<transitionNodeList.getLength();i++){
                String id = transitionNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                String name = transitionNodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                
                float[] xy = getAdoxxElementCoordinatesXY(pnModelEl, id);
                GeneratedElements genList = pnm.processElement(id, "t", id, xy[0], xy[1]);
                if(genList.transitionList.length>0){
                    genList.transitionList[0].addInfo("name", name);
                }
            }
            
            NodeList relationNodeList =  (NodeList) XMLUtils.execXPath(pnModelEl, relationQuery, XPathConstants.NODESET);
            for(int i=0;i<relationNodeList.getLength();i++){
                String id = relationNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
                
                String sourceName = (String) XMLUtils.execXPath(relationNodeList.item(i), "./*[local-name()='FROM' or local-name()='from']/@instance", XPathConstants.STRING);
                String targetName = (String) XMLUtils.execXPath(relationNodeList.item(i), "./*[local-name()='TO' or local-name()='to']/@instance", XPathConstants.STRING);
                String sourceId = (String) XMLUtils.execXPath(pnModelEl, "./*[@name="+XMLUtils.escapeXPathField(sourceName)+"]/@id", XPathConstants.STRING);
                String targetId = (String) XMLUtils.execXPath(pnModelEl, "./*[@name="+XMLUtils.escapeXPathField(targetName)+"]/@id", XPathConstants.STRING);

                String weight = (String) XMLUtils.execXPath(relationNodeList.item(i), "./*[@name='Weight']", XPathConstants.STRING);
                int weightN = 1;
                try{
                    weightN = Integer.parseInt(weight);
                }catch(Exception ex){}
                if(sourceId.isEmpty()) throw new Exception("ERROR: Can not identify the element with name " + sourceName + " for the relation " + id);
                if(targetId.isEmpty()) throw new Exception("ERROR: Can not identify the element with name " + targetName + " for the relation " + id);
                
                GeneratedElements elList = pnm.processRelation(id, "connection", sourceId, targetId);
                if(elList.ptList.length>0){
                    elList.ptList[0].weight = weightN;
                }
                if(elList.tpList.length>0){
                    elList.tpList[0].weight = weightN;
                }
            }
        
            ret[modelIndex] = pnm.generatePN(modelId);
            ret[modelIndex].additionalInfoList.put("model_name", modelName);
        }
        
        return ret;
    }
    
    private float[] getAdoxxElementCoordinatesXY(Node pnModelEl, String elementId) throws Exception{
        String position = (String) XMLUtils.execXPath(pnModelEl, "./*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[@name='Position']", XPathConstants.STRING);
        String x = position.substring(position.indexOf("x:")+2);
        x = x.substring(0, x.indexOf("cm"));
        String y = position.substring(position.indexOf("y:")+2);
        y = y.substring(0, y.indexOf("cm"));
        return new float[]{Float.valueOf(x)*29, Float.valueOf(y)*29};
    }

}
