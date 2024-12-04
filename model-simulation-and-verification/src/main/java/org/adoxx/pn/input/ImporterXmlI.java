package org.adoxx.pn.input;

import org.adoxx.pn.PetriNet;
import org.w3c.dom.Document;

public interface ImporterXmlI {
    public String getName();
    public boolean isCompliant(Document modelXml);
    public PetriNet[] generatePetriNet(Document modelXml) throws Exception;
}
