<h1 align="center">Scrolless</h1>
<h3 align="center">Anti Brain Rot App</h3>

<p align="center"><img src="art/app_logo.png" alt="Scrolless" height="256"></p>
<p align="center"> 
  <br/>  
  <a href="https://opensource.org/license/gpl-3"><img src="https://img.shields.io/badge/License-GPL_3.0-blue.svg?color=3F51B5&style=for-the-badge&label=License&logoColor=000000&labelColor=ececec" alt="License: AGPLv3"></a>
  <br/>
  <a href="https://play.google.com/store/apps/details?id=com.scrolless.app"><img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="70"></a>
  <br/>  
  <br/>   
</p>

**Scrolless** is an open-source Android application designed to help users avoid excessive consumption of brain rot by blocking distracting content like Instagram Reels, TikTok, and YouTube Shorts. 

Using Android's accessibility permissions, the app detects and blocks this type of content whenever it appears on the screen.
Since the app requires accessibility permissions, which can have sketchy uses, Scrolless is fully open-source.


## Emitter fork: Instagram without the feed

Source: https://github.com/Emitter-166/Scrolless (fork of DuarteBarbosaDev/Scrolless).

Select **Block All**, enable the accessibility service, and keep blocking unpaused. This fork covers recognized Instagram **Home and Explore recommendation areas** with an opaque, touch-consuming message, leaving the header (messages/search) and bottom tabs available. DMs, profiles and profile posts remain accessible; full-screen Reels still trigger Back. The cover uses the existing blocking mode, so Daily Limit, Interval Timer and Pause can allow feeds temporarily. DM-video exceptions do not exempt Home/Explore. Enable **Include Stories** to block full-screen Stories as well.

This is an accessibility overlay, not a modification of Instagram: content may still download/play behind the cover, and a frame may appear before detection. It targets the standard `com.instagram.android` app, not Instagram Lite, clones or the website. Detection relies on known resource IDs plus selected navigation tabs and avoids generic scrolling-list matching to protect messages/profiles. Unknown Instagram layouts may not match. Regression tests use synthetic layouts; no claim of device/version compatibility is made without testing. If a feed slips through, provide the Instagram version and a redacted accessibility layout captured only on a feed screen (never share private chats).

The APK retains local usage history (app category, duration and timestamps). Android system backup remains enabled, so history may be copied into system-managed backups or device transfers. No Internet permission or telemetry was added.

## Features

- **Block All**  
  Instantly block access to all supported platforms, including Instagram Reels, TikTok, YouTube Shorts, and Facebook Reels.

- **Daily Limit**  
  Set a daily limit for how long you can spend on supported platforms. Once the limit is reached, access is blocked for the rest of the day.

- **Live Brain Rot Timer**  
  A real-time overlay timer tracks your session while watching Shorts or Reels, keeping you aware of your screen time.

- **Pause**   
  Pause the blocking feature for 5 minutes, allowing temporary access to the platforms.

- **Interval Timer**  
  Set intervals for usage and breaks, allowing controlled social media access throughout the day.

- **Usage Tracking**   
  Keep track of how much time you’ve spent on Instagram Reels, TikTok, YouTube Shorts, and Facebook Reels.

## Screenshots

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/01-less-scrolling.png" alt="Less scrolling, more living" width="200">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/02-choose-your-cap.png" alt="Choose your daily limit" width="200">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/03-set-your-rhythm.png" alt="Set an interval timer" width="200">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/04-keep-the-chats.png" alt="Keep videos sent in DMs" width="200">
</p>
<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/05-timeline-averages.png" alt="Daily timeline and weekday averages" width="200">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/06-usage-history.png" alt="Usage history" width="200">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/07-built-on-trust.png" alt="Open source, no ads, no tracking" width="200">
</p>

## Troubleshooting

**Scrolless stops working or Android says the service is "not working"**

Some phones stop apps running in the background when you clear them from recent apps. This takes the accessibility service down with them. Scrolless shows a warning when this happens. To fix it:

1. Open Accessibility settings, then turn Scrolless off and on again.
2. Tap **Allow background activity** in Scrolless and confirm the system popup. On some phones you also need to allow background activity in the phone's own settings:
   - **Xiaomi / Redmi / POCO (MIUI, HyperOS):** turn on **Autostart** and set Battery saver to **No restrictions**.
   - **Huawei / Honor:** in App launch, set Scrolless to **Manage manually** with Auto-launch and Run in background turned on.
   - **Oppo / Realme / OnePlus:** turn on **Allow auto launch** and **Allow background activity**.
   - **Vivo / iQOO:** turn on **Auto-start** and **Allow high background power consumption**.
   - **Samsung:** set battery usage to **Unrestricted** and remove Scrolless from **Sleeping apps**.

More device-specific tips are at [dontkillmyapp.com](https://dontkillmyapp.com).

# Architecture

Scrolless app architecture is inspired by the Google open source project [Jetcaster](https://github.com/android/compose-samples/tree/main/Jetcaster), published under the [Apache License](https://github.com/android/compose-samples/blob/main/LICENSE).

Some icons used in this app are obtained from [Icons8](https://icons8.com).

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=Emitter-166/Scrolless&type=Date)](https://star-history.com/#Emitter-166/Scrolless&Date)
