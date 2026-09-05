import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// 读取 DeepSeek 密钥，优先级：secrets.properties > local.properties > 环境变量 DEEPSEEK_API_KEY
// 两个 properties 文件均已 gitignore，不会提交到仓库；模板见 secrets.properties.example
val secretProperties = Properties()
val secretFile = rootProject.file("secrets.properties")
if (secretFile.exists()) secretFile.inputStream().use { secretProperties.load(it) }

val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) localFile.inputStream().use { localProperties.load(it) }

val deepseekApiKey: String = secretProperties.getProperty("DEEPSEEK_API_KEY")
    ?.takeIf { it.isNotBlank() }
    ?: localProperties.getProperty("DEEPSEEK_API_KEY")?.takeIf { it.isNotBlank() }
    ?: (System.getenv("DEEPSEEK_API_KEY") ?: "")

android {
    namespace = "com.example.lifehelper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lifehelper"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // DeepSeek API 密钥（从 secrets.properties 注入；未配置时为空字符串）
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"$deepseekApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM（统一管理 Compose 各库版本）
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)

    // 核心
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // 导航
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // 协程（通过 ktx 传递，显式引入以明确依赖）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // 网络（仅翻译功能）
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // 测试
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
