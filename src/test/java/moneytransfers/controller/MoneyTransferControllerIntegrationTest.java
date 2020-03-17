package moneytransfers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import moneytransfers.dto.TransferDto;
import moneytransfers.exception.NoSuchAccountException;
import moneytransfers.exception.TransferExecutionException;
import moneytransfers.service.AccountService;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.*;
import static moneytransfers.TestUtils.*;
import static moneytransfers.controller.MoneyTransferController.*;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoneyTransferControllerIntegrationTest {

    @Mock
    private AccountService accountService;

    private static final Long FIRST_ACCOUNT_ID = 1L;
    private static final Long SECOND_ACCOUNT_ID = 2L;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(50);
    private static final BigDecimal NEGATIVE_AMOUNT = BigDecimal.valueOf(-1L);
    private static final String TRANSFER_PATH = "/transfer";

    @BeforeEach
    void beforeEach() {
        bootstrapMoneyTransferApplication(
                () -> new MoneyTransferController(accountService, new ObjectMapper()).run());
    }

    @Test
    void getBalance_happyFlow() {
        when(accountService.getBalanceByAccountId(FIRST_ACCOUNT_ID)).thenReturn(AMOUNT);

        get("/accounts/1/balance")
                .then()
                .statusCode(HttpStatus.OK_200)
                .body(equalTo(AMOUNT.toString()));
    }

    @Test
    void getBalance_notExistingAccountId_shouldReturnCorrectErrorMessage() {
        when(accountService.getBalanceByAccountId(FIRST_ACCOUNT_ID)).thenThrow(NoSuchAccountException.class);

        get("/accounts/1/balance")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(equalTo(NO_ACCOUNT_FOUND_ERROR_MESSAGE));
    }

    @Test
    void getBalance_notNumericAccountId_shouldReturnCorrectErrorMessage() {
        get("/accounts/str/balance")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @Test
    void getTransfers_happyFlow() {
        List<TransferDto> transfers = List.of(
                new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AMOUNT)
        );

        when(accountService.getTransfersByAccountId(FIRST_ACCOUNT_ID)).thenReturn(transfers);

        Response response = get("/accounts/1/transfers");
        response.then().statusCode(HttpStatus.OK_200);
        List<TransferDto> deserializedResponseBody = response.as(new TypeRef<>() {
        });

        assertEqualsTransferList(transfers, deserializedResponseBody);
    }

    @Test
    void getTransfers_notNumericAccountId_shouldReturnCorrectErrorMessage() {
        get("/accounts/str/transfers")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @Test
    void transfer_happyFlow() {
        TransferDto transferDto = new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AMOUNT);

        doNothing().when(accountService).executeMoneyTransfer(transferDto);

        with()
                .body(transferDto)
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.OK_200);
    }

    @Test
    void transfer_notExistingAccountId_shouldReturnCorrectErrorMessage() {
        TransferDto transferDto = new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AMOUNT);

        doThrow(NoSuchAccountException.class).when(accountService).executeMoneyTransfer(transferDto);

        with()
                .body(transferDto)
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(equalTo(NO_ACCOUNT_FOUND_ERROR_MESSAGE));
    }

    @Test
    void transfer_notSufficientFunds_shouldReturnCorrectErrorMessage() {
        TransferDto transferDto = new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, AMOUNT);

        doThrow(TransferExecutionException.class).when(accountService).executeMoneyTransfer(transferDto);

        with()
                .body(transferDto)
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500)
                .body(equalTo(TRANSFER_EXECUTION_ERROR_MESSAGE));
    }

    @Test
    void transfer_unspecifiedAmount_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, null))
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @Test
    void transfer_unspecifiedToAccountId_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(FIRST_ACCOUNT_ID, null, AMOUNT))
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @Test
    void transfer_unspecifiedFromAccountId_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(null, SECOND_ACCOUNT_ID, AMOUNT))
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @Test
    void transfer_invalidRequestBody_shouldReturnCorrectErrorMessage() {
        post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @Test
    void transfer_negativeAmountOfMoney_shouldReturnCorrectErrorMessage() {
        with()
                .body(new TransferDto(FIRST_ACCOUNT_ID, SECOND_ACCOUNT_ID, NEGATIVE_AMOUNT))
                .post(TRANSFER_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST_400)
                .body(equalTo(INVALID_REQUEST_MESSAGE));
    }

    @AfterEach
    void afterEach() {
        stopMoneyTransferApplication();
    }
}
