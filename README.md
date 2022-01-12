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
- MOCKED-Mode must be set in the TransactionProvider **and** in the Accessory
```
      // For starting transaction in mocked mode use fallowing provider:
      val transactionProvider = Mpos.createTransactionProvider(
          this,
          ProviderMode.MOCK,
          "cbf4d153-e78a-4937-8ece-6b1ec948a2f9",
          "ZCMNdotEb3dLkRWabOxUsqe20hDq31ml"
      )
    
      /* When using the Bluetooth Miura, use the following parameters: */
      val accessoryParameters = AccessoryParameters.Builder(AccessoryFamily.MOCK)
          .mocked()
          .build()
```

# Backlog
- TODO CHRISTOPHER: unify styles for buttons / different colors
- TODO CAMILLE: Button 'clear console'
- TODO SIMON: scroll log output on appending logs

- TODO update gradle build tools to latest version?
- TODO unify start and cancel transaction to one single button
- TODO extract transaction-handling to Service
- TODO Signature Required
- TODO Map for assignment of specific amounts (read all 'AMOUNT_'-fields from DefaultMockConfiguration via reflection?)!
- TODO TDD Feature Approach
- TODO move test project to GitLab

- TODO implement 'Refunds'
- TODO 2nd library (mPOS PayButton SDK) with 2nd fragment and demo
  https://payworks.mpymnt.com/cp_int_pos_paybutton_overview/cp_int_pos_paybutton_integration.html
  - TODO Stripe Payments
  - TODO Signature Required

## Nice to Haves
- TODO Colored line output in log console
- TODO Different Layout for Landscape mode?
- TODO IP/Port as input field?
- TODO Move assignment of Button callback listeners from FirstFragment to MainActivity?
- TODO Introduce Coding Style Linter (+GitHub/GitLab CI/CD)?
