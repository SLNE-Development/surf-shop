plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withCloudServer()
}

dependencies {
    api(project(":surf-shop-core:common-core"))
}