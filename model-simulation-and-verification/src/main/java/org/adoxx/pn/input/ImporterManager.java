package org.adoxx.pn.input;

import java.util.ArrayList;
import java.util.List;

import org.adoxx.pn.PetriNet;
import org.adoxx.pn.input.impl.ImporterADONPBPMN;
import org.adoxx.pn.input.impl.ImporterADOxxBPMN;
import org.adoxx.pn.input.impl.ImporterADOxxPetriNet;
import org.adoxx.pn.input.impl.ImporterBeeUPBPMN;
import org.adoxx.pn.input.impl.ImporterOMGBPMN;
import org.adoxx.pn.input.impl.ImporterPNML;
import org.adoxx.utils.Utils;
import org.adoxx.utils.XMLUtils;
import org.w3c.dom.Document;

public class ImporterManager {
    
    List<ImporterXmlI> pnImporterXmlList = new ArrayList<ImporterXmlI>();
    List<ImporterI> pnImporterList = new ArrayList<ImporterI>();
    
    public ImporterManager() {
        pnImporterXmlList.add(new ImporterADONPBPMN());
        pnImporterXmlList.add(new ImporterBeeUPBPMN());
        pnImporterXmlList.add(new ImporterOMGBPMN());
        pnImporterXmlList.add(new ImporterADOxxBPMN());
        pnImporterXmlList.add(new ImporterADOxxPetriNet());
        pnImporterXmlList.add(new ImporterPNML());
    }

    public void addImporter(ImporterI importer){
        pnImporterList.add(importer);
    }
    
    public void addImporter(ImporterXmlI importer){
        pnImporterXmlList.add(importer);
    }
    
    public ArrayList<String> getAllImporterNames(){
        ArrayList<String> ret = new ArrayList<String>();
        for(ImporterXmlI pnImporter:pnImporterXmlList)
            ret.add(pnImporter.getName());
        for(ImporterI pnImporter:pnImporterList)
            ret.add(pnImporter.getName());
        
        return ret;
    }
    
    public PetriNet[] generateFromModel(String model) throws Exception{
        
        Document modelXml = null;
        try{
            modelXml = XMLUtils.getXmlDocFromString(model);
        }catch(Exception ex){
            ex.printStackTrace(); 
            Utils.log(ex);
        }
        
        if(modelXml != null)
            for(ImporterXmlI pnImporter:pnImporterXmlList)
                if(pnImporter.isCompliant(modelXml))
                    return pnImporter.generatePetriNet(modelXml);
        
        for(ImporterI pnImporter:pnImporterList)
            if(pnImporter.isCompliant(model))
                return pnImporter.generatePetriNet(model);
        throw new Exception("ERROR: The model format can not be recognized!");
    }
    
    public PetriNet[] generateFromModel(String model, String modelType) throws Exception{
        
        try{
            Document modelXml = XMLUtils.getXmlDocFromString(model);
            for(ImporterXmlI pnImporter:pnImporterXmlList)
                if(pnImporter.getName().equals(modelType))
                    return pnImporter.generatePetriNet(modelXml);
        }catch(Exception ex){}
        
        for(ImporterI pnImporter:pnImporterList)
            if(pnImporter.getName().equals(modelType))
                return pnImporter.generatePetriNet(model);
        throw new Exception("ERROR: The model format "+modelType+" can not be recognized!");
    }
    
    /*
    public static void main(String[] args) {
        try {
            String modelUrl = "D:\\SIMULATOR\\TEST MODELS\\simple bpmn (en).bpmn";

            PetriNet[] pnList = new ImporterManager().generateFromModel(new String(org.adoxx.utils.IOUtils.readFile(modelUrl), "UTF-8"));

            for(PetriNet pn: pnList)
                System.out.println(org.adoxx.pn.output.ExporterPNML.exportTo_PNML(pn));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}
