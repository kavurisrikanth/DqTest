plugins {
   java
   application
   `maven-publish`
}
repositories {
   jcenter()
   mavenLocal()
}
group = "dataquery"
version = "1.0-SNAPSHOT"
dependencies {
   implementation("com.google.guava:guava:26.0-jre")
   implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.1.6.RELEASE")
   implementation("org.springframework.boot:spring-boot-starter-data-solr:2.1.6.RELEASE")
   implementation("org.postgresql:postgresql:42.2.5")
   implementation("org.flywaydb:flyway-core:5.2.4")
   implementation("org.springframework.boot:spring-boot-starter-web:2.1.6.RELEASE")
   implementation("org.eclipse.persistence:javax.persistence:2.2.1")
   implementation("org.springframework.boot:spring-boot-starter-security:2.1.6.RELEASE")
   implementation("org.springframework.security:spring-security-jwt:1.0.10.RELEASE")
   implementation("org.springframework.security.oauth:spring-security-oauth2:2.3.6.RELEASE")
   implementation("com.graphql-java:graphql-java:2019-07-15T07-36-13-5761d24")
   implementation("com.graphql-java:graphiql-spring-boot-autoconfigure:5.0.2")
   implementation("io.jsonwebtoken:jjwt:0.9.0")
   implementation("org.springframework.boot:spring-boot-starter-websocket:2.1.6.RELEASE")
   implementation("studio.d3e:d3e-lang-java:1.0-SNAPSHOT")
   implementation("io.reactivex.rxjava3:rxjava:3.0.0-RC2")
   implementation("org.eclipse.persistence:javax.persistence:2.2.1")
   implementation("commons-io:commons-io:2.5")
   implementation("org.apache.velocity:velocity:1.7")
   implementation("org.apache.commons:commons-text:1.7")
   implementation("org.apache.commons:commons-compress:1.20")
   implementation("io.reactivex.rxjava3:rxjava:3.0.0-RC2")
   implementation("com.google.googlejavaformat:google-java-format:1.7")
   implementation("com.jcraft:jsch:0.1.55")
   implementation("de.siegmar:logback-gelf:2.0.0")
   implementation("org.codehaus.janino:janino:2.7.8")
   implementation("org.json:json:20210307")
   testCompile("com.h2database:h2:1.4.193")
   testCompile("org.springframework.boot:spring-boot-starter-test:2.1.6.RELEASE")
   compileOnly("org.projectlombok:lombok:1.18.8")
   annotationProcessor("org.projectlombok:lombok:1.18.8")
   testImplementation("junit:junit:4.12")
   compile("javax.mail:javax.mail-api:1.6.0")
   compile("com.sun.mail:javax.mail:1.6.2")
   compile("javax.json:javax.json-api:1.1.4")
}
val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
application {
   mainClassName = "main.DataQueryTest"
}