plugins {
    id("dev.slne.surf.surfapi.gradle.paper-raw")
}

surfRawPaperApi {
    withSurfDatabaseR2dbc("1.1.0-SNAPSHOT", "dev.slne.surf.shop.libs.db")
}

dependencies {
    api(project(":surf-shop-core"))
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:1.21.11-3.0.1")
}