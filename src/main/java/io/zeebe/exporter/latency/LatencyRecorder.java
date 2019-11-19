package io.zeebe.exporter.latency;

import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.value.JobRecordValue;
import io.zeebe.protocol.record.value.WorkflowInstanceRecordValue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;

public class LatencyRecorder {

  private final Logger log;

  private final Map<Long, Long> workflowInstances = new ConcurrentHashMap<>();
  private final Map<Long, Long> workflowInstanceTaskDurations = new ConcurrentHashMap<>();

  private final Histogram workflowInstanceDurationHistogram = new ConcurrentHistogram(2);
  private final Histogram workflowInstanceOverheadHistogram = new ConcurrentHistogram(2);

  private final Map<Long, Long> jobs = new ConcurrentHashMap<>();
  private final Map<Long, Long> activatedJobs = new ConcurrentHashMap<>();

  private final Histogram jobDurationHistogram = new ConcurrentHistogram(2);
  private final Histogram jobActivationHistogram = new ConcurrentHistogram(2);
  private final Histogram jobCompletionHistogram = new ConcurrentHistogram(2);

  LatencyRecorder(final Logger log) {
    this.log = log;
  }

  void workflowInstanceCreated(final Record<WorkflowInstanceRecordValue> record) {
    workflowInstances.put(record.getKey(), record.getTimestamp());
  }

  void workflowInstanceCompleted(final Record<WorkflowInstanceRecordValue> record) {
    final long workflowInstanceKey = record.getKey();
    final Long startTime = workflowInstances.remove(workflowInstanceKey);

    Long taskDurations = workflowInstanceTaskDurations.remove(workflowInstanceKey);
    if (taskDurations == null) {
      taskDurations = 0L;
    }

    if (startTime != null) {
      final long workflowInstanceDuration = record.getTimestamp() - startTime;
      workflowInstanceDurationHistogram.recordValue(workflowInstanceDuration);

      final long workflowInstanceOverhead = workflowInstanceDuration - taskDurations;
      workflowInstanceOverheadHistogram.recordValue(workflowInstanceOverhead);
    } else {
      log.warn("Unknown workflow instances completed with key {}", workflowInstanceKey);
    }
  }

  void jobCreated(final Record<JobRecordValue> record) {
    jobs.put(record.getKey(), record.getTimestamp());
  }

  void jobActivated(final Record<JobRecordValue> record) {
    activatedJobs.putIfAbsent(record.getKey(), record.getTimestamp());
  }

  void jobCompleted(final Record<JobRecordValue> record) {
    final long jobKey = record.getKey();
    long workflowInstanceKey = record.getValue().getWorkflowInstanceKey();

    final Long startTime = jobs.remove(jobKey);
    Long activationTime = activatedJobs.remove(jobKey);

    if (startTime != null) {
      if (activationTime == null) {
        log.warn(
            "No activation time found for job with key {}, using start time {}", jobKey, startTime);
        activationTime = startTime;
      }

      final long endTime = record.getTimestamp();
      final long duration = endTime - startTime;
      final long activationDuration = activationTime - startTime;
      final long completionDuration = endTime - activationTime;

      jobDurationHistogram.recordValue(duration);
      jobActivationHistogram.recordValue(activationDuration);
      jobCompletionHistogram.recordValue(completionDuration);

      workflowInstanceTaskDurations.computeIfPresent(workflowInstanceKey, (key, value) -> value + duration);
      workflowInstanceTaskDurations.putIfAbsent(workflowInstanceKey, duration);
    } else {
      log.warn("Unknown job completed with key {}", jobKey);
    }
  }

  void reset() {
    workflowInstances.clear();
    workflowInstanceTaskDurations.clear();
    workflowInstanceDurationHistogram.reset();
    workflowInstanceOverheadHistogram.reset();

    jobs.clear();
    activatedJobs.clear();
    jobDurationHistogram.reset();
    jobActivationHistogram.reset();
    jobCompletionHistogram.reset();
  }

  long getWorkflowInstanceCount() {
    return workflowInstanceDurationHistogram.getTotalCount();
  }

  Histogram getWorkflowInstanceDurationHistogram() {
    return workflowInstanceDurationHistogram;
  }

  public Histogram getWorkflowInstanceOverheadHistogram() {
    return workflowInstanceOverheadHistogram;
  }

  Histogram getJobDurationHistogram() {
    return jobDurationHistogram;
  }

  Histogram getJobActivationHistogram() {
    return jobActivationHistogram;
  }

  Histogram getJobCompletionHistogram() {
    return jobCompletionHistogram;
  }
}
