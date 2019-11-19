package io.zeebe.exporter.latency;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.HdrHistogram.Histogram;

public class LatencyExporterHttpHandler {

  private static final Charset CHARSET = StandardCharsets.UTF_8;

  private final LatencyExporterHistogramRegistry registry;

  public LatencyExporterHttpHandler(LatencyExporterHistogramRegistry registry) {
    this.registry = registry;
  }

  public void getLatency(HttpExchange httpExchange) {}

  public void resetLatency(HttpExchange httpExchange) throws IOException {
    sendResponse(httpExchange, "");
  }

  public void getCount(HttpExchange httpExchange) throws IOException {
    long totalCount = histogram.getTotalCount();
    sendResponse(httpExchange, String.valueOf(totalCount));
  }

  private Histogram getHistogramForQuery(Map<String, String> query) {
    int partition = -1;
    String taskType = null;
    if (query.containsKey("partition")) {
      partition = Integer.parseInt(query.get("partition"));
    }
  }

  private static Map<String, String> getQuery(HttpExchange httpExchange) {
    URI uri = httpExchange.getRequestURI();
    String query = uri.getRawQuery();
    return Arrays.stream(query.split("&"))
        .collect(Collectors.toMap(v -> v.split("=")[0], v -> v.split("=")[1]));
  }

  private static void sendResponse(HttpExchange httpExchange, String response) throws IOException {
    byte[] bytes = response.getBytes(CHARSET);
    httpExchange.sendResponseHeaders(200, bytes.length);
    OutputStream responseBody = httpExchange.getResponseBody();
    responseBody.write(bytes);
    responseBody.close();
  }
}
