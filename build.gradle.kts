plugins {
  java
  id("org.springframework.boot") version "3.2.4"
  id("io.spring.dependency-management") version "1.1.4"
}

group = "ph.ota"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.projectlombok:lombok:1.18.32")
  implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
  annotationProcessor("org.projectlombok:lombok:1.18.32")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("io.r2dbc:r2dbc-h2")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.instancio:instancio-junit:4.5.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
