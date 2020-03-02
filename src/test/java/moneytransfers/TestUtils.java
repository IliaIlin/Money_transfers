package moneytransfers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import moneytransfers.controller.MoneyTransferController;
import moneytransfers.dto.TransferDto;
import spark.Spark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtils {
    private static ObjectMapper objectMapper;
    private static final String BASE_URL = "http://localhost:4567";
    public static final String GET = "GET";
    public static final String POST = "POST";

    static void bootstrapMoneyTransferApplication() {
        Spark.awaitStop();
        Injector injector = Guice.createInjector(new MoneyTransferModule());
        objectMapper = injector.getInstance(ObjectMapper.class);
        injector.getInstance(MoneyTransferController.class)
                .run();
    }

    static void stopMoneyTransferApplication() {
        Spark.stop();
    }

    static void assertResponse(int expectedResponseCode, String expectedResponseOutput, Response response) {
        assertEquals(expectedResponseCode, response.getResponseCode());
        assertEquals(expectedResponseOutput, response.getResponseOutput());
    }

    static void assertGetTransfersResponse(int expectedResponseCode, List<TransferDto> expectedTransfers, Response response) throws JsonProcessingException {
        assertEquals(expectedResponseCode, response.getResponseCode());

        List<TransferDto> transfers = objectMapper.readValue(response.getResponseOutput(), new TypeReference<>() {
        });

        assertEquals(expectedTransfers.size(), transfers.size());

        for (TransferDto transfer : transfers) {
            assertTrue(expectedTransfers.contains(transfer));
        }
    }

    static void sendRequestSuppressingException(String url, String httpMethod, TransferDto requestBody) {
        try {
            sendRequest(url, httpMethod, requestBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Response sendRequest(String url, String httpMethod) throws IOException {
        HttpURLConnection connection = createConnection(url, httpMethod);
        return sendRequest(connection);
    }

    static Response sendRequest(String url, String httpMethod, TransferDto requestBody) throws IOException {
        HttpURLConnection connection = createConnection(url, httpMethod, requestBody);
        return sendRequest(connection);
    }

    private static Response sendRequest(HttpURLConnection connection) throws IOException {
        String responseOutput = getResponseOutput(connection);
        Response response = new Response(responseOutput, connection.getResponseCode());
        connection.disconnect();
        return response;
    }

    private static HttpURLConnection createConnection(String url, String httpMethod) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + url).openConnection();
        connection.setRequestMethod(httpMethod);
        return connection;
    }

    private static HttpURLConnection createConnection(String url, String httpMethod, TransferDto requestBody) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + url).openConnection();
        connection.setRequestMethod(httpMethod);
        if (requestBody != null) {
            String requestBodyString = objectMapper.writeValueAsString(requestBody);
            connection.setDoOutput(true);
            connection.getOutputStream().write(requestBodyString.getBytes());
        }
        return connection;
    }

    private static String getResponseOutput(HttpURLConnection connection) throws IOException {
        StringBuffer responseOutput;
        InputStream inputStream = getInputStream(connection);
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream))) {
            String inputLine;
            responseOutput = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                responseOutput.append(inputLine);
            }
        }
        return responseOutput.toString();
    }

    private static InputStream getInputStream(HttpURLConnection connection) throws IOException {
        InputStream inputStream;
        if (connection.getResponseCode() > 299) {
            inputStream = connection.getErrorStream();
        } else {
            inputStream = connection.getInputStream();
        }
        return inputStream;
    }
}

class Response {
    private final String responseOutput;
    private final int responseCode;

    Response(String responseOutput, int responseCode) {
        this.responseOutput = responseOutput;
        this.responseCode = responseCode;
    }

    public String getResponseOutput() {
        return responseOutput;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
