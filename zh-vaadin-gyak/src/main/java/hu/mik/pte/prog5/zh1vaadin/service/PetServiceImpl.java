package hu.mik.pte.prog5.zh1vaadin.service;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import hu.mik.pte.prog5.zh1vaadin.repository.PetRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;

    public PetServiceImpl(@Qualifier("mock") PetRepository petRepository){
        this.petRepository = petRepository;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @PostFilter("@securityService.getRolesAsString().contains(filterObject.species.name())")
    public List<Pet> listAll() {
        return petRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('REGISTER')")
    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    @Override
    @PreAuthorize("hasRole('REGISTER')")
    public List<PetSpecies> listSpecies() {
        return List.of(PetSpecies.values());
    }
}
