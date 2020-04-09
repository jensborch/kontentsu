package dk.kontentsu.util;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class LoggerProducer {

    @Produces
    public Logger logger(InjectionPoint injectionPoint) {
        Class<?> targetClass = injectionPoint.getMember().getDeclaringClass();
        return LoggerFactory.getLogger(targetClass);
    }
}