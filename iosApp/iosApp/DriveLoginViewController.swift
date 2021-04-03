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

class ViewController: UIViewController, UITextFieldDelegate {
    @IBOutlet weak var loginButton: MDCButton!
    @IBOutlet weak var finalUrl: UILabel!
    @IBOutlet weak var usernameOrDomain: MDCTextField!
    
    var textController: MDCTextInputControllerOutlined!

    private var loginViewModel: DriveLoginViewModel!
    @objc var containerScheme: MDCContainerScheme
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        containerScheme = MDCContainerScheme()
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        initContainerScheme()
    }
    
    required init?(coder aDecoder: NSCoder) {
        containerScheme = MDCContainerScheme()
        super.init(coder: aDecoder)
        initContainerScheme()
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "DriveLogin"
        configView()
        initViewModel()
        
        self.textController = MDCTextInputControllerOutlined(textInput: usernameOrDomain)
        self.textController.textInsets(UIEdgeInsets(top: 16, left:16, bottom:16, right:16))
        
        loginViewModel.finalUrl.addObserver { (newUrl: NSString?) in
            self.finalUrl.text = newUrl as String?
        }
    }
    
    func initContainerScheme() {
        
        let colorScheme = MDCSemanticColorScheme()
        
        colorScheme.primaryColor = UIColor(red: 0.18, green: 0.57, blue: 0.96, alpha: 1.00)
        colorScheme.onPrimaryColor = .white
        
        colorScheme.onSurfaceColor = .darkGray
        
        containerScheme.colorScheme = colorScheme
        
        let shapeScheme = MDCShapeScheme()
        
        let smallShapeCategory = MDCShapeCategory()
        
        let rounded24dpCorner = MDCCornerTreatment.corner(withRadius: 20, valueType: .absolute)
        
        smallShapeCategory?.topLeftCorner = rounded24dpCorner
        smallShapeCategory?.bottomLeftCorner = rounded24dpCorner
        smallShapeCategory?.topRightCorner = rounded24dpCorner
        smallShapeCategory?.bottomRightCorner = rounded24dpCorner
        
        shapeScheme.smallComponentShape = smallShapeCategory!
        
        containerScheme.shapeScheme = shapeScheme
    }
    
    func configView() {
        loginButton.addTarget(self, action: #selector(didButtonClick), for: .touchUpInside)
        loginButton.applyContainedTheme(withScheme: containerScheme)
        
        usernameOrDomain.addTarget(self, action: #selector(textFieldDidChange(_:)), for: .editingChanged)
        usernameOrDomain.delegate = self
        usernameOrDomain.placeholder = "Username Or Custom Domain"
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        usernameOrDomain.resignFirstResponder()
        return true
    }
    
    @objc func textFieldDidChange(_ textField: UITextField) {
        loginViewModel.urlChanged(newInput: textField.text!)
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
        loginViewModel.registerAuthClient()
    }

    deinit {
        loginViewModel.onCleared()
    }
}

extension ViewController: DriveLoginViewModelDriveLoginFragmentEventListener {
    func routeToAuthFlow() {
        let clientRegistration = ((loginViewModel.authClientRegistrationResult.value as! SuccessAuthClientRegistration)
            .response as! ResponseSuccess)
            .data!

        self.launchAuthorizationFlow(authClientRegistration: clientRegistration)
    }
    
    func routeToCreateAccount() {
        //FIXME: account creation can only happen during setup
    }
    
    func routeToHerdr() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        self.navigationController?.pushViewController(storyboard.instantiateViewController(withIdentifier: "herdr") as UIViewController, animated: false)
    }
}
