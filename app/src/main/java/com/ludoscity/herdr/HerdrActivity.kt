/*
 *     Copyright (c) 2020. f8full https://github.com/f8full
 *     Herdr is a privacy conscious multiplatform mobile data collector
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ludoscity.herdr

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.ui.login.*
import kotlinx.android.synthetic.main.activity_herdr.*
import sample.hello

class HerdrActivity : AppCompatActivity() {

    lateinit var loginViewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_herdr)

        //bind views
        activity_herdr_tv.text = hello()

        activity_herdr_button.setOnClickListener {
            loginViewModel.registerAuthClient(hello())
        }

        //init viewmodel
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        //register observer
        loginViewModel.authClientRegistrationResult.addObserver { getClientRegistrationState(it) }
    }

    private fun getClientRegistrationState(state: AuthClientRegistrationState) {
        when (state) {
            is SuccessAuthClientRegistration -> {
                //TODO: hide in progress
                val response = state.response as Response.Success
                onClientRegistrationSuccess(registrationInfo = response.data)
            }
            is InProgressAuthClientRegistration -> {
                //TODO: show in progress
            }
            is ErrorAuthClientRegistration -> {
                //TODO: hide loading
                val response = state.response as Response.Error
                showError(response.message)
            }
        }
    }

    private fun onClientRegistrationSuccess(registrationInfo: AuthClientRegistration) {

        activity_herdr_tv.text = "Registration = $registrationInfo"
    }

    private fun showError(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}