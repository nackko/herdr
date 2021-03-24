//
//  Koin.swift
//  iosApp
//
//  Created by f. on 2021-03-21.
//

import Foundation

import MultiPlatformLibrary

func startKoin() {
    // You could just as easily define all these dependencies in Kotlin, but this helps demonstrate how you might pass platform-specific dependencies in a larger scale project where declaring them in Kotlin is more difficult, or where they're also used in iOS-specific code.
    
    //let userDefaults = UserDefaults(suiteName: "KAMPSTARTER_SETTINGS")!
    //let iosAppInfo = IosAppInfo()
    //let doOnStartup = { NSLog("Hello from iOS/Swift!") }
    let secureDataStore = SecureDataStoreImpl()
    
    //let koinApplication = KoinIOSKt.doInitKoinIos(userDefaults: userDefaults, appInfo: iosAppInfo, doOnStartup: doOnStartup)
    let koinApplication = KoinIOSKt.doInitKoinIos(dataStore: secureDataStore)
    _koin = koinApplication.koin
}

private var _koin: Koin_coreKoin? = nil
var koin: Koin_coreKoin {
    return _koin!
}

//class IosAppInfo : AppInfo {
//    let appId: String = Bundle.main.bundleIdentifier!
//}
