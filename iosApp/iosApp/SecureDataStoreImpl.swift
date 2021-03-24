//
//  SecureDataStore.swift
//  iosApp
//
//  Created by Fabrice on 2020-04-16.
//

import Foundation
import MultiPlatformLibrary
import SecureDefaults

class SecureDataStoreImpl: SecureDataStore  {
    var secureDefaults: SecureDefaults
        override init() {
            secureDefaults = SecureDefaults.shared
            if !secureDefaults.isKeyCreated {
                secureDefaults.password = UUID().uuidString
            }
        }
    
    override func getString(key: String, callback: KotlinContinuation) {
        callback.resumeWith(result: secureDefaults.string(forKey: key))
    }
    
    override func putString(key: String, data: String?, callback: KotlinContinuation) {
        secureDefaults.set(data, forKey: key)
        secureDefaults.synchronize()
        callback.resumeWith(result: Void())
    }
    
    override func deleteKey(key: String, callback: KotlinContinuation) {
        putString(key: key, data: nil, callback: callback)
    }
}
