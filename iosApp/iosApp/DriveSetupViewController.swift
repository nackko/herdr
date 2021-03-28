//
//  DriveSetupViewController.swift
//  iosApp
//
//  Created by f. on 2021-03-27.
//

import UIKit
import MultiPlatformLibrary

class DriveSetupViewController: UIViewController {
    @IBOutlet weak var loginButton: UIButton!
    
    @IBOutlet weak var createAccountButton: UIButton!
    private var driveSetupViewModel: DriveSetupViewModel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "DriveSetup"

        initViewModel()
        configView()
    }
    
    func initViewModel() {
        driveSetupViewModel = DriveSetupViewModel(eventsDispatcher: EventsDispatcher())
        driveSetupViewModel.eventsDispatcher.listener = self
    }
    
    func configView() {
        loginButton.addTarget(self, action: #selector(onLoginButtonPressed), for: .touchUpInside)
        createAccountButton.addTarget(self, action: #selector(onCreateAccountButtonPressed), for: .touchUpInside)
    }
    
    @objc func onLoginButtonPressed(_ sender:UIButton) {
        driveSetupViewModel.onLoginButtonPressed()
    }
    
    @objc func onCreateAccountButtonPressed(_ sender:UIButton) {
        driveSetupViewModel.onCreateAccountButtonPressed()
    }
    
    deinit {
        driveSetupViewModel.onCleared()
    }
}

extension DriveSetupViewController: DriveSetupViewModelDriveSetupFragmentEventListener {
    func routeToCreateAccount() {
        //TODO: open URL
    }
    
    func routeToDriveLogin() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        self.navigationController?.pushViewController(storyboard.instantiateViewController(withIdentifier: "driveLogin") as UIViewController, animated: true)
    }
}
