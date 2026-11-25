package carRental.CarRrental;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SuperAdminSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedAccessToSuperAdminIsRejected() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRoleCannotAccessSuperAdminEndpoints() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCannotAccessSuperAdminEndpoints() throws Exception {
        mockMvc.perform(get("/super-admin/settings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRoleCanViewReadOnlyPricingRules() throws Exception {
        mockMvc.perform(get("/api/pricing/rules"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanAccessDashboardAndSettings() throws Exception {
        mockMvc.perform(get("/super-admin/dashboard/stats"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/super-admin/settings"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/super-admin/hosts"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/super-admin/fleet-managers"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/super-admin/support-agents"))
                .andExpect(status().isOk());
    }
}
