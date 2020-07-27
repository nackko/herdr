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
import app

class ViewController: UIViewController {
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var label: UILabel!
    @IBOutlet weak var logoutButton: UIButton!
    
    private var loginViewModel: DriveLoginViewModel!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //label.text = Proxy().proxyHello()
        configView()
        initViewModel()
    }
    
    func configView() {
        loginButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
        logoutButton.addTarget(self, action: #selector(didLogoutButtonClick), for: .touchUpInside)
    }
    
    func initViewModel() {
        loginViewModel = DriveLoginViewModel()
        observeLoginViewModel()
    }
    
    // OBSERVER
    func observeLoginViewModel() {
        loginViewModel.authClientRegistrationResult.addObserver{ registrationState in
            if(registrationState is SuccessAuthClientRegistration) {
                let successState = registrationState as! SuccessAuthClientRegistration
                let response = (successState.response as! Response.Success)
                self.onClientRegistrationSuccess(authClientRegistration: response.data as! AuthClientRegistration)
            } else if(registrationState is InProgressAuthClientRegistration) {
                self.label.text = "InProgressAuthClientRegistration"
            } else {
                self.label.text = "ErrorAuthClientRegistration"
            }
        }
        
        loginViewModel.userCredentialsResult.addObserver{ userCredentialsState in
            if(userCredentialsState is SuccessUserCredentials) {
                let successState = userCredentialsState as! SuccessUserCredentials
                let response = (successState.response as! Response.Success)
                self.onUserCredentialsSuccess(userCredentials: response.data as! UserCredentials)
            }
        }
        
        loginViewModel.requestAuthFlowEvent.addObserver{ flowRequested in
            if(flowRequested != nil && flowRequested is Bool && flowRequested as! Bool == true ) {
                let authInfo = ((self.loginViewModel.authClientRegistrationResult.value as! SuccessAuthClientRegistration)
                    .response as! Response.Success)
                    .data as! AuthClientRegistration
                
                self.launchAuthorizationFlow(authInfo: authInfo)
                self.loginViewModel.authFlowRequestProcessed()
            }
        }
    }
    
    func launchAuthorizationFlow(authInfo: AuthClientRegistration) {
        
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            //self.logMessage("Error accessing AppDelegate")
            return
        }
        
        let configuration = OIDServiceConfiguration(authorizationEndpoint: URL(string: authInfo.stackBaseUrl + "/auth/authorize")!,
                                                    tokenEndpoint: URL(string: authInfo.stackBaseUrl + "/auth/authorize")!)
        let authorizationRequest = OIDAuthorizationRequest(configuration: configuration,
                                                            clientId: authInfo.clientId,
                                                            clientSecret: authInfo.clientSecret,
                                                            scopes: [OIDScopeOpenID, "io.cozy.files", "io.cozy.oauth.clients"],
                                                            redirectURL: URL(string: authInfo.redirectUriCollection[0])!,
                                                            responseType: OIDResponseTypeCode,
                                                            additionalParameters: nil)
        // performs authentication request
        //logMessage("Initiating authorization request with scope: \(request.scope ?? "DEFAULT_SCOPE")")

        appDelegate.currentAuthorizationFlow = OIDAuthorizationService.present(authorizationRequest, presenting: self) { (response, error) in

            if let response = response {
                self.label.text = "CODE: " + (response.authorizationCode ?? "DEFAULT_CODE")
                self.loginViewModel.exchangeCodeForAccessAndRefreshToken(authCode: response.authorizationCode ?? "")
                // could just call [self tokenExchange:nil] directly, but will let the user initiate it.
            } else {
                //self.logMessage("Authorization error: \(error?.localizedDescription ?? "DEFAULT_ERROR")")
            }
        }
    }
    
    @objc func didButtonClick(_ sender: UIButton) {
        loginViewModel.registerAuthClient()
    }
    
    @objc func didLogoutButtonClick(_ sender: UIButton) {
        loginViewModel.unregisterAuthClient()
    }

    deinit {
        loginViewModel.onCleared()
    }
}
