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
import MultiPlatformLibrary

class ViewController: UIViewController {
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var label: UILabel!
    
    private var loginViewModel: DriveLoginViewModel!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //label.text = Proxy().proxyHello()
        configView()
        initViewModel()
    }
    
    func configView() {
        loginButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
    }
    
    func initViewModel() {
        loginViewModel = DriveLoginViewModel(eventsDispatcher: EventsDispatcher())
        observeLoginViewModel()
    }
    
    // OBSERVER
    func observeLoginViewModel() {
        loginViewModel.authClientRegistrationResult.addObserver{ registrationState in
            if(registrationState is SuccessAuthClientRegistration) {
                let successState = registrationState as! SuccessAuthClientRegistration
                let response = (successState.response as! ResponseSuccess)
                self.onClientRegistrationSuccess(authClientRegistration: response.data as! AuthClientRegistration)
            }
        }
        
        loginViewModel.userCredentialsResult.addObserver{ userCredentialsState in
            if(userCredentialsState is SuccessUserCredentials) {
                let successState = userCredentialsState as! SuccessUserCredentials
                let response = (successState.response as! ResponseSuccess)
                self.onUserCredentialsSuccess(userCredentials: response.data as! UserCredentials)
            }
        }
    }
    
    func onUserCredentialsSuccess(userCredentials: UserCredentials) {
        self.label.text = userCredentials.accessToken
    }
    
    func onClientRegistrationSuccess(authClientRegistration: AuthClientRegistration) {
        
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            //self.logMessage("Error accessing AppDelegate")
            return
        }
        
        self.label.text = authClientRegistration.clientRegistrationToken
        
        let configuration = OIDServiceConfiguration(authorizationEndpoint: URL(string: authClientRegistration.stackBaseUrl + "/auth/authorize")!,
                                                    tokenEndpoint: URL(string: authClientRegistration.stackBaseUrl + "/auth/authorize")!)
        let authorizationRequest = OIDAuthorizationRequest(configuration: configuration,
                                                            clientId: authClientRegistration.clientId,
                                                            clientSecret: authClientRegistration.clientSecret,
                                                            scopes: [OIDScopeOpenID, "io.cozy.files", "io.cozy.oauth.clients"],
                                                            redirectURL: URL(string: authClientRegistration.redirectUriCollection[0])!,
                                                            responseType: OIDResponseTypeCode,
                                                            additionalParameters: nil)
        // performs authentication request
        //logMessage("Initiating authorization request with scope: \(request.scope ?? "DEFAULT_SCOPE")")

        appDelegate.currentAuthorizationFlow = OIDAuthorizationService.present(authorizationRequest, presenting: self) { (response, error) in

            if let response = response {
                //let authState = OIDAuthState(authorizationResponse: response)
                self.label.text = "CODE: " + (response.authorizationCode ?? "DEFAULT_CODE")
                self.loginViewModel.exchangeCodeForAccessAndRefreshToken(authCode: response.authorizationCode ?? "")
                //self.setAuthState(authState)
                //self.logMessage("Authorization response with code: \(response.authorizationCode ?? "DEFAULT_CODE")")
                // could just call [self tokenExchange:nil] directly, but will let the user initiate it.
            } else {
                //self.logMessage("Authorization error: \(error?.localizedDescription ?? "DEFAULT_ERROR")")
            }
        }
    }
    
    @objc func didButtonClick(_ sender: UIButton) {
        loginViewModel.registerAuthClient()
    }

    deinit {
        loginViewModel.onCleared()
    }
}
