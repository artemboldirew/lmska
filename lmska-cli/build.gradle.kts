plugins {
    id("java")
}

group = "com.github.artemboldirew"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":lmska-core"))
    implementation(project(":bring"))
    implementation(project(":lmska-common"))
    testImplementation(project(":lmska-core"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
