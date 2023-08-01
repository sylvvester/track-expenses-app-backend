package pl.byczazagroda.trackexpensesappbackend.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.byczazagroda.trackexpensesappbackend.BaseIntegrationTestIT;
import pl.byczazagroda.trackexpensesappbackend.dto.FinancialTransactionCategoryCreateDTO;
import pl.byczazagroda.trackexpensesappbackend.exception.ErrorCode;
import pl.byczazagroda.trackexpensesappbackend.model.FinancialTransactionType;
import pl.byczazagroda.trackexpensesappbackend.model.User;
import pl.byczazagroda.trackexpensesappbackend.model.UserStatus;
import pl.byczazagroda.trackexpensesappbackend.repository.FinancialTransactionCategoryRepository;
import pl.byczazagroda.trackexpensesappbackend.repository.FinancialTransactionRepository;
import pl.byczazagroda.trackexpensesappbackend.repository.UserRepository;
import pl.byczazagroda.trackexpensesappbackend.repository.WalletRepository;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.byczazagroda.trackexpensesappbackend.exception.ErrorCode.TEA003;

class CreateFinancialTransactionCategoryIT extends BaseIntegrationTestIT {

    @Autowired
    private FinancialTransactionRepository financialTransactionRepository;

    @Autowired
    private FinancialTransactionCategoryRepository financialTransactionCategoryRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void clearDatabase() {
        financialTransactionRepository.deleteAll();
        financialTransactionCategoryRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }


    @DisplayName("Should successfully create financial transaction category")
    @Test
    void testCreateFinancialTransactionCategory_whenValidDataProvided_thenShouldCreateCategorySuccessfully(
    ) throws Exception {
        User testUser = createTestUser();
        FinancialTransactionCategoryCreateDTO financialTransactionCategoryCreateDTO
                = new FinancialTransactionCategoryCreateDTO("Category",
                        FinancialTransactionType.INCOME,
                        testUser.getId());

        mockMvc.perform(post("/api/categories")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(financialTransactionCategoryCreateDTO))
                .with(user(String.valueOf(testUser.getId())))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Category"))
                .andExpect(jsonPath("$.type").value("INCOME"));

        assertEquals(1, financialTransactionCategoryRepository.count());
    }

    @DisplayName("Should return error when name length is greater than 30")
    @Test
    void testCreateFinancialTransactionCategory_whenNameExceeds30Characters_thenShouldReturnValidationError()
            throws Exception {
        var financialTransactionCategoryCreateDTO
                = new FinancialTransactionCategoryCreateDTO("ThisIsVeryLongNameForCategoryMoreThan30Characters",
                        FinancialTransactionType.INCOME,
                        1L);

        var result = mockMvc.perform(post("/api/categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(financialTransactionCategoryCreateDTO))
                        .with(user("1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TEA003.getBusinessStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TEA003.getBusinessMessage()))
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.TEA003.getBusinessStatusCode()));


        assertEquals(0, financialTransactionCategoryRepository.count());
        assertTrue(result.andReturn().getResolvedException() instanceof ConstraintViolationException);
        assertEquals(TEA003.getBusinessStatusCode(), result.andReturn().getResponse().getStatus());
    }

    @DisplayName("Should return error when name is empty")
    @Test
    void testCreateFinancialTransactionCategory_whenNameIsEmpty_thenShouldReturnValidationError() throws Exception {
        var financialTransactionCategoryCreateDTO = new FinancialTransactionCategoryCreateDTO("",
                        FinancialTransactionType.INCOME,
                        1L);

        var result = mockMvc.perform(post("/api/categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(financialTransactionCategoryCreateDTO))
                        .with(user("1"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TEA003.getBusinessStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TEA003.getBusinessMessage()))
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.TEA003.getBusinessStatusCode()));

        assertEquals(0, financialTransactionCategoryRepository.count());
        assertTrue(result.andReturn().getResolvedException() instanceof ConstraintViolationException);
        assertEquals(TEA003.getBusinessStatusCode(), result.andReturn().getResponse().getStatus());
    }

    @DisplayName("Should return error when name contains invalid characters")
    @Test
    void testCreateFinancialTransactionCategory_whenNameContainsInvalidCharacters_thenShouldReturnError(
    ) throws Exception {
        User testUser = createTestUser();
        var financialTransactionCategoryCreateDTO = new FinancialTransactionCategoryCreateDTO(
                "Catego*&*^ry@",
                        FinancialTransactionType.INCOME,
                        1L);

        var result = mockMvc.perform(post("/api/categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(financialTransactionCategoryCreateDTO))
                        .with(user(String.valueOf(testUser.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TEA003.getBusinessStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TEA003.getBusinessMessage()))
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.TEA003.getBusinessStatusCode()));


        assertEquals(0, financialTransactionCategoryRepository.count());
        assertTrue(result.andReturn().getResolvedException() instanceof ConstraintViolationException);
        assertEquals(TEA003.getBusinessStatusCode(), result.andReturn().getResponse().getStatus());
    }

    @DisplayName("Should return error when type is empty")
    @Test
    void testCreateFinancialTransactionCategory_whenTypeIsEmpty_thenShouldReturnValidationError() throws Exception {
        User testUser = createTestUser();
        var financialTransactionCategoryCreateDTO = new FinancialTransactionCategoryCreateDTO(
                "Category",
                        null,
                        1L);

        var result = mockMvc.perform(post("/api/categories")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(financialTransactionCategoryCreateDTO))
                        .with(user(String.valueOf(testUser.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(ErrorCode.TEA003.getBusinessStatus()))
                .andExpect(jsonPath("$.message").value(ErrorCode.TEA003.getBusinessMessage()))
                .andExpect(jsonPath("$.statusCode").value(ErrorCode.TEA003.getBusinessStatusCode()));


        assertEquals(0, financialTransactionCategoryRepository.count());
        assertTrue(result.andReturn().getResolvedException() instanceof ConstraintViolationException);
        assertEquals(TEA003.getBusinessStatusCode(), result.andReturn().getResponse().getStatus());
    }

    private User createTestUser() {
        final User userOne = User.builder()
                .id(1L)
                .userName("userone")
                .email("Email@wp.pl")
                .password("Password1@")
                .userStatus(UserStatus.VERIFIED)
                .build();
        return userRepository.save(userOne);
    }

}
