//
//  StartViewController.swift
//  iosApp
//
//  Created by f8full on 2021-03-26.
//

import UIKit
import MultiPlatformLibrary
import MaterialComponents.MaterialCards
import MaterialComponents.MaterialCards_Theming
import MaterialComponents.MDCButton
import MaterialComponents.MDCButton_MaterialTheming
import MaterialComponents.MaterialShapeScheme

class StartViewController: UIViewController {
    
    @IBOutlet weak var cozySetupButton: MDCButton!
    @IBOutlet weak var cozySetupCard: MDCCard!
    private var startViewModel: StartViewModel!
    @objc var containerScheme: MDCContainerScheme
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        containerScheme = MDCContainerScheme()
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        containerScheme = MDCContainerScheme()
        
        let colorScheme = MDCSemanticColorScheme()
        
        colorScheme.primaryColor = UIColor(red: 0.18, green: 0.57, blue: 0.96, alpha: 1.00)
        colorScheme.onPrimaryColor = .white
        
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
        
        super.init(coder: aDecoder)
    }
    
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
        cozySetupButton.applyContainedTheme(withScheme: containerScheme)
        
        //the following doesn't work
        cozySetupButton.imageView?.image = #imageLiteral(resourceName: "add_white_18pt")
        //
        
        cozySetupCard.addTarget(self, action: #selector(didCozySetupButtonClick), for: .touchUpInside)
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
