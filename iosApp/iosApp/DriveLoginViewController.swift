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
import MaterialComponents.MaterialTextFields

class ViewController: UIViewController {
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var label: UILabel!

    var textController: MDCTextInputControllerOutlined!

    private var loginViewModel: DriveLoginViewModel!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "DriveLogin"
        //label.text = Proxy().proxyHello()
        configView()
        initViewModel()
        
        
        ////////////
        let outlinedTextField = MDCTextField(frame: CGRect(x:0, y:200, width: self.view.frame.width - 50, height: 50))
        outlinedTextField.placeholder = "Username Or Custom Domain"
        //outlinedTextField.leadingAssistiveLabel.text = "This is helper text"
        outlinedTextField.sizeToFit()
        
        
        self.view.addSubview(outlinedTextField)
        
        self.textController = MDCTextInputControllerOutlined(textInput: outlinedTextField)
        
        self.textController.textInsets(UIEdgeInsets(top: 16, left:16, bottom:16, right:16))
    }
    
    func configView() {
        loginButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
    }
    
    func initViewModel() {
        loginViewModel = DriveLoginViewModel(eventsDispatcher: EventsDispatcher())
        loginViewModel.eventsDispatcher.listener = self
        observeLoginViewModel()
    }
    
    // OBSERVER
    func observeLoginViewModel() {
        loginViewModel.authClientRegistrationResult.addObserver{ registrationState in
            //TODO: reflect model state in UI
            
            //if(registrationState is SuccessAuthClientRegistration) {
            //    let successState = registrationState as! SuccessAuthClientRegistration
            //    let response = (successState.response as! ResponseSuccess)
            //    self.onClientRegistrationSuccess(authClientRegistration: response.data!)
            //}
        }
        
        loginViewModel.userCredentialsResult.addObserver{ userCredentialsState in
            //TODO: reflect model state in UI
            
            //if(userCredentialsState is SuccessUserCredentials) {
            //    let successState = userCredentialsState as! SuccessUserCredentials
            //    let response = (successState.response as! ResponseSuccess)
            //    self.onUserCredentialsSuccess(userCredentials: response.data as! UserCredentials)
            //}
        }
    }
    
    private func launchAuthorizationFlow(authClientRegistration: AuthClientRegistration) {
        
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            //self.logMessage("Error accessing AppDelegate")
            return
        }
        
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
                self.loginViewModel.exchangeCodeForAccessAndRefreshToken(authCode: response.authorizationCode ?? "")
            } else {
                //self.logMessage("Authorization error: \(error?.localizedDescription ?? "DEFAULT_ERROR")")
            }
        }
    }
    
    @objc func didButtonClick(_ sender: UIButton) {
        loginViewModel.urlChanged(newInput: "https://cozy.ludos.city")
        loginViewModel.registerAuthClient()
    }

    deinit {
        loginViewModel.onCleared()
    }
}

extension ViewController: DriveLoginViewModelDriveLoginFragmentEventListener {
    func routeToAuthFlow() {
        self.label.text = "registered"
        
        let clientRegistration = ((loginViewModel.authClientRegistrationResult.value as! SuccessAuthClientRegistration)
            .response as! ResponseSuccess)
            .data!

        self.launchAuthorizationFlow(authClientRegistration: clientRegistration)
    }
    
    func routeToCreateAccount() {
        //FIXME: account creation can only happen during setup
    }
    
    func routeToHerdr() {
        
        let userCredentials = ((loginViewModel.userCredentialsResult.value as! SuccessUserCredentials)
            .response as! ResponseSuccess)
            .data!
        
        self.label.text = userCredentials.accessToken
        
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        self.navigationController?.pushViewController(storyboard.instantiateViewController(withIdentifier: "herdr") as UIViewController, animated: false)
    }
}
