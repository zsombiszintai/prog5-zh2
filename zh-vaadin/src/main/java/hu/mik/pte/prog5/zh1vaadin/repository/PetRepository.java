package hu.mik.pte.prog5.zh1vaadin.repository;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;

import java.util.List;

public interface PetRepository {

    List<Pet> findAll();

    List<Pet> findByName(String name);

    List<Pet> findBySpecies(PetSpecies species);

    Pet save(Pet pet);

}
