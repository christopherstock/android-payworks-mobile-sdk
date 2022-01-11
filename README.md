# Payworks Mobile Payment SDK for Android
This Android App is a test implementation of the [Payworks Mobile Payment SDK for Android](https://payworks.mpymnt.com/cp_int_pos_custom_overview/cp_int_pos_custom_installation.html)

# Manual
The following manual has been used in order to implement the Payworks mPOS-SDK and the transaction testcode:
https://payworks.mpymnt.com/cp_int_pos_custom_overview/cp_int_pos_custom_installation.html

# Fixes
Please note these fixes compared to the manual in order for the **Mobile Payment SDK** for Android to work:

- The Payworks URL with the Maven-Repository needs to specify the **https** rather than the **http** protocol.
- The Payworks URL with the Maven-Repository needs to be specified inside `settings.gradle`,
  and **not** inside `build.gradle` as described in the Payworks manual.
```
dependencyResolutionManagement {
  ..
  repositories {
    ..
    maven {
      url 'https://releases.payworks.io/artifactory/mpos/'
..
```
- Downgrade the Dependency for the Payworks Mobile SDK to `2.57`.
Higher versions yield a Kotlin runtime error in transaction() issue. (`NoSuchMethodException`)
- Fix the Version of the **Kotlin Gradle Plugin** if a Kotlin transpilation problem occurs. (working with 1.5.30)

# Backlog
- TODO add input fields for amount, drop-down currency
- TODO Disable "Start Transaction" button while a transaction is running
- TODO unify start and cancel transaction to one single button
