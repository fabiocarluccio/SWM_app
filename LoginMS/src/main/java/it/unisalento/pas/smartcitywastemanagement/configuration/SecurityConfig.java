package it.unisalento.pas.smartcitywastemanagement.configuration;

import it.unisalento.pas.smartcitywastemanagement.security.JwtAuthenticationFilter;
import it.unisalento.pas.smartcitywastemanagement.service.CustomUserDetailsService;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors().and().csrf().disable()
                .authorizeRequests().requestMatchers("/api/authentication/getall").permitAll().
                requestMatchers("/api/authentication/authenticate").permitAll().
                requestMatchers("/api/authentication/password_update").permitAll().
                requestMatchers("/api/authentication/password_reset").permitAll().
                requestMatchers("/api/authentication/password_reset_token").permitAll().
                requestMatchers("/api/authentication/citizen_registration").permitAll().
                requestMatchers("/api/authentication/registration").permitAll().
                anyRequest().authenticated().and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
        /*
        return http.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/api/users/**").authenticated() // necessaria l'autenticazione
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/api/bins/**").permitAll() // non necessaria l'autenticazione
                .and()
                .httpBasic(Customizer.withDefaults())
                .build();
        */
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();

    }



    /*
    @Bean
    public UserDetailsService userDetailsService() {




        UserDetails fabio = User.builder() // questo User non e' quello cheabbiamo creato noi. ma e1 una classe
                .username("fabio")
                .password(passwordEncoder().encode("12345"))
                .roles("ADMIN")
                .build();
        UserDetails paolo = User.builder()
                .username("roberto")
                .password(passwordEncoder().encode("12345"))
                .roles("USER")
                .build();


        return new InMemoryUserDetailsManager(fabio, paolo);

    }*/
}
