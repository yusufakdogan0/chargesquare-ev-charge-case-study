package com.yusufakdogan.session_service.controller;

import com.yusufakdogan.session_service.TestDatabaseConfig;
import com.yusufakdogan.session_service.facade.SessionFacade;
import com.yusufakdogan.session_service.security.CustomUserDetailsService;
import com.yusufakdogan.session_service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfig.class)
@Transactional
class SessionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionFacade sessionFacade;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldReturnForbiddenForViewerWithValidAuthentication() throws Exception {
        mockMvc.perform(post("/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"connectorId\":1}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(sessionFacade);
    }

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"connectorId\":1}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sessionFacade);
    }

    @Test
    void shouldReturnUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(post("/sessions/start")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"connectorId\":1}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(sessionFacade);
    }
}
