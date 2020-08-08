//
//  AppDelegate.swift
//  herdr
//
//  Created by Fabrice on 2020-07-26.
//  Copyright © 2020 Ludosicty. All rights reserved.
//

import UIKit
import AppAuth
import MultiPlatformLibrary

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    var currentAuthorizationFlow: OIDExternalUserAgentSession?
    
    // Lazy so it doesn't try to initialize before startKoin() is called
    lazy var log = koin.get(objCClass: Kermit.self, parameter: "AppDelegate") as! Kermit
    
    func application(
        _ app: UIApplication,
        open url: URL,
        didFinishLaunchingWithOptions: [UIApplication.OpenURLOptionsKey : Any] = [:]
    ) -> Bool {
        if let authorizationFlow = self.currentAuthorizationFlow, authorizationFlow.resumeExternalUserAgentFlow(with: url) {
            self.currentAuthorizationFlow = nil
            
            startKoin()
            
            // Manually launch storyboard so that ViewController doesn't initialize before Koin
            //let storyboard = UIStoryboard(name: "Main", bundle: nil)
            //let viewController = storyboard.instantiateViewController(identifier: "BreedsViewController")
            
            //self.window = UIWindow(frame: UIScreen.main.bounds)
            //self.window?.rootViewController = viewController
            //sself.window?.makeKeyAndVisible()
            
            log.v(withMessage: {"App Started"})
            
            return true
        }

        return false
    }

    func applicationWillResignActive(_ application: UIApplication) {}

    func applicationDidEnterBackground(_ application: UIApplication) {}

    func applicationWillEnterForeground(_ application: UIApplication) {}

    func applicationDidBecomeActive(_ application: UIApplication) {}

    func applicationWillTerminate(_ application: UIApplication) {}
}
