package moneytransfers;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import moneytransfers.dto.TransferDto;
import spark.Spark;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {

    public static void bootstrapMoneyTransferApplication(Runnable runnable) {
        Spark.awaitStop();
        runnable.run();
        Spark.awaitInitialization();
        setupRestAssured();
    }

    private static void setupRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Spark.port();
    }

    public static void stopMoneyTransferApplication() {
        Spark.stop();
    }

    public static void assertEqualsTransferList(List<TransferDto> first, List<TransferDto> second){
        assertEquals(first.size(), second.size());
        assertTrue(first.containsAll(second));
    }
}
