#!/bin/bash
sed -i '/\[versions\]/a \
vico = "1.15.0"' gradle/libs.versions.toml

sed -i '/\[libraries\]/a \
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }\
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }\
vico-core = { group = "com.patrykandpatrick.vico", name = "core", version.ref = "vico" }' gradle/libs.versions.toml

sed -i '/implementation(libs.androidx.navigation.compose)/a \
    implementation(libs.vico.compose)\
    implementation(libs.vico.compose.m3)\
    implementation(libs.vico.core)' app/build.gradle.kts
