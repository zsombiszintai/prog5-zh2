package hu.mik.pte.prog5.zh1vaadin.config;

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import hu.mik.pte.prog5.zh1vaadin.ui.view.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.with(VaadinSecurityConfigurer.vaadin(), vaadinSecurityConfigurer -> vaadinSecurityConfigurer.loginView(LoginView.class));
        return httpSecurity.build();
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        var admin = User.builder()
                .username("admin")
                .password("{noop}password")
                .roles("ADMIN","REGISTER","CAT","DOG","PARROT","TURTLE","FERRET")
                .build();
        var dog = User.builder()
                .username("dog")
                .password("{noop}password")
                .roles("DOG")
                .build();
        var cat = User.builder()
                .username("cat")
                .password("{noop}password")
                .roles("REGISTER","DOG")
                .build();
        var uncommon = User.builder()
                .username("uncommon")
                .password("{noop}password")
                .roles("TURTLE","PARROT","FERRET")
                .build();
        return new InMemoryUserDetailsManager(admin, dog, cat, uncommon);
    }
}
