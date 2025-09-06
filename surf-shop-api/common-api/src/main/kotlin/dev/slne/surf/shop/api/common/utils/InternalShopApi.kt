package dev.slne.surf.shop.api.common.utils

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is intended for internal use only. Do not use it in your code unless you are sure you know what you are doing. " +
            "Using this API may lead to unexpected behavior or compatibility issues in future versions."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD
)
annotation class InternalShopApi