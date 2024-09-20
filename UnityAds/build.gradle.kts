plugins {
    id("adapter-config")
}

afterEvaluate {
    apply(plugin = "adapter-publish")
}

val libraryVersionName by extra("4.12.3.0")

android.defaultConfig.minSdk = 19

