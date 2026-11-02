package hu.mik.pte.prog5.zh1vaadin.entity;

import java.util.Objects;

public class Pet {

    private String uid;
    private String name;
    private PetSpecies species;

    public Pet(String uid, String name, PetSpecies species) {
        this.uid = uid;
        this.name = name;
        this.species = species;
    }

    public Pet() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PetSpecies getSpecies() {
        return species;
    }

    public void setSpecies(PetSpecies species) {
        this.species = species;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(uid, pet.uid) && Objects.equals(name, pet.name) && species == pet.species;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, species);
    }
}
