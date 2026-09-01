plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "com.sager.jtm.core"
  compileSdk = 36

  defaultConfig {
    minSdk = 26
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    buildConfig = false
  }

  androidResources.enable = false
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  testImplementation(libs.junit)
}
