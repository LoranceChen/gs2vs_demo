package gs2vs.core.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.ResourceAttributes;


public class TracingUtils {

    // auto config
//    public OpenTelemetrySdk openTelemetrySdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    // manual config with log to console
    Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "gs2vs-server").put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .setResource(resource)
            .build();

    SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build())
            .setResource(resource)
            .build();

    SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(SystemOutLogRecordExporter.create()).build())
            .setResource(resource)
            .build();

    public OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setMeterProvider(sdkMeterProvider)
            .setLoggerProvider(sdkLoggerProvider)
            .setPropagators(ContextPropagators.create(TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
            .buildAndRegisterGlobal();

//    ManagedChannel jaegerChannel = ManagedChannelBuilder.forAddress("localhost", 3336)
//            .usePlaintext()
//            .build();
//
//    JaegerGrpcSpanExporter jaegerExporter = JaegerGrpcSpanExporter.builder()
//            .setEndpoint("localhost:3336")
//            .setTimeout(30, TimeUnit.SECONDS)
//            .build();
//
//    SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
//            .addSpanProcessor(BatchSpanProcessor.builder(jaegerExporter).build())
//            .build();
}
