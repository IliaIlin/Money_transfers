package moneytransfers;

import com.google.inject.Guice;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import moneytransfers.controller.MoneyTransferController;
import moneytransfers.dto.TransferDto;
import spark.Spark;

import java.util.List;

import static io.restassured.RestAssured.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {

    static void bootstrapMoneyTransferApplication() {
        Spark.awaitStop();
        Guice.createInjector(new MoneyTransferModule())
                .getInstance(MoneyTransferController.class)
                .run();
        Spark.awaitInitialization();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Spark.port();
        RestAssured.defaultParser = Parser.JSON;
    }

    static void stopMoneyTransferApplication() {
        Spark.stop();
    }

    static void assertGetTransfersResponse(int expectedResponseCode, List<TransferDto> expectedResponseBody, String path) {
        Response response = get(path);
        response.then().statusCode(expectedResponseCode);
        List<TransferDto> deserializedResponseBody = response.as(new TypeRef<>() {
        });

        assertEquals(expectedResponseBody.size(), deserializedResponseBody.size());
        assertTrue(expectedResponseBody.containsAll(deserializedResponseBody));
    }
}
