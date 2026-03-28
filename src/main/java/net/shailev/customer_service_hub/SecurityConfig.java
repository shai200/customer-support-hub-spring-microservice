package net.shailev.customer_service_hub;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

        private static final String ADMIN = RoleType.ADMIN.name();
        private static final String AGENT = RoleType.AGENT.name();
        private static final String CUSTOMER = RoleType.CUSTOMER.name();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/alive", "/oauth/token").permitAll()
                                                .requestMatchers("/admin/**").hasRole(ADMIN)
                                                .requestMatchers("/agent/**").hasAnyRole(ADMIN, AGENT)
                                                .requestMatchers("/tickets/**").hasAnyRole(ADMIN, AGENT, CUSTOMER)
                                                .anyRequest().hasAnyRole(ADMIN, AGENT, CUSTOMER)
                )
                                .oauth2ResourceServer(resourceServer -> resourceServer
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                                )
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public SecretKey jwtSecretKey(@Value("${security.jwt.secret}") String secret) {
                return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        }

        @Bean
        public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
                return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                                .algorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
                return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();
        }

        @Bean
        public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return jwtAuthenticationConverter;
        }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername("admin")
                .password("{noop}admin11")
                .roles(ADMIN)
                .build();

        UserDetails developer = User.withUsername("developer")
                .password("{noop}developer11")
                .roles(AGENT)
                .build();

        UserDetails agent = User.withUsername("agent")
                .password("{noop}agent11")
                .roles(AGENT)
                .build();

        UserDetails customer = User.withUsername("customer")
                .password("{noop}java11")
                .roles(CUSTOMER)
                .build();

        return new InMemoryUserDetailsManager(admin, developer, agent, customer);
    }
}
