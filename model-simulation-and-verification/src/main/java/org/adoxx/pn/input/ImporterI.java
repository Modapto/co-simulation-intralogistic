package org.adoxx.pn.input;

import org.adoxx.pn.PetriNet;

public interface ImporterI {
    public String getName();
    public boolean isCompliant(String model);
    public PetriNet[] generatePetriNet(String model) throws Exception;
}
