@file:Suppress("unused")

package com.apboutos.moneytrack.view

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.apboutos.moneytrack.R
import com.apboutos.moneytrack.utilities.error.RegisterError
import com.apboutos.moneytrack.viewmodel.RegisterActivityViewModel
import com.apboutos.moneytrack.viewmodel.receiver.RegisterReceiver

class RegisterActivity : Activity() {

    private val viewModel by lazy { ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(RegisterActivityViewModel::class.java) }
    private val usernameBox by lazy { findViewById<EditText>(R.id.activity_register_username_box) }
    private val passwordBox by lazy { findViewById<EditText>(R.id.activity_register_password_box) }
    private val passwordRetypeBox by lazy { findViewById<EditText>(R.id.activity_register_password_re_box) }
    private val emailBox by lazy { findViewById<EditText>(R.id.activity_register_email_box) }
    private val receiver by lazy { RegisterReceiver(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signUpButton  = findViewById<Button>(R.id.activity_register_signUp_button)
        signUpButton.setOnClickListener {

            if(dataEnteredAreValid()) viewModel.registerNewUser(usernameBox.text.toString(),passwordBox.text.toString(),emailBox.text.toString())
        }
    }

    /**
     * The RegisterReceiver calls this method to handle the server's response to a register request.
     */
    fun handleResponse(error : RegisterError){

        when(error){
            RegisterError.NO_ERROR -> { Toast.makeText(applicationContext, "Registration completed.", Toast.LENGTH_SHORT).show()
                                        viewModel.addUserToDatabase(usernameBox.text.toString(),passwordBox.text.toString(),emailBox.text.toString())
                                        finish() }

            RegisterError.USERNAME_TAKEN -> { usernameBox.error = getString(R.string.activity_register_user_error) }

            RegisterError.EMAIL_TAKEN -> { emailBox.error = getString(R.string.activity_register_email_error) }

            RegisterError.NO_INTERNET -> { Toast.makeText(applicationContext, getString(R.string.activity_register_no_internet_error), Toast.LENGTH_LONG).show()}

            RegisterError.SERVER_UNREACHABLE -> { Toast.makeText(applicationContext, getString(R.string.activity_register_server_unreachable_error), Toast.LENGTH_LONG).show() }
        }
    }

    /**
     * Checks if the information provided by the user is in a valid format and produces the appropriate error boxes.
     */
    private fun dataEnteredAreValid(): Boolean {

        if (usernameBox.text.toString().isEmpty()) {usernameBox.error = "Enter a username."; return false}
        else usernameBox.error = null

        if (passwordBox.text.toString().isEmpty()) { passwordBox.error = "Enter a password."; return false}
        else passwordBox.error = null

        if (passwordRetypeBox.text.toString().isEmpty()) { passwordRetypeBox.error = "Retype the password you entered above."; return false }
        else passwordRetypeBox.error = null

        if (passwordBox.text.toString().length < 8) { passwordBox.error = "Password must be at least 8 characters long."; return false }
        else passwordRetypeBox.error = null

        if (passwordRetypeBox.text.toString().length < 8) { passwordBox.error = "Password must be at least 8 characters long."; return false }
        else passwordRetypeBox.error = null

        if (passwordBox.text.toString() != passwordRetypeBox!!.text.toString()) { passwordRetypeBox.error = "Passwords do not match."; return false }
        else passwordRetypeBox.error = null

        if (emailBox.text.toString().isEmpty()) { emailBox.error = "Enter an e-mail address."; return false }

        return true
    }

    /**
     * Registers the broadcast receiver.
     */
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addAction(RegisterReceiver.SERVER_REGISTER_RESPONSE)
        registerReceiver(receiver,filter)
    }

    /**
     * Unregisters the broadcast receiver to avoid leaks.
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}