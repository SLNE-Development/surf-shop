import dev.slne.surf.microservice.gradle.plugin.rabbit.RabbitModule

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-raw")
    id("dev.slne.surf.microservice")
}

surfRawPaperApi {
    withCoreCommon()
}

surfMicroservice {
    withRabbitModule(RabbitModule.CLIENT_API)
}

dependencies {
    api(projects.surfShopCore.surfShopCoreCommon)
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:1.21.11-3.0.1")
}