plugins {
    id("com.android.application") version "8.5.0"
    id("org.jetbrains.kotlin.android") version "1.9.22"
    id("com.google.gms.google-services") version "4.4.0"
    id("org.jetbrains.kotlin.kapt") version "1.9.22"
    id("com.google.dagger.hilt.android") version "2.51"
    //    id("com.google.devtools.ksp")
}


android {
    namespace = "com.example.shots"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shots"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }

    }

    buildTypes {

        debug {

        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }

    kotlinOptions {
        jvmTarget = "19"
    }


//
//    configurations.all {
//        resolutionStrategy.force("org.conscrypt:conscrypt-openjdk-uber:2.5.2")
//    }
//
//    configurations.implementation {
//        exclude("org.conscrypt", "conscrypt-openjdk-uber:2.5.2")
//    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
            )
        )
    }

}

//Lottie
dependencies {
    val lottieVersion = "6.4.0"
    implementation("com.airbnb.android:lottie-compose:$lottieVersion")
}

//Shimmer
dependencies {
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.0")
}


/** These should be the most updated getStream dependencies
 *
 *
 */

//GetStream

dependencies {

    implementation("io.getstream:stream-chat-java:1.22.2")

    implementation("io.getstream:stream-chat-android-compose:6.0.8")
    implementation("io.getstream:stream-chat-android-offline:6.0.8")
    implementation("io.getstream:stream-android-push-firebase:1.1.7")


    implementation("androidx.compose.material:material-icons-extended:1.6.8")
}

//Java-JWT for generating JWT tokens for chat messaging via GetStream


dependencies {
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")
}


//Sendbird
//dependencies {
//    implementation("com.sendbird.sdk:sendbird-chat:4.16.0")
//}

////Sendbird
//dependencies {
//    implementation("com.sendbird.sdk:sendbird-chat:4.16.0") {
//        // Exclude conscrypt-android from Sendbird SDK
////        exclude("org.conscrypt", "conscrypt-android")
//    }
//    implementation("org.conscrypt:conscrypt-openjdk-uber:2.5.2") {
//        // Exclude conscrypt-android from conscrypt-openjdk-uber
//        exclude("org.conscrypt", "conscrypt-android")
//    }
//}

//CometChat
//dependencies {
//    implementation("com.cometchat:chat-sdk-android:4.0.5")
//    implementation("com.cometchat:calls-sdk-android:4.0.2")
//}


//Hilt
dependencies {
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    // For instrumentation tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.51")

    // For local unit tests
    testImplementation("com.google.dagger:hilt-android-testing:2.51")
    kaptTest("com.google.dagger:hilt-compiler:2.51")
}

//for Hilt
kapt {
    correctErrorTypes = true
}




dependencies {
    // Compose UI
    implementation("androidx.compose.ui:ui:1.6.8")

    // Compose runtime
    implementation("androidx.compose.runtime:runtime:1.6.8")

    // LiveData integration for Compose
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")
}


dependencies {
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    kapt("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")


    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$roomVersion")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$roomVersion")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$roomVersion")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$roomVersion")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$roomVersion")
}

//SignalOne
dependencies {
    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
}


//Accompanist Permissions
dependencies {
    val version = "0.33.0-alpha"
    implementation("com.google.accompanist:accompanist-permissions:$version")
}


//Android Testing
dependencies {

    constraints {
        implementation("androidx.test:core:1.6.1") {
            because("This version is required by other dependencies.")
        }

        implementation("androidx.test.ext:junit:1.2.1") {
            because("This is the preferred version for compatibility.")
        }
    }

    // To use the androidx.test.core APIs
    androidTestImplementation("androidx.test:core:1.6.1")
    // Kotlin extensions for androidx.test.core
    androidTestImplementation("androidx.test:core-ktx:1.6.1")

    // To use the androidx.test.espresso
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")

    // To use the JUnit Extension APIs
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    // Kotlin extensions for androidx.test.ext.junit
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")

    // To use the Truth Extension APIs
    androidTestImplementation("androidx.test.ext:truth:1.5.0")

    // To use the androidx.test.runner APIs
    androidTestImplementation("androidx.test:runner:1.5.0")

    // To use android test orchestrator
    androidTestUtil("androidx.test:orchestrator:1.5.0")


    // Espresso dependencies
//        androidTestImplementation( "androidx.test.espresso:espresso-core:$espressoVersion")
//        androidTestImplementation( "androidx.test.espresso:espresso-contrib:$espressoVersion")
//        androidTestImplementation( "androidx.test.espresso:espresso-intents:$espressoVersion")
//        androidTestImplementation( "androidx.test.espresso:espresso-accessibility:$espressoVersion")
//        androidTestImplementation( "androidx.test.espresso:espresso-web:$espressoVersion")
//        androidTestImplementation( "androidx.test.espresso.idling:idling-concurrent:$espressoVersion")

    // The following Espresso dependency can be either "implementation",
    // or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK"s compile classpath or the test APK
    // classpath.
//        androidTestImplementation( "androidx.test.espresso:espresso-idling-resource:$espressoVersion")


    //espresso
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val mockkVersion = "1.13.11"
    //mockk
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("io.mockk:mockk-android:${mockkVersion}")
    testImplementation("io.mockk:mockk-agent:${mockkVersion}")
    androidTestImplementation("io.mockk:mockk-android:${mockkVersion}")
    androidTestImplementation("io.mockk:mockk-agent:${mockkVersion}")

    //robolectric
//    testImplementation("org.robolectric:robolectric:4.11")

//    testImplementation("org.mockito:mockito-inline:2.8.47")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    // Required -- JUnit 4 framework
    testImplementation("junit:junit:4.13.2")
    // Optional -- Robolectric environment
    testImplementation("androidx.test:core:1.6.1")
    // Optional -- Mockito framework
    testImplementation("org.mockito:mockito-core:5.7.0")
    // Optional -- mockito-kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    // Optional -- Mockk framework
    testImplementation("io.mockk:mockk:1.13.11")

}

