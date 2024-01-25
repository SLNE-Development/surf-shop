plugins {
    id("dev.slne.java-conventions")
    id("dev.slne.java-shadow-conventions")
}

dependencies {
    compileOnlyApi(libs.io.papermc.paper.api)
    compileOnlyApi(libs.org.jetbrains.annotations)
    compileOnlyApi(libs.dev.slne.surf.api.bukkit.api)
    compileOnlyApi(libs.dev.slne.surf.transaction.api)
}

description = "surf-shop-api"
