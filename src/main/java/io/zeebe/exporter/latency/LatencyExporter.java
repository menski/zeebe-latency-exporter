package io.zeebe.exporter.latency;

import com.sun.net.httpserver.HttpServer;
import io.zeebe.exporter.api.Exporter;
import io.zeebe.exporter.api.context.Context;
import io.zeebe.exporter.api.context.Controller;
import io.zeebe.protocol.record.Record;
import java.net.InetSocketAddress;
import org.slf4j.Logger;

public class LatencyExporter implements Exporter {

  private static HttpServer server;
  private static LatencyExporterRecorder recorder;

  private Logger log;
  private LatencyExporterConfiguration config;

  @Override
  public void configure(Context context) throws Exception {
    config = context.getConfiguration().instantiate(LatencyExporterConfiguration.class);
    log = context.getLogger();

    LatencyExporterHistogramRegistry registry =
        new LatencyExporterHistogramRegistry(config.getMaxLatencyNs());

    LatencyExporterHttpHandler handler = new LatencyExporterHttpHandler(registry);
    server =
        HttpServer.create(
            new InetSocketAddress(config.getHost(), config.getPort()), config.getBacklog());
    server.createContext("/", handler::getLatency);
    server.createContext("/count", handler::getCount);
    server.createContext("/reset", handler::resetLatency);

    LatencyExporterRecorder recorder = new LatencyExporterRecorder(histogram);
  }

  @Override
  public void open(Controller controller) {}

  @Override
  public void export(Record record) {
    recorder.record(record);
  }
}
