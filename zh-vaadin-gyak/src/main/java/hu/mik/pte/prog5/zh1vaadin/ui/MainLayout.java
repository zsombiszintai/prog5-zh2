package hu.mik.pte.prog5.zh1vaadin.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import hu.mik.pte.prog5.zh1vaadin.service.SecurityService;
import hu.mik.pte.prog5.zh1vaadin.ui.view.ListView;
import hu.mik.pte.prog5.zh1vaadin.ui.view.RegisterView;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.core.userdetails.UserDetails;

@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout(AuthenticationContext authenticationContext, SecurityService securityService){
        String username = authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername).orElse("");
        Span user = new Span(getTranslation("header.username") + " " + username);
        RouterLink listLink = new RouterLink(getTranslation("header.menu.pet.list"), ListView.class);
        RouterLink registerLink = new RouterLink(getTranslation("header.menu.pet.register"), RegisterView.class);
        registerLink.setVisible(securityService.getRolesAsString().contains("REGISTER"));
        Button logout = new Button(getTranslation("header.menu.logout"),
                event -> authenticationContext.logout());
        HorizontalLayout header = new HorizontalLayout(user, listLink, registerLink, logout);
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.expand(user);
        header.getStyle().set("flex-wrap","wrap");
        addToNavbar(header);

    }

}