//Firebase
dependencies {

    implementation("androidx.compose.ui:ui-test-junit4-android:1.6.8")
    implementation("androidx.compose.ui:ui-test-android:1.6.8")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    implementation("com.google.firebase:firebase-appcheck-playintegrity")

    implementation("com.google.firebase:firebase-appcheck-debug")

    implementation("com.google.firebase:firebase-messaging")

    //This may not be able to be used in conjunction with Android SDK
    //How do I know, because I saw it here on stack overflow -
    //https://stackoverflow.com/questions/76089838/error-cannot-find-symbol-setcredentialsgooglecredentials-fromstreamserviceac

//    implementation("com.google.firebase:firebase-admin:9.2.0")


    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")


    // Declare the dependency for the Cloud Firestore library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-firestore")

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Add the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-storage")

    // Media3 Dependency
    dependencies {
        val media3Version = "1.3.1"

        // For media playback using ExoPlayer
        implementation("androidx.media3:media3-exoplayer:$media3Version")

        // For DASH playback support with ExoPlayer
        implementation("androidx.media3:media3-exoplayer-dash:$media3Version")
        // For HLS playback support with ExoPlayer
        implementation("androidx.media3:media3-exoplayer-hls:$media3Version")
        // For SmoothStreaming playback support with ExoPlayer
        implementation("androidx.media3:media3-exoplayer-smoothstreaming:$media3Version")
        // For RTSP playback support with ExoPlayer
        implementation("androidx.media3:media3-exoplayer-rtsp:$media3Version")
        // For MIDI playback support with ExoPlayer (see additional dependency requirements in
        // https://github.com/androidx/media/blob/release/libraries/decoder_midi/README.md)
//        implementation("androidx.media3:media3-exoplayer-midi:$media3_version")
        // For ad insertion using the Interactive Media Ads SDK with ExoPlayer
        implementation("androidx.media3:media3-exoplayer-ima:$media3Version")

        // For loading data using the Cronet network stack
        implementation("androidx.media3:media3-datasource-cronet:$media3Version")
        // For loading data using the OkHttp network stack
        implementation("androidx.media3:media3-datasource-okhttp:$media3Version")
        // For loading data using librtmp
        implementation("androidx.media3:media3-datasource-rtmp:$media3Version")

        // For building media playback UIs
        implementation("androidx.media3:media3-ui:$media3Version")
        // For building media playback UIs for Android TV using the Jetpack Leanback library
        implementation("androidx.media3:media3-ui-leanback:$media3Version")

        // For exposing and controlling media sessions
        implementation("androidx.media3:media3-session:$media3Version")

        // For extracting data from media containers
        implementation("androidx.media3:media3-extractor:$media3Version")

        // For integrating with Cast
        implementation("androidx.media3:media3-cast:$media3Version")

        // For scheduling background operations using Jetpack Work's WorkManager with ExoPlayer
        implementation("androidx.media3:media3-exoplayer-workmanager:$media3Version")

        // For transforming media files
        implementation("androidx.media3:media3-transformer:$media3Version")

        // For applying effects on video frames
        implementation("androidx.media3:media3-effect:$media3Version")

        // For muxing media files
        implementation("androidx.media3:media3-muxer:$media3Version")

        // Utilities for testing media components (including ExoPlayer components)
        implementation("androidx.media3:media3-test-utils:$media3Version")
        // Utilities for testing media components (including ExoPlayer components) via Robolectric
//        implementation("androidx.media3:media3-test-utils-robolectric:$media3Version")

        // Common functionality for reading and writing media containers
        implementation("androidx.media3:media3-container:$media3Version")
        // Common functionality for media database components
        implementation("androidx.media3:media3-database:$media3Version")
        // Common functionality for media decoders
        implementation("androidx.media3:media3-decoder:$media3Version")
        // Common functionality for loading data
        implementation("androidx.media3:media3-datasource:$media3Version")
        // Common functionality used across multiple media libraries
        implementation("androidx.media3:media3-common:$media3Version")
    }


    //Glide
    implementation("com.github.bumptech.glide:compose:1.0.0-beta01")

    // More Compose
//    implementation("androidx.compose.ui:ui:1.6.1")
//    implementation("androidx.compose.ui:ui-tooling:1.6.1")
//    implementation("androidx.compose.foundation:foundation:1.6.1")
//    implementation("androidx.compose.material:material:1.6.1")
//    implementation("androidx.compose.runtime:runtime-livedata:1.6.1")
//    implementation("androidx.compose.ui:ui-video:1.6.1")
//    implementation("androidx.compose.ui:ui:1.6.1")
//    implementation("androidx.compose.ui:ui-tooling:1.6.1")
//    implementation("androidx.compose.foundation:foundation:1.6.1")
//    implementation("androidx.compose.material:material:1.6.1")
//    implementation("androidx.compose.runtime:runtime-livedata:1.6.1")
//    implementation("androidx.compose.ui:ui-video:1.6.1")


    // CameraX
    dependencies {
        // CameraX core library using the camera2 implementation
//        val camerax_version = "1.4.0-alpha04"
        val cameraxVersion = "1.3.4"
        // The following line is optional, as the core library is included indirectly by camera-camera2
        implementation("androidx.camera:camera-core:${cameraxVersion}")
        implementation("androidx.camera:camera-camera2:${cameraxVersion}")
        // If you want to additionally use the CameraX Lifecycle library
        implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
        // If you want to additionally use the CameraX VideoCapture library
        implementation("androidx.camera:camera-video:${cameraxVersion}")
        // If you want to additionally use the CameraX View class
        implementation("androidx.camera:camera-view:${cameraxVersion}")
        // If you want to additionally add CameraX ML Kit Vision Integration
//        implementation("androidx.camera:camera-mlkit-vision:${camerax_version}")
        // If you want to additionally use the CameraX Extensions library
        implementation("androidx.camera:camera-extensions:${cameraxVersion}")
    }

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1") // For UI components (optional)

    dependencies {
        // Jetpack Compose
        implementation("androidx.compose.ui:ui:1.6.8")
        implementation("androidx.compose.material:material:1.6.8")
        implementation("androidx.compose.ui:ui-tooling:1.6.8")
        // ExoPlayer
        implementation("com.google.android.exoplayer:exoplayer:2.19.1")
    }

    //Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    //DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // More exo and accompanist
//    implementation("dev.chrisbanes.accompanist:accompanist-video:0.20.0")

    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    //Accompanist dependency
    implementation("com.google.accompanist:accompanist-pager:0.32.0")

    //Ksp dependency
//    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.8.21-1.0.11")

    //Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.51")
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-video:1.3.4")
    implementation("androidx.compose.material3:material3-android:1.2.1")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.8")
    implementation("com.google.android.material:material:1.12.0")
    kapt("com.google.dagger:hilt-compiler:2.51")


    // For instrumentation tests
//    androidTestImplementation"(com.google.dagger:hilt-android-testing:2.50")
//    androidTestAnnotationProcessor 'com.google.dagger:hilt-compiler:2.50'

    // For local unit tests
    testImplementation("com.google.dagger:hilt-android-testing:2.51")
    testAnnotationProcessor("com.google.dagger:hilt-compiler:2.51")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")


    dependencies {
        implementation("androidx.compose.material:material:1.6.8")
    }

    dependencies {
        implementation("androidx.core:core-splashscreen:1.0.1")
    }

    dependencies {
        implementation("androidx.compose.ui:ui:1.6.8")
        implementation("androidx.compose.ui:ui-tooling:1.6.8")
        implementation("androidx.compose.runtime:runtime:1.6.8")
        implementation("androidx.compose.foundation:foundation:1.6.8")
        implementation("androidx.compose.material3:material3:1.2.1")
//        implementation("androidx.compose.ui:ui-android-view:1.0.5")
        implementation("androidx.activity:activity-compose:1.9.0")
//        implementation("androidx.fragment:fragment-compose:1.4.0")
        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
        implementation("androidx.compose.ui:ui-tooling-preview:1.6.8")
        implementation("androidx.activity:activity-ktx:1.9.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    }

    dependencies {
        val lifecycleVersion = "2.8.3"
        val archVersion = "2.2.0"

        //OKHTTP for Getting Stuff From The Internet
        implementation("com.squareup.okhttp3:okhttp:4.12.0")

        //Google Places API for Location
        implementation("com.google.android.libraries.places:places:3.5.0")

        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")

        // ViewModel
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
        // ViewModel utilities for Compose
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
        // LiveData
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
        // Lifecycles only (without ViewModel or LiveData)
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
        // Lifecycle utilities for Compose
        implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")

        // Saved state module for ViewModel
        implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycleVersion")

        // Annotation processor
        // alternately - if using Java8, use the following instead of lifecycle-compiler
        implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

        // optional - helpers for implementing LifecycleOwner in a Service
        implementation("androidx.lifecycle:lifecycle-service:$lifecycleVersion")

        // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
        implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")

        // optional - ReactiveStreams support for LiveData
        implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycleVersion")

        // optional - Test helpers for LiveData
        testImplementation("androidx.arch.core:core-testing:$archVersion")

        // optional - Test helpers for Lifecycle runtime
        testImplementation("androidx.lifecycle:lifecycle-runtime-testing:$lifecycleVersion")
    }


    android {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.9"
        }

        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}