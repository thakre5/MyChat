plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // or kapt if you use annotation processor
}

dependencies {
    // Room components
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // SQLCipher for Android (AES-256 encryption backend)
    implementation("net.zetetic:sqlcipher-android:4.5.4@aar")
    implementation("androidx.sqlite:sqlite:2.4.0")

    // AndroidX Security (EncryptedSharedPreferences for safe key storage)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
}
