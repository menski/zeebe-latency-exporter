package io.zeebe.exporter.latency;

import com.sun.net.httpserver.HttpServer;
import io.zeebe.exporter.api.Exporter;
import io.zeebe.exporter.api.context.Context;
import io.zeebe.exporter.api.context.Controller;
import io.zeebe.protocol.record.Record;
import io.zeebe.protocol.record.intent.JobIntent;
import io.zeebe.protocol.record.intent.WorkflowInstanceIntent;
import io.zeebe.protocol.record.value.BpmnElementType;
import io.zeebe.protocol.record.value.JobRecordValue;
import io.zeebe.protocol.record.value.WorkflowInstanceRecordValue;
import java.net.InetSocketAddress;
import org.slf4j.Logger;

public class LatencyExporter implements Exporter {

  private static HttpServer server;
  private static LatencyRecorder recorder;

  private Logger log;
  private LatencyExporterConfiguration config;
  private Controller controller;

  @Override
  public void configure(final Context context) throws Exception {
    config = context.getConfiguration().instantiate(LatencyExporterConfiguration.class);
    log = context.getLogger();

    if (recorder == null) {
      recorder = new LatencyRecorder(log);
    }

    if (server == null) {
      final LatencyExporterHttpHandler handler = new LatencyExporterHttpHandler(recorder);
      server =
          HttpServer.create(
              new InetSocketAddress(config.getHost(), config.getPort()), config.getBacklog());
      server.createContext("/reset", handler::resetLatency);
      server.createContext("/instance/count", handler::getWorkflowInstanceCount);
      server.createContext("/instance/duration", handler::getWorkflowInstanceDuration);
      server.createContext("/instance/overhead", handler::getWorkflowInstanceOverhead);
      server.createContext("/job/count", handler::getJobCount);
      server.createContext("/job/duration", handler::getJobDurationLatency);
      server.createContext("/job/activation", handler::getJobActivationLatency);
      server.createContext("/job/completion", handler::getJobCompletionLatency);
      server.start();
    }
  }

  @Override
  public void open(final Controller controller) {
    this.controller = controller;
  }

  @Override
  public void export(final Record record) {
    switch (record.getValueType()) {
      case WORKFLOW_INSTANCE:
        exportWorkflowInstanceRecord(record);
        break;
      case JOB:
        exportJob(record);
        break;
    }
    controller.updateLastExportedRecordPosition(record.getPosition());
  }

  private void exportWorkflowInstanceRecord(final Record<WorkflowInstanceRecordValue> record) {
    if (BpmnElementType.PROCESS == record.getValue().getBpmnElementType()) {
      final WorkflowInstanceIntent intent = (WorkflowInstanceIntent) record.getIntent();
      switch (intent) {
        case ELEMENT_ACTIVATING:
          recorder.workflowInstanceCreated(record);
          break;
        case ELEMENT_COMPLETED:
        case ELEMENT_TERMINATED:
          recorder.workflowInstanceCompleted(record);
          break;
      }
    }
  }

  private void exportJob(final Record<JobRecordValue> record) {
    final JobIntent intent = (JobIntent) record.getIntent();
    switch (intent) {
      case CREATED:
        recorder.jobCreated(record);
        break;
      case ACTIVATED:
        recorder.jobActivated(record);
        break;
      case COMPLETED:
      case FAILED:
      case CANCELED:
        recorder.jobCompleted(record);
        break;
    }
  }
}
