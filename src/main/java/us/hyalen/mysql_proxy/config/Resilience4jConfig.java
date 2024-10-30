package us.hyalen.mysql_proxy.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.BadSqlGrammarException;
import us.hyalen.mysql_proxy.core.ResourceNotFoundException;

import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

//@Configuration
public class Resilience4jConfig {

//    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Create a CircuitBreakerRegistry with default configuration
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        // Define a custom exception predicate to ignore specific exceptions
        Predicate<Throwable> recordExceptionPredicate = throwable -> {
            Throwable cause = (throwable instanceof CompletionException) ? throwable.getCause() : throwable;
            // Return true if the exception should be recorded as a failure
            // We want to ignore ResourceNotFoundException and BadSqlGrammarException
            return !(cause instanceof ResourceNotFoundException || cause instanceof BadSqlGrammarException);
        };

        // Create custom configurations for each DB type
        CircuitBreakerConfig dbConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .minimumNumberOfCalls(5)
                .slidingWindowSize(10)
                .recordException(recordExceptionPredicate)
                .build();

        // Register circuit breakers with their configurations under the appropriate names
        registry.circuitBreaker("MYSQL_DEV", dbConfig);
        registry.circuitBreaker("REDSHIFT_DEV", dbConfig);
        registry.circuitBreaker("H2_DEV", dbConfig);
        // Add other circuit breakers as needed

        return registry;
    }
}
