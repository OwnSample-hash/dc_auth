plugins {
    id("java")
    id("io.ktor.plugin") version "2.3.5"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.12")
    implementation("net.dv8tion:JDA:5.0.0-beta.12")
    //Change 'implementation' to 'compile' in old Gradle versions
    implementation("net.dv8tion:JDA:5.0.0-beta.12")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
}

application {
    mainClass.set("me.ownsample.dc_auth.dc_auth")
}

ktor {
    fatJar {
        archiveFileName.set("dc_auth-fat-$version.jar")
    }
}
