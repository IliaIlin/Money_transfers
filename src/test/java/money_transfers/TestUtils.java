package money_transfers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Module;
import money_transfers.dto.TransferDto;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BASE_URL = "http://localhost:4567";
    public static final String GET = "GET";
    public static final String POST = "POST";

    static void bootstrapMoneyTransferApplication(Module... modules) {
        Spark.awaitStop();
        Guice.createInjector(modules)
                .getInstance(MoneyTransferApplication.class)
                .run();
    }

    static void stopMoneyTransferApplication() {
        Spark.stop();
    }

    static String getResponseSuppressingException(String url, String httpMethod, TransferDto requestBody) {
        try {
            return getResponse(url, httpMethod, requestBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String getResponse(String url, String httpMethod, TransferDto requestBody) throws IOException {
        HttpURLConnection connection = createConnection(url, httpMethod);

        String requestBodyString = objectMapper.writeValueAsString(requestBody);
        connection.setDoOutput(true);
        connection.getOutputStream().write(requestBodyString.getBytes());
        return getResponseByConnection(connection);
    }

    static String getResponse(String url, String httpMethod) throws IOException {
        HttpURLConnection connection = createConnection(url, httpMethod);
        return getResponseByConnection(connection);
    }

    private static HttpURLConnection createConnection(String url, String httpMethod) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL + url).openConnection();
        connection.setRequestMethod(httpMethod);
        return connection;
    }

    private static String getResponseByConnection(HttpURLConnection connection) throws IOException {
        StringBuffer response;
        InputStream inputStream;
        if (connection.getResponseCode() > 299) {
            inputStream = connection.getErrorStream();
        } else {
            inputStream = connection.getInputStream();
        }
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(inputStream))) {
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        connection.disconnect();
        return response.toString();
    }

    static void assertTransfersResponse(List<TransferDto> expectedTransfers, String response) throws JsonProcessingException {
        List<TransferDto> transfers = objectMapper.readValue(
                response, new TypeReference<>() {
                });

        assertEquals(expectedTransfers.size(), transfers.size());

        for (TransferDto transfer : transfers) {
            assertTrue(expectedTransfers.contains(transfer));
        }
    }
}
