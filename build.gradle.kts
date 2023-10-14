plugins {
    kotlin("jvm") version "1.9.0"
    id("cc.polyfrost.loom") version "0.10.0.5"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("maven-publish")
}

group = "dev.macrohq"
version = "1.0.0"

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public")
    maven("https://repo.polyfrost.cc/releases")
}

val embed: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(embed)

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    compileOnly("org.spongepowered:mixin:0.8.5-SNAPSHOT")
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT:processor")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.0-alpha180")
    embed("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")
}

loom {
    runConfigs {
        named("client") {
            ideConfigGenerated(true)
        }
    }

    launchConfigs {
        getByName("client") {
            property("fml.coreMods.load", "dev.macrohq.macroframework.MFLoader")
            property("devauth.enabled", "true")
        }
    }

    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.macroframework.json")
        mixin.defaultRefmapName.set("mixins.macroframework.refmap.json")
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.macrohq"
            artifactId = "macroframework"
            version = project.version.toString()
            from(components["java"])
        }
    }
}