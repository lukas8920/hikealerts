package org.hikingdev.microsoft_hackathon.security;

import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.security.failures.service.LoginAttemptService;
import org.hikingdev.microsoft_hackathon.security.failures.service.RegisterAttemptService;
import org.hikingdev.microsoft_hackathon.user.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class JwtTokenFilterTest {
    private JwtTokenFilter jwtTokenFilter;

    @BeforeEach
    public void setup(){
        RegisterAttemptService registerAttemptService = mock(RegisterAttemptService.class);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        Publisher publisher = mock(Publisher.class);
        LoginAttemptService loginAttemptService = mock(LoginAttemptService.class);
        RoleRegister roleRegister = new RoleRegister();
        this.jwtTokenFilter = new JwtTokenFilter(registerAttemptService, jwtTokenProvider, publisher, loginAttemptService, roleRegister);
    }

    @Test
    public void testEmptyEndpoints(){
        List<Role> roles = new ArrayList<>();

        List<String> allowedEndpoints = this.jwtTokenFilter.getAllowedEndpoints(roles);

        assertThat(allowedEndpoints.isEmpty(), is(true));
    }

    @Test
    public void testMultipleRoles(){
        List<Role> roles = List.of(Role.API_USER, Role.GEOTREK);

        List<String> allowedEndpoints = this.jwtTokenFilter.getAllowedEndpoints(roles);

        RoleRegister roleRegister = new RoleRegister();
        List<String> resultEndpoints = new ArrayList<>();
        resultEndpoints.addAll(roleRegister.getAPI_USER_ENDPOINTS());
        resultEndpoints.addAll(roleRegister.getGEOTREK_ENDPOINTS());

        assertThat(allowedEndpoints.size(), is(3));
        assertThat(allowedEndpoints.containsAll(resultEndpoints), is(true));
    }
}
