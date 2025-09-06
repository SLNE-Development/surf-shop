plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    withCloudClientPaper()
    mainClass("dev.slne.surf.template.paper.PaperMain")
    bootstrapper("dev.slne.surf.template.paper.PaperBootstrap")
}

dependencies {
    api(project(":surf-shop-core:common-core"))
}