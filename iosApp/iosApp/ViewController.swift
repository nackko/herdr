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
import app

class ViewController: UIViewController {
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var label: UILabel!
    
    private var loginViewModel: LoginViewModel!
    
    
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
        loginViewModel = LoginViewModel()
        observeLoginViewModel()
    }
    
    // OBSERVER
    func observeLoginViewModel() {
        loginViewModel.authClientRegistrationResult.addObserver{ registrationState in
            if(registrationState is SuccessAuthClientRegistration) {
                let successState = registrationState as! SuccessAuthClientRegistration
                let response = (successState.response as! Response.Success)
                self.label.text = (response.data as! AuthClientRegistration).clientRegistrationToken
            }
        }
    }
    
    @objc func didButtonClick(_ sender: UIButton) {
        loginViewModel.registerAuthClient(stackBaseUrl: "https://f8full.mycozy.cloud")
    }

    deinit {
        loginViewModel.onCleared()
    }
}
