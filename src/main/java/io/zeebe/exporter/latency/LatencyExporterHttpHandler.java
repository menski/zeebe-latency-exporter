package io.zeebe.exporter.latency;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.HdrHistogram.Histogram;

class LatencyExporterHttpHandler {

  private static final Charset CHARSET = StandardCharsets.UTF_8;

  private final LatencyRecorder recorder;

  LatencyExporterHttpHandler(final LatencyRecorder recorder) {
    this.recorder = recorder;
  }

  private static void sendResponse(final HttpExchange httpExchange, final String response)
      throws IOException {
    final byte[] bytes = response.getBytes(CHARSET);
    httpExchange.sendResponseHeaders(200, bytes.length);
    final OutputStream responseBody = httpExchange.getResponseBody();
    responseBody.write(bytes);
    responseBody.close();
  }

  private static void sendHistogram(final HttpExchange httpExchange, final Histogram histogram)
      throws IOException {
    httpExchange.sendResponseHeaders(200, 0);
    try (final OutputStream responseBody = httpExchange.getResponseBody();
        final PrintStream printStream = new PrintStream(responseBody)) {
      histogram.outputPercentileDistribution(printStream, 1.0);
    }
  }

  private static void sendCount(final HttpExchange httpExchange, final long count) throws IOException {
    sendResponse(httpExchange, String.valueOf(count));
  }

  void resetLatency(final HttpExchange httpExchange) throws IOException {
    recorder.reset();
    sendResponse(httpExchange, "");
  }

  void getJobDurationLatency(final HttpExchange httpExchange) throws IOException {
    sendHistogram(httpExchange, recorder.getJobDurationHistogram());
  }

  void getJobActivationLatency(final HttpExchange httpExchange) throws IOException {
    sendHistogram(httpExchange, recorder.getJobActivationHistogram());
  }

  void getJobCompletionLatency(final HttpExchange httpExchange) throws IOException {
    sendHistogram(httpExchange, recorder.getJobCompletionHistogram());
  }

  void getWorkflowInstanceCount(final HttpExchange httpExchange) throws IOException {
    sendCount(httpExchange, recorder.getWorkflowInstanceDurationHistogram().getTotalCount());
  }

  void getWorkflowInstanceDuration(final HttpExchange httpExchange) throws IOException {
    sendHistogram(httpExchange, recorder.getWorkflowInstanceDurationHistogram());
  }

  void getWorkflowInstanceOverhead(final HttpExchange httpExchange) throws IOException {
    sendHistogram(httpExchange, recorder.getWorkflowInstanceOverheadHistogram());
  }

  void getJobCount(final HttpExchange httpExchange) throws IOException {
    sendCount(httpExchange, recorder.getJobDurationHistogram().getTotalCount());
  }
}
