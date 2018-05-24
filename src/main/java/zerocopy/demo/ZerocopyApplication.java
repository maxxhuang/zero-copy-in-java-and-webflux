package zerocopy.demo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.graphite.GraphiteMeterRegistry;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import reactor.core.publisher.Flux;
import zerocopy.Files;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@SpringBootApplication
@RestController
public class ZerocopyApplication implements WebFluxConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(ZerocopyApplication.class);


    @Value("${download.file}")
    private String downloadFilePath;

    private String fileResourcePath;

    private ResourceLoader resourceLoader;

    private Counter zeroCopy1Counter;
    private Counter zeroCopy2Counter;
    private Counter nonZeroCopyCounter;


    public ZerocopyApplication(ResourceLoader resourceLoader,
                               MeterRegistry meterRegistry) {
        this.resourceLoader = resourceLoader;
        initMetrics(meterRegistry);
    }

    private void initMetrics(MeterRegistry meterRegistry) {
        logger.info("naming convention is {}.", meterRegistry.config().namingConvention());

        meterRegistry.config().namingConvention(NamingConvention.dot);
        this.zeroCopy1Counter = meterRegistry.counter(counterName("zeroCopy1"));
        this.zeroCopy2Counter = meterRegistry.counter(counterName("zeroCopy2"));
        this.nonZeroCopyCounter= meterRegistry.counter(counterName("nonZeroCopy"));
    }

    private static String counterName(String name) {
        return String.format("zerocopyapp.%s.counter", name);
    }

    @GetMapping(value = "/zerocopy1")
    public Resource zeroCopy() {
        try {
            return this.resourceLoader.getResource(this.fileResourcePath);
        } finally {
            this.zeroCopy1Counter.increment();
        }
    }

    /**
     * Or specify resource locations in application.properties by setting
     * <code>
     *     spring.resources.static-locations=classpath:/META-INF/resources/,file:/anotherResources/
     * </code>
     * with default url mapping as "/static/**"
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String dirResourcePath = String.format("file:%s", FilenameUtils.getFullPath(this.downloadFilePath));
        logger.info("'/zerocopy2' => File to serve: under {}", dirResourcePath);

        registry.addResourceHandler("/zerocopy2/**")
                .addResourceLocations(dirResourcePath);
    }

    @GetMapping("/nonzerocopy")
    public Flux<DataBuffer> nonZeroCopy(ServerHttpResponse response) {
        try {
            Path file = Paths.get(this.downloadFilePath);
            return DataBufferUtils.readAsynchronousFileChannel(
                    () -> AsynchronousFileChannel.open(file, StandardOpenOption.READ),
                    response.bufferFactory(),
                    Files.BUFFER_SIZE_IN_BYTES);
        } finally {
            this.nonZeroCopyCounter.increment();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initResourcePath() {
        this.fileResourcePath = String.format("file:%s", this.downloadFilePath);

        logger.info("'/zerocopy1' => File to serve: {}", this.downloadFilePath);
    }


    @Bean
    MeterRegistryCustomizer<GraphiteMeterRegistry> namingConvention() {
        return registry -> registry.config().namingConvention(NamingConvention.dot);
    }

}
