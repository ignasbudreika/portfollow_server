package com.github.ignasbudreika.portfollow.api;

import com.github.ignasbudreika.portfollow.api.dto.response.PortfolioInfoDTO;
import com.github.ignasbudreika.portfollow.api.dto.response.UserInfoDTO;
import com.github.ignasbudreika.portfollow.helper.UserDetailsHelper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {SettingsControllerIT.Initializer.class})
class SettingsControllerIT {
    private static final String USER_ID = "b4711b3d-5a09-4a47-a013-b13cc75051f4";
    private static final String USER_EMAIL = "email@email.com";
    private static final String USER_USERNAME = "username";
    private static final String PORTFOLIO_TITLE = "Portfolio title";
    private static final String PORTFOLIO_DESCRIPTION = "Portfolio description";
    private static final boolean PORTFOLIO_IS_PUBLIC = true;
    private static final boolean PORTFOLIO_IS_HIDDEN_VALUE = true;
    @Autowired
    private UserDetailsHelper userDetailsHelper;
    @Autowired
    @Qualifier("wrapped")
    private ObjectMapper mapper;
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        userDetailsHelper.setUpUser(USER_ID, USER_EMAIL, USER_USERNAME, PORTFOLIO_TITLE, PORTFOLIO_DESCRIPTION, PORTFOLIO_IS_PUBLIC, PORTFOLIO_IS_HIDDEN_VALUE);
    }

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres")
            .withDatabaseName("postgres")
            .withUsername("integrationUser")
            .withPassword("testPass");

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Test
    void shouldReturnUserSettings() throws Exception {
        MvcResult result = mockMvc.perform(get("/settings")).andExpect(status().isOk()).andReturn();

        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());

        UserInfoDTO userInfo = null;
        if (node.has("user_info")) {
            userInfo = mapper.treeToValue(node.get("user_info"), UserInfoDTO.class);
        }

        PortfolioInfoDTO portfolioInfo = null;
        if (node.has("portfolio_info")) {
            portfolioInfo = mapper.treeToValue(node.get("portfolio_info"), PortfolioInfoDTO.class);
        }

        Assertions.assertNotNull(userInfo);
        Assertions.assertEquals(USER_EMAIL, userInfo.getEmail());
        Assertions.assertEquals(USER_USERNAME, userInfo.getUsername());

        Assertions.assertNotNull(portfolioInfo);
        Assertions.assertEquals(PORTFOLIO_TITLE, portfolioInfo.getTitle());
        Assertions.assertEquals(PORTFOLIO_DESCRIPTION, portfolioInfo.getDescription());
        Assertions.assertEquals(PORTFOLIO_IS_PUBLIC, portfolioInfo.isPublic());
        Assertions.assertEquals(!PORTFOLIO_IS_HIDDEN_VALUE, portfolioInfo.isRevealValue());
    }
}
