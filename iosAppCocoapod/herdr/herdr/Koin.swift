//
//  Koin.swift
//  herdr
//
//  Created by Fabrice on 2020-08-06.
//  Copyright © 2020 Ludosicty. All rights reserved.
//

import Foundation
import MultiPlatformLibrary

func startKoin() {
    // You could just as easily define all these dependencies in Kotlin, but this helps demonstrate how you might pass platform-specific dependencies in a larger scale project where declaring them in Kotlin is more difficult, or where they're also used in iOS-specific code.
    
    //let doOnStartup = { NSLog("Hello from iOS/Swift!") }
    
    let koinApplication = KoinIOSKt.doInitKoinIos(secureDataStore: SecureDataStoreImpl())
    _koin = koinApplication.koin
}

private var _koin: Koin_coreKoin? = nil
var koin: Koin_coreKoin {
    return _koin!
}
