package io.zeebe.exporter.latency;

import java.time.Duration;

public class LatencyExporterConfiguration {

  private String host = "0.0.0.0";
  private int port = 8090;
  private int backlog = 100;
  private long maxLatencyNs = Duration.ofMinutes(1).toNanos();

  public String getHost() {
    return host;
  }

  public LatencyExporterConfiguration setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public LatencyExporterConfiguration setPort(int port) {
    this.port = port;
    return this;
  }

  public int getBacklog() {
    return backlog;
  }

  public LatencyExporterConfiguration setBacklog(int backlog) {
    this.backlog = backlog;
    return this;
  }

  public long getMaxLatencyNs() {
    return maxLatencyNs;
  }

  public LatencyExporterConfiguration setMaxLatencyNs(long maxLatencyNs) {
    this.maxLatencyNs = maxLatencyNs;
    return this;
  }

  @Override
  public String toString() {
    return "LatencyExporterConfiguration{"
        + "host='"
        + host
        + '\''
        + ", port="
        + port
        + ", backlog="
        + backlog
        + ", maxLatencyNs="
        + maxLatencyNs
        + '}';
  }
}
