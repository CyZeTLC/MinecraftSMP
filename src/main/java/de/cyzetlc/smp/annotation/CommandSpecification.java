package de.cyzetlc.smp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE})
public @interface CommandSpecification {
    String command();

    String permission() default "";

    String[] aliases() default {};

    int cooldownValue() default 0;

    TimeUnit cooldownType() default TimeUnit.MILLISECONDS;
    enum TimeUnit {
        MILLISECONDS(1),
        SECONDS(1000),
        MINUTES(60000),
        HOURS(3600000),
        DAYS(86400000);

        private final int value;

        TimeUnit(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
}
