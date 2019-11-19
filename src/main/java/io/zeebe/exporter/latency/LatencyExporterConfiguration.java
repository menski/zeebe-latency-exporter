package io.zeebe.exporter.latency;

public class LatencyExporterConfiguration {

  private String host = "0.0.0.0";
  private int port = 8090;
  private int backlog = 100;

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
        + '}';
  }
}
