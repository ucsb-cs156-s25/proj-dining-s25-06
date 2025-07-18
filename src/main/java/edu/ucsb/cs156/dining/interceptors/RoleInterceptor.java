package edu.ucsb.cs156.dining.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import edu.ucsb.cs156.dining.repositories.UserRepository;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;
import java.util.stream.Collectors;
import edu.ucsb.cs156.dining.entities.User;

@Slf4j
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Autowired
    UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getClass() == OAuth2AuthenticationToken.class) {
            OAuth2User principal = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = principal.getAttribute("email");
            Optional<User> optionalUser = userRepository.findByEmail(email);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                Set<GrantedAuthority> revisedAuthorities = authorities.stream().filter(
                        grantedAuth -> !grantedAuth.getAuthority().equals("ROLE_ADMIN")
                                    && !grantedAuth.getAuthority().equals("ROLE_MODERATOR"))
                        .collect(Collectors.toSet());
                if (user.getAdmin()) {
                    revisedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
                if (user.getModerator()) {
                    revisedAuthorities.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));
                }
                Authentication newAuth = new OAuth2AuthenticationToken(principal, revisedAuthorities,
                        (((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()));
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }
        }
        return true;
    }
}