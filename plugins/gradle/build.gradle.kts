plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.evosuite","evosuite-runtime","1.0.6")
    implementation("org.zeroturnaround","zt-exec","1.12")
    implementation("org.apache.maven", "maven-resolver-provider", "3.8.1")
    implementation("org.apache.maven.resolver", "maven-resolver-connector-basic", "1.7.0")
    implementation("org.apache.maven.resolver", "maven-resolver-transport-http", "1.7.0")
}

gradlePlugin{
    plugins{
        create("plugin"){
            id = "org.evosuite.plugin"
            implementationClass = "org.evosuite.gradle.EvoSuiteGradlePlugin"
        }
    }
}

tasks.compileKotlin.get().kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"