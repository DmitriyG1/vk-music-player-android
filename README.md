# vk-music-player-android
VKontakte (vk.com) music player for Android. Project uses VKontakte SDK and AdMob mobile advertising platform.

App on Google Play: [InTheMusic](https://play.google.com/store/apps/details?id=com.blogspot.colibriapps.inthemusic)

### VKontakte SDK
You need to add and initialize VK Android SDK with version 1.5. GitHub: [vk-android-sdk](https://github.com/VKCOM/vk-android-sdk)

Add this to the resource file, vk_scheme = vk+APP_ID
```
  <string name="scheme">vk_scheme</string>

```

### AdMob Advertising Initialization
Add this to the resource file
```
  <string name="banner_ad_unit_id">admob_banner_ad_unit_id</string>
```
