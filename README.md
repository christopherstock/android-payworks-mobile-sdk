# Payworks Mobile Payment SDK for Android
This Android App is a test implementation of the [Payworks Mobile Payment SDK for Android](https://payworks.mpymnt.com/cp_int_pos_custom_overview/cp_int_pos_custom_installation.html)

# Notes
Please note these items when working with the **Mobile Payment SDK** for Android:

- The Payworks URL with the Maven-Repository needs to be specified inside `settings.gradle`, not inside `build.gradle` as described in the Payworks manual.
```
dependencyResolutionManagement {
  ..
  repositories {
    ..
    maven {
      url 'https://releases.payworks.io/artifactory/mpos/'
..
```

- Downgrade the Dependency for the Payworks Mobile SDK from `2.58` to `2.57`.
This fixes the `Kotlin runtime error in transaction()` issue.

- Fix the Version of the **Kotlin Gradle Plugin** if a Kotlin transpilation problem occurs. (working with 1.5.30)
