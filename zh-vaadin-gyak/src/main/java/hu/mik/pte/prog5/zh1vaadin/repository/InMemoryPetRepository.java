package hu.mik.pte.prog5.zh1vaadin.repository;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository("mock")
public class InMemoryPetRepository implements PetRepository {

    private final List<Pet> petList;

    public InMemoryPetRepository() {
        petList = new ArrayList<Pet>(
                List.of(
                        new Pet(UUID.randomUUID().toString(), "Cirmos", PetSpecies.CAT),
                        new Pet(UUID.randomUUID().toString(), "Pityu", PetSpecies.PARROT),
                        new Pet(UUID.randomUUID().toString(), "Ubul", PetSpecies.DOG),
                        new Pet(UUID.randomUUID().toString(), "Bandita", PetSpecies.FERRET),
                        new Pet(UUID.randomUUID().toString(), "Béla", PetSpecies.TURTLE)
                )
        );
    }

    @Override
    public List<Pet> findAll() {
        return new ArrayList<>(this.petList);
    }

    @Override
    public List<Pet> findByName(String name) {
        return this.petList.stream()
                .filter(pet -> pet.getName().equals(name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Pet> findBySpecies(PetSpecies species) {
        return this.petList.stream()
                .filter(p -> p.getSpecies()
                        .equals(species))
                .collect(Collectors.toList());
    }

    @Override
    public Pet save(Pet pet) {
        if (pet.getUid() == null) {
            pet.setUid(UUID.randomUUID().toString());
        } else {
            this.petList.remove(this.petList.stream().filter(p -> p.getUid().equals(pet.getUid())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Pet with id: " + pet.getUid() + " not found!")));
        }
        this.petList.add(pet);
        return pet;
    }
}
