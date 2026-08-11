# Switch XML Parsing from TikXml to SimpleXML

This plan outlines the steps to migrate the XML parsing logic from `TikXml` to `SimpleXML` using the Retrofit `converter-simplexml`.

## User Review Required

> [!WARNING]
> **SimpleXML is Deprecated**: The `SimpleXML` library and its Retrofit converter are officially deprecated and no longer maintained. While it is "simpler" to use as it relies on reflection rather than annotation processing, it is slower and may have compatibility issues with modern Android features (like R8/ProGuard) compared to `TikXml`.

## Proposed Changes

### [Component: Build Configuration]

#### [MODIFY] [libs.versions.toml](file:///Users/mahmoudhawas/AndroidStudioProjects/NotificationScheduler/gradle/libs.versions.toml)
- Remove `tikxml` versions and libraries.
- Add `converter-simplexml` library.

#### [MODIFY] [app/build.gradle.kts](file:///Users/mahmoudhawas/AndroidStudioProjects/NotificationScheduler/app/build.gradle.kts)
- Remove TikXml dependencies.
- Add `implementation(libs.retrofit.simplexml)`.
- (Note) We can potentially remove `kapt(libs.tikxml.processor)` but Hilt still requires `kapt`.

### [Component: Data Layer]

#### [MODIFY] [Notification.kt](file:///Users/mahmoudhawas/AndroidStudioProjects/NotificationScheduler/app/src/main/java/com/example/notificationscheduler/core/model/Notification.kt)
- Replace TikXml `@Xml` and `@PropertyElement` annotations with SimpleXML `@Root` and `@Element` annotations.

#### [MODIFY] [NotificationResponse.kt](file:///Users/mahmoudhawas/AndroidStudioProjects/NotificationScheduler/app/src/main/java/com/example/notificationscheduler/core/data/NotificationResponse.kt)
- Replace TikXml annotations with SimpleXML `@Root` and `@ElementList`.

#### [MODIFY] [NetworkModule.kt](file:///Users/mahmoudhawas/AndroidStudioProjects/NotificationScheduler/app/src/main/java/com/example/notificationscheduler/core/di/NetworkModule.kt)
- Remove `TikXml` provider.
- Update `provideRetrofit` to use `SimpleXmlConverterFactory.create()`.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure successful compilation and dependency resolution.

### Manual Verification
- Deploy the app and verify that notifications are correctly parsed and displayed using SimpleXML.
- Check for any reflection-related crashes during parsing.
