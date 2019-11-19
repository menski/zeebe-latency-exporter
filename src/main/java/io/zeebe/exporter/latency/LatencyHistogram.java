package io.zeebe.exporter.latency;

import java.io.PrintStream;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;

public class LatencyHistogram {

  private final Histogram histogram = new ConcurrentHistogram(3);

  public void recordValue(long milliseconds) {
    histogram.recordValue(milliseconds);
  }

  public void print(PrintStream printStream) {
    histogram.outputPercentileDistribution(printStream, 1.0);
  }


  public void reset() {
    histogram.reset();
  }
}
