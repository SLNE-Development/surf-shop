package dev.slne.surf.shop.api.annontation;

import java.lang.annotation.*;

/**
 * Indicates that a value may be null if a certain condition is met
 * <p>
 * When you see this annotation, you should check the documentation to see what the condition is
 * <br>
 * And if you are using this annotation, you should document the condition
 * </p>
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
public @interface MayBeNull {
}
