package net.shailev.customer_service_hub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        private static final String ADMIN = RoleType.ADMIN.name();
        private static final String AGENT = RoleType.AGENT.name();
        private static final String CUSTOMER = RoleType.CUSTOMER.name();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/alive").permitAll()
                                                .requestMatchers("/admin/**").hasRole(ADMIN)
                                                .requestMatchers("/agent/**").hasAnyRole(ADMIN, AGENT)
                                                .requestMatchers("/tickets/**").hasAnyRole(ADMIN, AGENT, CUSTOMER)
                                                .anyRequest().hasAnyRole(ADMIN, AGENT, CUSTOMER)
                )
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin11")
                .roles(ADMIN)
                .build();

        UserDetails agent = User.withUsername("agent")
                .password("{noop}agent11")
                .roles(AGENT)
                .build();

        UserDetails customer = User.withUsername("customer")
                .password("{noop}java11")
                .roles(CUSTOMER)
                .build();

        return new InMemoryUserDetailsManager(admin, agent, customer);
    }
}
