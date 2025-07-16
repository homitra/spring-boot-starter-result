package io.github.homitra.spring.boot.result.infrastructure.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import io.github.homitra.spring.boot.result.Result;
import io.github.homitra.spring.boot.result.annotations.PublishEvent;
import io.github.homitra.spring.boot.result.domain.events.ResultEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that automatically publishes events for methods annotated with {@code @PublishEvent}.
 * 
 * <p>This aspect intercepts method calls annotated with {@code @PublishEvent} and publishes
 * {@link ResultEvent} objects based on the Result outcome and annotation configuration.</p>
 * 
 * <p>Events are published conditionally based on the {@code EventType}:</p>
 * <ul>
 *   <li>SUCCESS - Only when Result indicates success</li>
 *   <li>FAILURE - Only when Result indicates failure</li>
 *   <li>BOTH - Always, regardless of Result outcome</li>
 * </ul>
 * 
 * <p>Requires Spring AOP and AspectJ to be configured in the application.</p>
 * 
 * @author Smit Joshi
 * @see <a href="https://in.linkedin.com/in/smit-joshi814">LinkedIn Profile</a>
 * @since 1.0.0
 */
@Aspect
@Component
public final class EventPublishingAspect {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructs the aspect with the Spring event publisher.
     * 
     * @param eventPublisher Spring's application event publisher
     */
    public EventPublishingAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Intercepts methods annotated with {@code @PublishEvent} and publishes events based on Result outcomes.
     * 
     * @param pjp the proceeding join point representing the intercepted method
     * @param publishEvent the PublishEvent annotation with configuration
     * @return the original method result
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("@annotation(publishEvent)")
    public Object publishResultEvent(ProceedingJoinPoint pjp, PublishEvent publishEvent) throws Throwable {
        Object result = pjp.proceed();

        if (result instanceof Result<?>) {
            Result<?> resultObj = (Result<?>) result;
            String eventName = publishEvent.eventName().isEmpty() 
                ? pjp.getSignature().getName() 
                : publishEvent.eventName();

            boolean shouldPublish = switch (publishEvent.on()) {
                case SUCCESS -> resultObj.isSuccess();
                case FAILURE -> !resultObj.isSuccess();
                case BOTH -> true;
            };

            if (shouldPublish) {
                ResultEvent<?> event = new ResultEvent<>(
                    pjp.getTarget(),
                    eventName, 
                    resultObj, 
                    pjp.getSignature().getName(), 
                    pjp.getArgs()
                );
                eventPublisher.publishEvent(event);
            }
        }

        return result;
    }
}
