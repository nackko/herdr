//
//  SecureDataStore.swift
//  iosApp
//
//  Created by Fabrice on 2020-04-16.
//

import Foundation
import app

class SecureDataStoreImpl: SecureDataStore  {
    override func getString(key: String, callback: KotlinContinuation) {
        callback.resumeWith(result: "MyString from xCode")
    }
    
    override func putString(key: String, data: String, callback: KotlinContinuation) {
        callback.resumeWith(result: Void())
    }
}