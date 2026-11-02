package hu.mik.pte.prog5.zh1vaadin.ui.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import hu.mik.pte.prog5.zh1vaadin.entity.Pet;
import hu.mik.pte.prog5.zh1vaadin.service.PetService;
import hu.mik.pte.prog5.zh1vaadin.service.SecurityService;
import hu.mik.pte.prog5.zh1vaadin.ui.MainLayout;
import jakarta.annotation.security.PermitAll;

@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
@Route(value = "list", layout = MainLayout.class)
public class ListView extends VerticalLayout implements HasDynamicTitle {

   public ListView(PetService petService, SecurityService securityService){
       setSizeFull();
       Grid<Pet> grid = new Grid<>(Pet.class,false);
       grid.addColumn(Pet::getUid)
               .setHeader(getTranslation("pet.id"))
               .setVisible(securityService.getRolesAsString().contains("ADMIN"));
       grid.addColumn(Pet::getName).setHeader(getTranslation("pet.name"));
       grid.addColumn(Pet::getSpecies).setHeader(getTranslation("pet.species"));
       grid.setItems(petService.listAll());
       grid.setSizeFull();
       add(new H2(getTranslation("title.pet.list")), grid);
       expand(grid);
   }


    @Override
    public String getPageTitle() {
        return getTranslation("title.pet.list");
    }
}
