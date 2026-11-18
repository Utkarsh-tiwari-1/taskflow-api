package com.taskflow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:taskflowtest;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.jwt.secret=integration-test-secret-at-least-32-characters-long",
        "ADMIN_EMAIL=admin@example.com",
        "ADMIN_PASSWORD=integration-admin-password-123"
})
@AutoConfigureMockMvc
class TaskFlowApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void registrationAssignmentFilteringCommentsAndAuthorizationWorkTogether() throws Exception {
        TestUser alice = register("alice@example.com", "alice-password-123");
        TestUser bob = register("bob@example.com", "bob-password-123");
        TestUser stranger = register("stranger@example.com", "stranger-password-123");
        long bobId = bob.id();

        MvcResult created = mvc.perform(post("/api/tasks")
                        .header("Authorization", bearer(alice.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Release checklist","description":"Prepare the release","priority":"HIGH","assigneeId":%d}
                                """.formatted(bobId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.assigneeId").value(bobId))
                .andReturn();
        long taskId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/tasks").queryParam("status", "OPEN")
                        .header("Authorization", bearer(alice.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Release checklist"));

        mvc.perform(patch("/api/tasks/{taskId}/status", taskId)
                        .header("Authorization", bearer(bob.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mvc.perform(post("/api/tasks/{taskId}/comments", taskId)
                        .header("Authorization", bearer(bob.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Started the checklist\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Started the checklist"));

        mvc.perform(get("/api/tasks/{taskId}/comments", taskId)
                        .header("Authorization", bearer(alice.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].authorId").value(bobId));

        mvc.perform(get("/api/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(stranger.token())))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/users").header("Authorization", bearer(alice.token())))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());

        String adminToken = login("admin@example.com", "integration-admin-password-123");
        mvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").exists());
        mvc.perform(get("/api/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    private TestUser register(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterBody(email, password, email))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return new TestUser(response.get("user").get("id").asLong(), response.get("accessToken").asText());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginBody(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private static String bearer(String token) { return "Bearer " + token; }

    private record TestUser(long id, String token) { }
    private record RegisterBody(String email, String password, String displayName) { }
    private record LoginBody(String email, String password) { }
}
