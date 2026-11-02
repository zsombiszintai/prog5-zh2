package hu.mik.pte.prog5.zh1vaadin;

import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import hu.mik.pte.prog5.zh1vaadin.repository.PetRepository;
import hu.mik.pte.prog5.zh1vaadin.service.PetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(PetServiceSecurityTests.TestConfig.class)
class PetServiceSecurityTests {

    @Configuration
    @EnableMethodSecurity
    @ComponentScan(basePackageClasses = {PetService.class, PetRepository.class})
    static class TestConfig {
    }

    @Autowired
    private PetService petService;

    @Autowired
    @Qualifier("mock")
    private PetRepository repository;

    @Test
    @WithMockUser(roles = "DOG")
    void dogRoleSeesOnlyDogsWithoutRemovingOtherPets() {
        int count = repository.findAll().size();
        List<Pet> pets = petService.listAll();
        assertFalse(pets.isEmpty());
        assertTrue(pets.stream().allMatch(pet -> pet.getSpecies() == PetSpecies.DOG));
        assertEquals(count, repository.findAll().size());
    }

    @Test
    @WithMockUser(roles = {"TURTLE", "PARROT", "FERRET"})
    void multipleRolesSeeExactlyTheirSpecies() {
        assertEquals(List.of(PetSpecies.PARROT, PetSpecies.FERRET, PetSpecies.TURTLE),
                petService.listAll().stream().map(Pet::getSpecies).toList());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "REGISTER", "CAT", "DOG", "PARROT", "TURTLE", "FERRET"})
    void allSpeciesRolesSeeAllPets() {
        assertEquals(repository.findAll(), petService.listAll());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminWithoutSpeciesRolesSeesNoPets() {
        assertTrue(petService.listAll().isEmpty());
    }

    @Test
    @WithMockUser(roles = "DOG")
    void registrationMethodsRejectUsersWithoutRegisterRole() {
        int count = repository.findAll().size();
        assertThrows(AccessDeniedException.class, petService::listSpecies);
        assertThrows(AccessDeniedException.class,
                () -> petService.save(new Pet(null, "Tiltott", PetSpecies.DOG)));
        assertEquals(count, repository.findAll().size());
    }

    @Test
    @WithMockUser(roles = "REGISTER")
    @DirtiesContext
    void registerRoleCanListSpeciesAndSave() {
        assertEquals(List.of(PetSpecies.values()), petService.listSpecies());
        int count = repository.findAll().size();
        Pet pet = new Pet(null, "Teszt", PetSpecies.CAT);
        assertSame(pet, petService.save(pet));
        assertNotNull(pet.getUid());
        assertEquals(count + 1, repository.findAll().size());
        assertTrue(petService.listAll().isEmpty());
    }
}
