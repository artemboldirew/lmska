plugins {
    id("java")
    id("application")
}

group = "com.github.artemboldirew"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":bring"))
    implementation(project(":bibernate"))
    runtimeOnly(project(":lmska-cli"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("github.artemboldirew.lmska.core.LmskaApplication")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
