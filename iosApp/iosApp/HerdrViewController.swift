/*
 *     Copyright (c) 2021. f8full https://github.com/f8full
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
import MultiPlatformLibrary

class HerdrViewController: UIViewController {
    @IBOutlet weak var stillImageView: UIImageView!
    @IBOutlet weak var walkImageView: UIImageView!
    @IBOutlet weak var runImageView: UIImageView!
    @IBOutlet weak var bikeImageView: UIImageView!
    @IBOutlet weak var vehicleImageView: UIImageView!
    @IBOutlet weak var dirName: UILabel!
    @IBOutlet weak var stackBase: UILabel!
    
    private var herdrViewModel: HerdrFragmentViewModel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "herdr-v2020-alpha0"
        
        self.navigationItem.leftBarButtonItem = nil
        self.navigationItem.hidesBackButton = true
        
        initViewModel()
        
        stillImageView.image = stillImageView.image?.withRenderingMode(.alwaysTemplate)
        stillImageView.tintColor = .systemYellow

        // Do any additional setup after loading the view.
    }
    
    func initViewModel() {
        herdrViewModel = HerdrFragmentViewModel(eventsDispatcher: EventsDispatcher())
        herdrViewModel.eventsDispatcher.listener = self
        observeHerdrViewModel()
    }
    
    func observeHerdrViewModel() {
        herdrViewModel.cloudDirectoryName.addObserver{dirName in
            self.dirName.text = dirName as String?
        }
        
        herdrViewModel.stackBaseUrlText.addObserver{stackBase in
            self.stackBase.text = stackBase as String?
        }
    }
}

extension HerdrViewController: HerdrFragmentViewModelHerdrFragmentEventListener {
    func routeToDriveSettings(folderId: String) {
        
    }
}
