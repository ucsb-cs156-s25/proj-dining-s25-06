package edu.ucsb.cs156.dining.interceptors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import edu.ucsb.cs156.dining.ControllerTestCase;
import edu.ucsb.cs156.dining.entities.User;
import edu.ucsb.cs156.dining.repositories.UserRepository;
import edu.ucsb.cs156.dining.statuses.ModerationStatus;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleInterceptorTests extends ControllerTestCase {

    @MockBean
    UserRepository userRepository;

    @Autowired
    private RequestMappingHandlerMapping mapping;

    @BeforeEach
    public void mockLogin() {
        HashMap<String, Object> values = new HashMap<>();

        values.put("email", "cgaucho@ucsb.edu");
        values.put("googleSub", "googleSub");
        values.put("pictureUrl", "pictureUrl");
        values.put("fullName", "Joe Gaucho");
        values.put("givenName", "Joe");
        values.put("familyName", "Gaucho");
        values.put("emailVerified", true);
        values.put("locale", "en");
        values.put("hostedDomain", "ucsb.edu");
        values.put("alias", "joeg");
        values.put("proposedAlias", "joeprop");
        values.put("status", "APPROVED");
        values.put("dateApproved", "2025-05-18");

        Set<GrantedAuthority> credentials = new HashSet<>();
        credentials.add(new SimpleGrantedAuthority("ROLE_USER"));
        credentials.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        credentials.add(new SimpleGrantedAuthority("ROLE_MODERATOR"));

        OAuth2User user = new DefaultOAuth2User(credentials, values, "email");
        Authentication auth = new OAuth2AuthenticationToken(user, credentials, "google");
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void RoleInterceptorIsPresent() throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/currentUser");
        HandlerExecutionChain chain = mapping.getHandler(request);

        assert chain != null;
        Optional<HandlerInterceptor> RoleInterceptor = chain.getInterceptorList()
                .stream()
                .filter(RoleInterceptor.class::isInstance)
                .findFirst();

        assertTrue(RoleInterceptor.isPresent());
    }

    @Test
    public void updates_admin_role_when_user_admin_false() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("cgaucho@ucsb.edu")
                .admin(false)
                .moderator(true)
                .build();
        when(userRepository.findByEmail("cgaucho@ucsb.edu")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/currentUser");
        HandlerExecutionChain chain = mapping.getHandler(request);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assert chain != null;
        Optional<HandlerInterceptor> RoleInterceptor = chain.getInterceptorList()
                .stream()
                .filter(RoleInterceptor.class::isInstance)
                .findFirst();

        assertTrue(RoleInterceptor.isPresent());

        RoleInterceptor.get().preHandle(request, response, chain.getHandler());

        verify(userRepository, times(1)).findByEmail("cgaucho@ucsb.edu");

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities();

        boolean role_admin = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_ADMIN"));
        boolean role_moderator = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_MODERATOR"));
        boolean role_user = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_USER"));
        assertFalse(role_admin, "ROLE_ADMIN should not be in roles list");
        assertTrue(role_moderator, "ROLE_MODERATOR should be in roles list");
        assertTrue(role_user, "ROLE_USER should be in roles list");
    }

    @Test
    public void updates_moderator_role_when_user_moderator_false() throws Exception {
        User user = User.builder()
                .id(1L)
                .email("cgaucho@ucsb.edu")
                .admin(true)
                .moderator(false)
                .build();
        when(userRepository.findByEmail("cgaucho@ucsb.edu")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/currentUser");
        HandlerExecutionChain chain = mapping.getHandler(request);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assert chain != null;
        Optional<HandlerInterceptor> RoleInterceptor = chain.getInterceptorList()
                .stream()
                .filter(RoleInterceptor.class::isInstance)
                .findFirst();

        assertTrue(RoleInterceptor.isPresent());

        RoleInterceptor.get().preHandle(request, response, chain.getHandler());

        verify(userRepository, times(1)).findByEmail("cgaucho@ucsb.edu");

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities();

        boolean role_admin = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_ADMIN"));
        boolean role_moderator = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_MODERATOR"));
        boolean role_user = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_USER"));
        assertTrue(role_admin, "ROLE_ADMIN should be in roles list");
        assertFalse(role_moderator, "ROLE_MODERATOR should not be in roles list");
        assertTrue(role_user, "ROLE_USER should be in roles list");
    }

    @Test
    public void updates_nothing_when_user_not_present() throws Exception {
        User user = User.builder()
                .email("cgaucho2@ucsb.edu")
                .id(15L)
                .admin(false)
                .moderator(false)
                .build();
        when(userRepository.findByEmail("cgaucho2@ucsb.edu")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/currentUser");
        HandlerExecutionChain chain = mapping.getHandler(request);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assert chain != null;
        Optional<HandlerInterceptor> RoleInterceptor = chain.getInterceptorList()
                .stream()
                .filter(RoleInterceptor.class::isInstance)
                .findFirst();

        assertTrue(RoleInterceptor.isPresent());

        RoleInterceptor.get().preHandle(request, response, chain.getHandler());

        verify(userRepository, times(1)).findByEmail("cgaucho@ucsb.edu");

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities();

        boolean role_admin = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_ADMIN"));
        boolean role_moderator = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_MODERATOR"));
        boolean role_user = authorities.stream()
                .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_USER"));
        assertTrue(role_admin, "ROLE_ADMIN should be in roles list");
        assertTrue(role_moderator, "ROLE_MODERATOR should be in roles list");
        assertTrue(role_user, "ROLE_USER should be in roles list");
    }
}