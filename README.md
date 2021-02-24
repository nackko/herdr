# herdr
Herdr is a privacy conscious multiplatform mobile data collector

## Run
### Android
`./gradlew installDebug`
or just open IDE and click Run

### iOS
[CocoaPods](https://cocoapods.org/) required.

```
(cd iosApp && pod install)
open iosApp/iosApp.xcworkspace
```
or manually:
- do `pod install` in `iosApp` directory.
- open `iosApp.xcworkspace` and click Run. 

#### Description

[WIP]. To see screenshots of the app go to this [PR #5](https://github.com/f8full/herdr/pull/25)

Herdr is an activity tracker (walk, run, bike, in vehicle) allowing you to automatically record your personal geolocation data in a cloud service. You can set it to activate geolocation recording only when certain activities are detected.

For now, [Cozy Cloud](https://cozy.io) is the only supported cloud storage service. 
