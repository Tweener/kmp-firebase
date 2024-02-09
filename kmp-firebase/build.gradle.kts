plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

android {
    namespace = Dependencies.Versions.Firebase.namespace
    compileSdk = Dependencies.Versions.Firebase.compileSDK

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = Dependencies.Versions.Firebase.minSDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
        }
    }

    compileOptions {
        sourceCompatibility = Dependencies.Versions.Compiler.javaCompatibility
        targetCompatibility = Dependencies.Versions.Compiler.javaCompatibility

        isCoreLibraryDesugaringEnabled = true
    }

    dependencies {
        coreLibraryDesugaring(Dependencies.Libraries.Android.desugarJdkLibs)
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")

        compilations.all {
            kotlinOptions {
                jvmTarget = Dependencies.Versions.Compiler.jvmTarget
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "kmp-firebase"
            isStatic = true
        }
    }

    sourceSets {

        commonMain.dependencies {
            implementation(Dependencies.Libraries.napier)
            implementation(Dependencies.Libraries.annotations)

            // Tweener
            implementation(Dependencies.Libraries.Tweener.common)

            // Coroutines
            implementation(Dependencies.Libraries.Coroutines.core)

            // Firebase
            implementation(Dependencies.Libraries.Firebase.firestore)
            implementation(Dependencies.Libraries.Firebase.config)
            api(Dependencies.Libraries.Firebase.auth)
            implementation(Dependencies.Libraries.Firebase.functions)
        }

        androidMain.dependencies {
            // Coroutines
            implementation(Dependencies.Libraries.Coroutines.Android.android)

            // Android
            implementation(Dependencies.Libraries.Android.AndroidX.core)
        }

        iosMain.dependencies {

        }
    }
}

// region Publishing

// Dokka configuration
val dokkaOutputDir = buildDir.resolve("dokka")
tasks.dokkaHtml { outputDirectory.set(file(dokkaOutputDir)) }
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") { delete(dokkaOutputDir) }
val javadocJar = tasks.create<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    from(dokkaOutputDir)
}

group = Dependencies.Versions.Firebase.Maven.group
version = Dependencies.Versions.Firebase.versionName

publishing {
    publications {
        publications.withType<MavenPublication> {
            artifact(javadocJar)

            pom {
                name.set(Dependencies.Versions.Firebase.Maven.name)
                description.set(Dependencies.Versions.Firebase.Maven.description)
                url.set(Dependencies.Versions.Firebase.Maven.packageUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("${Dependencies.Versions.Firebase.Maven.packageUrl}/issues")
                }

                developers {
                    developer {
                        id.set(Dependencies.Versions.Firebase.Maven.Developer.id)
                        name.set(Dependencies.Versions.Firebase.Maven.Developer.name)
                        email.set(Dependencies.Versions.Firebase.Maven.Developer.email)
                    }
                }

                scm {
                    connection.set("scm:git:git://${Dependencies.Versions.Firebase.Maven.gitUrl}")
                    developerConnection.set("scm:git:ssh://${Dependencies.Versions.Firebase.Maven.gitUrl}")
                    url.set(Dependencies.Versions.Firebase.Maven.packageUrl)
                }
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        println("Signing lib...")
        useGpgCmd()
        sign(publishing.publications)
    }
}

// endregion Publishing
