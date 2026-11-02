package hu.mik.pte.prog5.zh1vaadin.ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.entity.PetSpecies;
import hu.mik.pte.prog5.zh1vaadin.service.PetService;
import hu.mik.pte.prog5.zh1vaadin.ui.MainLayout;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "register", layout = MainLayout.class)
@RolesAllowed("REGISTER")
public class RegisterView extends VerticalLayout implements HasDynamicTitle {

    private final Binder<Pet> binder = new Binder<>(Pet.class);

    public RegisterView(PetService petService) {
        TextField name = new TextField(getTranslation("pet.name"));
        ComboBox<PetSpecies> species = new ComboBox<>(getTranslation("pet.species"));
        species.setItems(petService.listSpecies());
        species.setItemLabelGenerator(value -> getTranslation("pet.species." + value.name()));

        binder.forField(name)
                .asRequired(getTranslation("register.name.required"))
                .withValidator(value -> !value.isBlank(), getTranslation("register.name.required"))
                .bind(Pet::getName, Pet::setName);
        binder.forField(species)
                .asRequired(getTranslation("register.species.required"))
                .bind(Pet::getSpecies, Pet::setSpecies);

        Button save = new Button(getTranslation("register.submit"), event -> {
            Pet pet = new Pet();
            if (!binder.writeBeanIfValid(pet)) {
                Notification.show(getTranslation("register.validation.error"),
                                5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            pet.setName(pet.getName().strip());
            petService.save(pet);
            binder.readBean(new Pet());
            Notification.show(getTranslation("register.success"),
                            3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        FormLayout form = new FormLayout(name, species);
        form.setMaxWidth("600px");
        binder.readBean(new Pet());
        add(new H2(getTranslation("title.pet.register")), form, save);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("title.pet.register");
    }
}
