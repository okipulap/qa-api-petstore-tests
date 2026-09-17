plugins {
    id("java")
    id("io.qameta.allure") version "4.1.0"
    id("checkstyle")
}

group = "com.github.okipulap"
version = "0.1.0"

val allureVersion = "2.35.3"
val checkstyleVersion = "10.26.1"

repositories {
    mavenCentral()
}

checkstyle {
    toolVersion = checkstyleVersion
    configFile = file("$rootDir/config/checkstyle/checkstyle.xml")
    maxErrors = 0
}

dependencies {

    //lombok
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    // Allure
    testImplementation("io.qameta.allure:allure-assertj")
    testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
    testImplementation("io.qameta.allure:allure-rest-assured")
    testImplementation("io.qameta.allure:allure-junit5")

    //REST-Assured
    testImplementation("io.rest-assured:rest-assured:6.0.0")

    //jackson
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    //dotenv
    implementation("io.github.cdimascio:dotenv-java:3.2.0")

    //json-schema-validator
    testImplementation("io.rest-assured:json-schema-validator:6.0.0")

    //javaFaker
    testImplementation("com.github.javafaker:javafaker:1.0.2")

    //JUnit5
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //AssertJ
    testImplementation("org.assertj:assertj-core:3.27.7")
}

val writeAllureEnvironment by tasks.registering {
doLast {
	val resultsDir = layout.buildDirectory.dir("allure-results").get().asFile
	resultsDir.mkdirs()
	File(resultsDir, "environment.properties").writeText(
		"""
			Environment = local-docker
			Petstore.Base.URI = ${System.getenv("PETSTORE_BASE_URI") ?: "http://localhost:8080/api/"}
			Java.Version = ${System.getProperty("java.version")}
			OS = ${System.getProperty("os.name")}
		""".trimIndent() + "\n"
	)
}
}

tasks.named("allureReport") {
	dependsOn(writeAllureEnvironment)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn("checkstyleTest")
}

tasks.register<Test>("smokeTest") {
	useJUnitPlatform() {
		includeTags("Smoke")
	}
	group = "verification"
	description = "Набор критичных проверок"
}

tasks.register<Test>("regressionTest") {
	useJUnitPlatform() {
		includeTags("Positive", "Negative")
	}
	group = "verification"
	description = "Полный регресс"
}

