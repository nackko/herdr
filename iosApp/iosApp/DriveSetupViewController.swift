//
//  DriveSetupViewController.swift
//  iosApp
//
//  Created by f. on 2021-03-27.
//

import UIKit
import MultiPlatformLibrary
import MaterialComponents.MDCButton
import MaterialComponents.MDCButton_MaterialTheming

class DriveSetupViewController: UIViewController {
    @IBOutlet weak var loginButton: MDCButton!
    @IBOutlet weak var createAccountButton: MDCButton!
    private var driveSetupViewModel: DriveSetupViewModel!
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
        
        title = "DriveSetup"

        initViewModel()
        configView()
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
    
    func initViewModel() {
        driveSetupViewModel = DriveSetupViewModel(eventsDispatcher: EventsDispatcher())
        driveSetupViewModel.eventsDispatcher.listener = self
    }
    
    func configView() {
        loginButton.addTarget(self, action: #selector(onLoginButtonPressed), for: .touchUpInside)
        
        loginButton.applyContainedTheme(withScheme: containerScheme)
        
        createAccountButton.addTarget(self, action: #selector(onCreateAccountButtonPressed), for: .touchUpInside)
        
        createAccountButton.applyOutlinedTheme(withScheme: containerScheme)
        
        createAccountButton.setBorderWidth(2, for: .normal)
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
