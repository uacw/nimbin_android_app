plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.serialization)
}

android {
    namespace = "tech.nimbus.nimbin"
    compileSdk = 35

    defaultConfig {
        applicationId = "tech.nimbus.nimbin"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "com.google.dagger.hilt.android.testing.HiltTestRunner"

        // Векторные drawable для всех плотностей
        vectorDrawables {
            useSupportLibrary = true
        }

        // ProGuard правила для Release
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    // Signing configs (должны быть объявлены до buildTypes)
    signingConfigs {
        create("release") {
            val ksFile = file("../keystore/release.keystore")
            if (ksFile.exists()) {
                storeFile = ksFile
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else {
                // Фолбэк: невалидный путь, чтобы не падала debug сборка
                println("[warn] release.keystore не найден, release подпись будет использовать debug keystore")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false

            buildConfigField("String", "API_BASE_URL", "\"https://nimbin-back-1de949af6629.herokuapp.com\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            buildConfigField("String", "API_BASE_URL", "\"https://api.nimbin.app\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "false")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Безопасный выбор signingConfig: если release не настроен, используем debug
            signingConfig = (signingConfigs.findByName("release")?.takeIf { file("../keystore/release.keystore").exists() }
                ?: signingConfigs.getByName("debug"))
        }

        create("staging") {
            initWith(buildTypes.getByName("release"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDebuggable = true

            buildConfigField("String", "API_BASE_URL", "\"https://staging-api.nimbin.app\"")
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf(
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xcontext-receivers"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/previous-compilation-data.bin"
        }
    }

    // Lint configuration
    lint {
        baseline = file("lint-baseline.xml")
        checkReleaseBuilds = true
        abortOnError = false  // Временно отключаем остановку на ошибках lint
        warningsAsErrors = false  // Временно отключаем преобразование предупреждений в ошибки
        disable += listOf("ContentDescription", "SelectableText", "NullSafeMutableLiveData")
        enable += listOf("UnusedResources", "UnusedIds")
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.preferences)

    // Modern Material 3 Design System (only stable versions)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.foundation)

    // System UI Controller for status bar/navigation bar theming
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh)

    // Navigation
    implementation(libs.navigationCompose)

    // Ktor
    implementation(libs.ktorClientAndroid)
    implementation(libs.ktorClientContentNegotiation)
    implementation(libs.ktorSerializationKotlinxJson)
    // Serialization (явно, для extension decodeFromString/decodeFromJsonElement)
    implementation(libs.kotlinxSerializationJson)

    // Room
    implementation(libs.roomRuntime)
    implementation(libs.roomKtx)
    ksp(libs.roomCompiler)

    // Hilt
    implementation(libs.hiltAndroid)
    ksp(libs.hiltCompiler)
    implementation(libs.hiltNavigationCompose)
    // Hilt Testing - добавляем недостающие зависимости для тестов
    androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:${libs.versions.hilt.get()}")
    testImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    kspTest("com.google.dagger:hilt-android-compiler:${libs.versions.hilt.get()}")

    // Shared module
    implementation(project(":shared"))

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Desugaring support for Java 8+ API usage
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.3")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Additional test dependencies
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0") // For Flow testing
    testImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")

    // Android instrumented tests
    androidTestImplementation("org.mockito:mockito-android:5.5.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    androidTestImplementation("io.ktor:ktor-client-mock:${libs.versions.ktor.get()}")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")

    // Ktor logging
    implementation("io.ktor:ktor-client-logging:${libs.versions.ktor.get()}")
}
