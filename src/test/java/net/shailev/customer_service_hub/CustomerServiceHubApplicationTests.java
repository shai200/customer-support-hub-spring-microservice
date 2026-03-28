package net.shailev.customer_service_hub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerServiceHubApplicationTests {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "admin11";
	private static final String DEVELOPER_USERNAME = "developer";
	private static final String DEVELOPER_PASSWORD = "developer11";
	private static final String AGENT_USERNAME = "agent";
	private static final String AGENT_PASSWORD = "agent11";
	private static final String CUSTOMER_USERNAME = "customer";
	private static final String CUSTOMER_PASSWORD = "java11";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void oauthTokenIssuedForValidCredentials() throws Exception {
		mockMvc.perform(post("/oauth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "customer",
								  "password": "java11"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.access_token").isString())
				.andExpect(jsonPath("$.token_type").value("Bearer"));
	}

	@Test
	void oauthTokenRejectedForInvalidCredentials() throws Exception {
		mockMvc.perform(post("/oauth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "username": "customer",
								  "password": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void protectedEndpointRejectsMissingToken() throws Exception {
		mockMvc.perform(get("/profile/me"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void aliveEndpointIsPublic() throws Exception {
		mockMvc.perform(get("/alive"))
				.andExpect(status().isOk())
				.andExpect(content().string("alive"));
	}

	@Test
	void customerCannotQueryAgentCustomers() throws Exception {
		String token = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);

		mockMvc.perform(get("/agent/customers")
						.header("Authorization", bearer(token)))
				.andExpect(status().isForbidden());
	}

	@Test
	void agentCanCreateCustomerUnderSelf() throws Exception {
		String token = oauthToken(AGENT_USERNAME, AGENT_PASSWORD);
		String username = "customer_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		String payload = """
				{
				  "username": "%s",
				  "fullName": "Created By Agent",
				  "email": "created.by.agent@example.com"
				}
				""".formatted(username);

		mockMvc.perform(post("/agent/customers")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(username))
				.andExpect(jsonPath("$.roleType").value("CUSTOMER"))
				.andExpect(jsonPath("$.agentUsername").value(AGENT_USERNAME));
	}

	@Test
	void agentCanQueryOwnCustomers() throws Exception {
		String token = oauthToken(AGENT_USERNAME, AGENT_PASSWORD);
		String username = "owned_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		String payload = """
				{
				  "username": "%s",
				  "fullName": "Owned Customer",
				  "email": "owned.customer@example.com"
				}
				""".formatted(username);

		mockMvc.perform(post("/agent/customers")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk());

		mockMvc.perform(get("/agent/customers")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].username", hasItem(username)));
	}

	@Test
	void developerCanQueryAgentCustomers() throws Exception {
		String token = oauthToken(DEVELOPER_USERNAME, DEVELOPER_PASSWORD);

		mockMvc.perform(get("/agent/customers")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	@Test
	void adminCanQueryAgentCustomers() throws Exception {
		String token = oauthToken(ADMIN_USERNAME, ADMIN_PASSWORD);

		mockMvc.perform(get("/agent/customers")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	@Test
	void customerCanQueryOwnProfile() throws Exception {
		String token = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);

		mockMvc.perform(get("/profile/me")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(CUSTOMER_USERNAME))
				.andExpect(jsonPath("$.roleType").value("CUSTOMER"));
	}

	@Test
	void customerCanUpdateOwnProfile() throws Exception {
		String token = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);
		String payload = """
				{
				  "fullName": "Customer Updated",
				  "email": "customer.updated@example.com"
				}
				""";

		mockMvc.perform(put("/profile/me")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(CUSTOMER_USERNAME))
				.andExpect(jsonPath("$.fullName").value("Customer Updated"))
				.andExpect(jsonPath("$.email").value("customer.updated@example.com"));
	}

	@Test
	void customerCanCreateTicket() throws Exception {
		String token = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);
		String payload = """
				{
				  "title": "Login issue",
				  "description": "Unable to log in after password reset"
				}
				""";

		mockMvc.perform(post("/tickets")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Login issue"))
				.andExpect(jsonPath("$.customerUsername").value(CUSTOMER_USERNAME));
	}

	@Test
	void customerCanGetOwnTickets() throws Exception {
		String token = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);
		String payload = """
				{
				  "title": "Billing question",
				  "description": "Need invoice copy"
				}
				""";

		mockMvc.perform(post("/tickets")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tickets")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].title", hasItem("Billing question")));
	}

	@Test
	void agentCanSearchTicketsFromOwnCustomers() throws Exception {
		String customerToken = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);
		String agentToken = oauthToken(DEVELOPER_USERNAME, DEVELOPER_PASSWORD);
		String payload = """
				{
				  "title": "Payment failed",
				  "description": "Card payment gets declined"
				}
				""";

		mockMvc.perform(post("/tickets")
						.header("Authorization", bearer(customerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk());

		mockMvc.perform(get("/agent/tickets")
						.param("search", "payment")
						.header("Authorization", bearer(agentToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].customerUsername", hasItem(CUSTOMER_USERNAME)));
	}

	@Test
	void customerCannotQueryAgentTickets() throws Exception {
		String token = oauthToken(CUSTOMER_USERNAME, CUSTOMER_PASSWORD);

		mockMvc.perform(get("/agent/tickets")
						.header("Authorization", bearer(token)))
				.andExpect(status().isForbidden());
	}

	@Test
	void agentCannotCreateTicket() throws Exception {
		String token = oauthToken(AGENT_USERNAME, AGENT_PASSWORD);
		String payload = """
				{
				  "title": "Agent attempt",
				  "description": "Agents should not create tickets"
				}
				""";

		mockMvc.perform(post("/tickets")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isForbidden());
	}

	private String oauthToken(String username, String password) throws Exception {
		String payload = """
				{
				  "username": "%s",
				  "password": "%s"
				}
				""".formatted(username, password);

		String responseBody = mockMvc.perform(post("/oauth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		Matcher matcher = Pattern.compile("\"access_token\"\\s*:\\s*\"([^\"]+)\"").matcher(responseBody);
		if (!matcher.find()) {
			throw new IllegalStateException("access_token not found in response: " + responseBody);
		}
		return matcher.group(1);
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}

}
