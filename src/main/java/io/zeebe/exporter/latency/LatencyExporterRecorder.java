package io.zeebe.exporter.latency;

import io.zeebe.protocol.record.Record;
import org.HdrHistogram.Histogram;

public class LatencyExporterRecorder {

  private final Histogram histogram;

  public LatencyExporterRecorder(Histogram histogram) {
    this.histogram = histogram;
  }

  public void record(Record record) {}
}
