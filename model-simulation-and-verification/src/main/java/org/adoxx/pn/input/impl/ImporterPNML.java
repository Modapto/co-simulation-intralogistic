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

public class ImporterPNML implements ImporterXmlI{

    @Override
    public String getName() {
        return "PNML-PETRINET";
    }
    
    @Override
    public boolean isCompliant(Document modelXml) {
        try{
            String queryRoot = "/*[local-name()='pnml']";
            Node pnmlRootNode =  (Node) XMLUtils.execXPath(modelXml.getDocumentElement(), queryRoot, XPathConstants.NODE);
            if(pnmlRootNode!=null)
                return true;
        }catch(Exception e){}
        return false;
    }

    @Override
    public PetriNet[] generatePetriNet(Document pnmlXml) throws Exception {

        if(!isCompliant(pnmlXml))
            throw new Exception("The model is not a Petri Net in PNML format!");
                
        MappingHelper pnm = new MappingHelper();
        
        pnm.addMapping("p : p ; in:connection=p ; out:connection=p");
        pnm.addMapping("t : t ; in:connection=t ; out:connection=t");        
        
        String placeQuery = "//*[local-name()='place']";
        String transitionQuery = "//*[local-name()='transition']";
        String relationQuery = "//*[local-name()='arc']";
        
        Node netEl = (Node) XMLUtils.execXPath(pnmlXml.getDocumentElement(), "//*[local-name()='net']", XPathConstants.NODE);
        String netId = netEl.getAttributes().getNamedItem("id").getNodeValue();
        
        NodeList placeNodeList =  (NodeList) XMLUtils.execXPath(pnmlXml.getDocumentElement(), placeQuery, XPathConstants.NODESET);
        for(int i=0;i<placeNodeList.getLength();i++){
            String id = placeNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = (String) XMLUtils.execXPath(placeNodeList.item(i), "./*[local-name()='name']/*[local-name()='text']", XPathConstants.STRING);
            
            float[] xy = getPNMLElementCoordinatesXY(pnmlXml, id);
            String nTokenS = (String) XMLUtils.execXPath(placeNodeList.item(i), "./*[local-name()='initialMarking']/*[local-name()='text']", XPathConstants.STRING);
            int nToken = 0;
            try{
                nToken = Integer.parseInt(nTokenS);
            }catch(Exception ex){}
            
            String costsString = (String) XMLUtils.execXPath(placeNodeList.item(i), "./*[local-name()='toolspecific']//*[local-name()='cost']", XPathConstants.STRING);
            String executionTimeString = (String) XMLUtils.execXPath(placeNodeList.item(i), "./*[local-name()='toolspecific']//*[local-name()='executionTime']", XPathConstants.STRING);
            if(costsString.isEmpty()) 
                costsString = "0";
            if(executionTimeString.isEmpty()) 
                executionTimeString = "0";
            else if(Utils.isAdoxxDateTime(executionTimeString))
                executionTimeString = "" + Utils.convertAdoxxDateTimeToMilliseconds(executionTimeString);
            
            GeneratedElements genList = pnm.processElement(id, "p", id, xy[0], xy[1]);
            
            if(genList.placeList.length>0){
                genList.placeList[0].numToken = nToken;
                genList.placeList[0].addInfo("executionTime", executionTimeString);
                genList.placeList[0].addInfo("cost", costsString);
                genList.placeList[0].addInfo("name", name);
                genList.placeList[0].addInfo("isEntryPoint", "true");
            }
        }
        
        NodeList transitionNodeList =  (NodeList) XMLUtils.execXPath(pnmlXml.getDocumentElement(), transitionQuery, XPathConstants.NODESET);
        for(int i=0;i<transitionNodeList.getLength();i++){
            String id = transitionNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            String name = (String) XMLUtils.execXPath(transitionNodeList.item(i), "./*[local-name()='name']/*[local-name()='text']", XPathConstants.STRING);
            
            float[] xy = getPNMLElementCoordinatesXY(pnmlXml, id);
            GeneratedElements genList = pnm.processElement(id, "t", id, xy[0], xy[1]);
            if(genList.transitionList.length>0){
                genList.transitionList[0].addInfo("name", name);
            }
        }
        
        NodeList relationNodeList =  (NodeList) XMLUtils.execXPath(pnmlXml.getDocumentElement(), relationQuery, XPathConstants.NODESET);
        for(int i=0;i<relationNodeList.getLength();i++){
            String id = relationNodeList.item(i).getAttributes().getNamedItem("id").getNodeValue();
            
            String sourceId = relationNodeList.item(i).getAttributes().getNamedItem("source").getNodeValue();
            String targetId = relationNodeList.item(i).getAttributes().getNamedItem("target").getNodeValue();

            String weight = (String) XMLUtils.execXPath(relationNodeList.item(i), "./*[local-name()='inscription']/*[local-name()='text']", XPathConstants.STRING);
            int weightN = 1;
            try{
                weightN = Integer.parseInt(weight);
            }catch(Exception ex){}
            
            String pathProbability = (String) XMLUtils.execXPath(relationNodeList.item(i), "./*[local-name()='toolspecific']//*[local-name()='pathProbability']", XPathConstants.STRING);
            
            GeneratedElements elList = pnm.processRelation(id, "connection", sourceId, targetId);
            if(elList.ptList.length>0){
                elList.ptList[0].weight = weightN;
                elList.ptList[0].addInfo("pathProbability", pathProbability);
            }
            if(elList.tpList.length>0)
                elList.tpList[0].weight = weightN;
        }
        PetriNet pn = pnm.generatePN(netId);
        pn.additionalInfoList.put("model_name", netId);
        return new PetriNet[]{pn};
    }

    private float[] getPNMLElementCoordinatesXY(Document pnmlXml, String elementId) throws Exception{
        Node position = (Node) XMLUtils.execXPath(pnmlXml.getDocumentElement(), "//*[@id=" + XMLUtils.escapeXPathField(elementId) + "]/*[local-name()='graphics']/*[local-name()='position']", XPathConstants.NODE);
        String x = position.getAttributes().getNamedItem("x").getNodeValue();
        String y = position.getAttributes().getNamedItem("y").getNodeValue();
        return new float[]{Float.valueOf(x), Float.valueOf(y)};
    }
}
