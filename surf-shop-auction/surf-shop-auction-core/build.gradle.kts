plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

surfCoreApi {
    withCoreCommon()
    withSurfDatabaseR2dbc("1.1.1-SNAPSHOT", "libs.database")
}

dependencies {
    api(project(":surf-shop-auction:surf-shop-auction-api"))
}