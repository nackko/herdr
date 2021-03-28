//
//  StartViewController.swift
//  iosApp
//
//  Created by f. on 2021-03-26.
//

import UIKit
import MultiPlatformLibrary

class StartViewController: UIViewController {
    
    @IBOutlet weak var cozySetupButton: UIButton!
    private var startViewModel: StartViewModel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        title = "StartScreen"
        
        initViewModel()
        configView()
    }
    
    func initViewModel() {
        startViewModel = StartViewModel(eventsDispatcher: EventsDispatcher())
        startViewModel.eventsDispatcher.listener = self
    }
    
    func configView() {
        cozySetupButton.addTarget(self, action: #selector(didCozySetupButtonClick), for: .touchUpInside)
    }
    
    @objc func didCozySetupButtonClick(_ sender: UIButton) {
        startViewModel.onSetupButtonPressed()
    }
    
    deinit {
        startViewModel.onCleared()
    }
}

extension StartViewController: StartViewModelStartFragmentEventListener {
    func routeToDriveSetup() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        self.navigationController?.pushViewController(storyboard.instantiateViewController(withIdentifier: "driveSetup") as UIViewController, animated: true)
    }
    
    func routeToHerdr() {
        let storyboard = UIStoryboard(name: "Main", bundle: nil)
        self.navigationController?.pushViewController(storyboard.instantiateViewController(withIdentifier: "herdr") as UIViewController, animated: false)
    }
}
