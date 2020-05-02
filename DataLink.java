// json_simple V3.1.1

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Vector;


public class DataLink {
    public Integer connect_timeout = 5; // seconds
    public Integer response_timeout = 5; // seconds

    private JsonArray get(String url) {
        JsonArray j = new JsonArray();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(connect_timeout))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(response_timeout))
                    .GET()
                    .build();
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            // check that response is JsonArray compatible
            // DEEP endpoint will return a single json object which is not JsonArray compatible
            if(response.charAt(0) != '['){
                // add brackets for JsonArray compatibility
                response = "[" + response + "]";
            }

            j = (JsonArray) Jsoner.deserialize(response);
        } catch (IOException | InterruptedException | JsonException e) {
            e.printStackTrace();
        }
        return j;
    }


    private void get_download(String url, String file_name) {
        try {
            InputStream input = new URL(url).openStream();
            System.out.println("Downloading: " + file_name);
            Files.copy(input, Paths.get(file_name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String build_url(Vector<String> items, String base_url) {
        // generate final url by appending items to base API endpoint url

        StringBuilder url = new StringBuilder(base_url);
        for (String item : items) {
            url.append(item);
            url.append(",");
        }
        return url.toString();
    }

    public JsonArray tops(Vector<String> tickers) {
        // Aggregated best bid and offer
        JsonArray j;
        if (tickers.isEmpty()) {
            // get all available tickers
            j = get("https://api.iextrading.com/1.0/tops");
        } else {
            // get specified tickers
            j = get(build_url(tickers, "https://api.iextrading.com/1.0/tops?symbols="));
        }
        return j;
    }

    public JsonArray last(Vector<String> tickers) {
        // Data on last execution for given ticker(s)
        JsonArray j;
        if (tickers.isEmpty()) {
            // get all available tickers
            j = get("https://api.iextrading.com/1.0/tops/last");
        } else {
            // get specified tickers
            j = get(build_url(tickers, "https://api.iextrading.com/1.0/tops/last?symbols="));
        }
        return j;
    }

    public JsonArray hist(String date, Boolean download) {
        // IEX historical data
        JsonArray j;
        if (date.isEmpty()) {
            // get all available dates
            j = get("https://api.iextrading.com/1.0/tops/hist");
        } else {
            // get specified date
            j = get("https://api.iextrading.com/1.0/hist?date=" + date);
        }

        if (download) {
            for (Object obj_data : j) {
                JsonObject data = (JsonObject) obj_data;  // convert type Object to type JsonObject
                if (data.get("feed").equals("DEEP")){
                    get_download(data.get("link").toString(), "IEX_DEEP_" + data.get("date").toString() + ".gz");
                }
                else if(data.get("feed").equals("TOPS")){
                    get_download(data.get("link").toString(), "IEX_TOPS_" + data.get("date").toString() + ".gz");
                }
            }
        }
        return j;
    }

    public JsonArray deep_get(String ticker) {
        // Get book-depth quote for specified ticker. IEX only allows one ticker per request for this API endpoint
        return get("https://api.iextrading.com/1.0/deep?symbols=" + ticker);
    }

    public static class IEX_Book_Exception extends Exception {
        public IEX_Book_Exception(String error_message) {
            super(error_message);
        }
    }

    public JsonArray book(Vector<String> tickers) throws IEX_Book_Exception {
        // Bid and ask data for given ticker(s), Max 10 tickers
        // check ticker qty
        if (tickers.size() > 10){
            throw new IEX_Book_Exception("Request exceeded maximum number of tickers. Limit is 10!");
        }

        return get(build_url(tickers, "https://api.iextrading.com/1.0/deep/book?symbols="));
    }
}
