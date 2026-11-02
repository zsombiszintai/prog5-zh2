package hu.mik.pte.prog5.zh1vaadin.service;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;

import java.util.List;

public interface PetService {

    List<Pet> listAll();

    Pet save(Pet pet);

    List<PetSpecies> listSpecies();
}
