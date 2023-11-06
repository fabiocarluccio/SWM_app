package it.unisalento.pas.smartcitywastemanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import it.unisalento.pas.smartcitywastemanagement.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static it.unisalento.pas.smartcitywastemanagement.security.SecurityConstants.JWT_SECRET;
import static it.unisalento.pas.smartcitywastemanagement.security.SecurityConstants.THIS_MICROSERVICE;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtilities jwtUtilities ;

    @Autowired
    private CustomUserDetailsService customerUserDetailsService ; // questo è collegato al nostro db

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // mando una richiesta e mi prendo un header che si chiama "Authorization"
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // poi ci prendiamo il valore di questo header, che è: Bearer <spazio> TOKEN
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // mi prendo il token (dal 7o carattere in poi)
            // dal token mi estraggo lo username
            username = jwtUtilities.extractUsername(jwt);
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(JWT_SECRET)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();


            List<String> audience = claims.get("aud", List.class);
            String role =null;
            if (audience != null && audience.contains(THIS_MICROSERVICE)) {
                role = claims.get("role", String.class);
                System.out.println(role);
                UserDetails userDetails = null;

                if(role.equals("MICROSERVICE-COMMUNICATION")) {
                    userDetails = User.builder()
                            .username(username)
                            .password("")
                            .roles(role)
                            .build();
                }else {
                    userDetails = this.customerUserDetailsService.loadUserByUsername(username);
                }

                System.out.println(username);


                // faccio la validazione per vedere se questo token è valido
                if (jwtUtilities.validateToken(jwt, userDetails)) {

                    System.out.println(userDetails.getAuthorities());
                    // e poi mi faccio tutta la parte di autenticazione
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }

            }
        chain.doFilter(request, response);

    }

}