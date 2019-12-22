package money_transfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Inject;
import money_transfers.dto.TransferDto;
import money_transfers.exception.InvalidRequestBodyException;
import money_transfers.exception.TransferExecutionException;
import money_transfers.service.AccountService;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static spark.Spark.*;

public class MoneyTransferApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MoneyTransferApplication.class);

    private final AccountService accountService;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;

    public static final String INTERNAL_SERVER_ERROR_MESSAGE =
            "Internal Server Error. Please check correctness of the request and try once more.";
    public static final String SUCCESSFUL_TRANSFER_MESSAGE = "Transfer was completed successfully";
    public static final String INVALID_REQUEST_BODY_MESSAGE =
            "Please check correctness of the request body and try again.";
    public static final String TRANSFER_EXECUTION_ERROR_MESSAGE =
            "Requested money transfer was rejected. Please check whether sender account has sufficient funds and correctness of the request body.";

    @Inject
    public MoneyTransferApplication(AccountService accountService, ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.objectMapper = objectMapper;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public static void main(String[] args) {
        Guice.createInjector(new MoneyTransferModule())
                .getInstance(MoneyTransferApplication.class)
                .run();
    }

    void run() {
        post("/transfer", (request, response) -> {
            LOG.info("Received request for transfer: " + request.body());
            writeLock.lock();
            try {
                TransferDto transferDto = getTransferDto(request);
                accountService.executeMoneyTransfer(transferDto);
                response.status(HttpStatus.OK_200);
                return SUCCESSFUL_TRANSFER_MESSAGE;
            } finally {
                LOG.debug("Finish money transfer");
                writeLock.unlock();
            }
        });

        get("/accounts/:id/transfers", (request, response) -> {
            LOG.info("Received get transfers request for id = " + request.params(":id"));
            readLock.lock();
            try {
                List<TransferDto> transfersByAccountId = accountService.getTransfersByAccountId(Long.valueOf(request.params(":id")));
                return objectMapper.writeValueAsString(transfersByAccountId);
            } finally {
                readLock.unlock();
            }
        });

        get("/accounts/:id/balance", (request, response) -> {
            LOG.info("Received get balance request for id = " + request.params(":id"));
            readLock.lock();
            try {
                return accountService.getBalanceByAccountId(Long.valueOf(request.params(":id")));
            } finally {
                readLock.unlock();
            }
        });

        exception(InvalidRequestBodyException.class, (exception, request, response) -> {
            response.status(HttpStatus.BAD_REQUEST_400);
            response.body(INVALID_REQUEST_BODY_MESSAGE);
        });

        exception(TransferExecutionException.class, (exception, request, response) -> {
            response.status(HttpStatus.BAD_REQUEST_400);
            response.body(TRANSFER_EXECUTION_ERROR_MESSAGE);
        });

        exception(Exception.class, (exception, request, response) -> {
            response.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            response.body(INTERNAL_SERVER_ERROR_MESSAGE);
        });
    }

    private TransferDto getTransferDto(Request request) {
        TransferDto transferDto = parseTransferDto(request);
        if (transferDto == null || !transferDto.isValid()) {
            throw new InvalidRequestBodyException();
        }
        return transferDto;
    }

    private TransferDto parseTransferDto(Request request) {
        try {
            return objectMapper.readValue(request.body(), TransferDto.class);
        } catch (Exception exception) {
            throw new InvalidRequestBodyException();
        }
    }
}