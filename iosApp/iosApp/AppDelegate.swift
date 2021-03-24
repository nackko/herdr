/*
 *     Copyright (c) 2020. f8full https://github.com/f8full
 *     Herdr is a privacy conscious multiplatform mobile data collector
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import UIKit
import AppAuth

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    var currentAuthorizationFlow: OIDExternalUserAgentSession?

    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        if let authorizationFlow = self.currentAuthorizationFlow, authorizationFlow.resumeExternalUserAgentFlow(with: url) {
            self.currentAuthorizationFlow = nil
            
            //FIXME
            // touchlab kampkit does it that way but I couldn't figure it out
            //startKoin()
                        
            // Manually launch storyboard so that ViewController doesn't initialize before Koin
            //        let storyboard = UIStoryboard(name: "Main", bundle: nil)
            //if #available(iOS 13.0, *) {
            //    let viewController = storyboard.instantiateViewController(identifier: "ViewController")
            //    self.window = UIWindow(frame: UIScreen.main.bounds)
            //    self.window?.rootViewController = viewController
            //    self.window?.makeKeyAndVisible()
            //} else {
                // Fallback on earlier versions
            //}
            
            //see https://github.com/touchlab/KaMPKit/blob/34c0d65cd776aa69d3ab5f24ce4492ab0a41b8a9/ios/KaMPKitiOS/AppDelegate.swift#L23
            
            
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
