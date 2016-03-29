# AppRemoteSettings: Android + Java Client

Update variables in your production Android apps, live. Skip the Play Store review process.

For use with the [AppRemoteSettings server](https://github.com/mplewis/AppRemoteSettings).

# Try It Out

Clone this repo and open the Android Studio project. Run the app on an Android device or in the simulator, then check Logcat to see how sharedPreferences gets updated from the server.

[MainActivity.java](app/src/main/java/com/kesdev/appremotesettingsexample/MainActivity.java) updates the app's settings on launch from the AppRemoteSettings server.

# Installation

Add [AppRemoteSettingsClient.java](app/src/main/java/com/kesdev/appremotesettingsexample/AppRemoteSettingsClient.java) to your project.

There is no Maven Central package at this time.

# Usage

1. Get a copy of your SharedPreferences object to use throughout your app with [`this.getSharedPreferences(...)`](app/src/main/java/com/kesdev/appremotesettingsexample/MainActivity.java#L18-L26)
2. Call [`AppRemoteSettingsClient.updatePreferencesWithAppRemoteSettings(...)`](app/src/main/java/com/kesdev/appremotesettingsexample/MainActivity.java#L58-L70) in your main Activity's OnCreate method
3. Replace your hardcoded variables with values from [`prefs.getType(...)`](app/src/main/java/com/kesdev/appremotesettingsexample/MainActivity.java#L83-L93), adding default values for offline development
4. Change values in the AppRemoteSettings Dashboard to update them in your production apps.

# Contributions

Bug reports, fixes, or features? Feel free to open an issue or pull request any time. You can also email me at [matt@mplewis.com](mailto:matt@mplewis.com).

# License

MIT
