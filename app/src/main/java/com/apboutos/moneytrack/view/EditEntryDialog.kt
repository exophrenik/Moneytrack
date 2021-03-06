@file:Suppress("unused")

package com.apboutos.moneytrack.view

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.apboutos.moneytrack.R
import com.apboutos.moneytrack.model.database.entity.Entry
import com.apboutos.moneytrack.utilities.Time
import com.apboutos.moneytrack.utilities.converter.CurrencyConverter

class EditEntryDialog(private val parentActivity : LedgerActivity,private val entry : Entry,private val position : Int) : Dialog(parentActivity) {

    private val updateButton by lazy { findViewById<Button>(R.id.activity_ledger_edit_entry_dialog_updateButton) }
    private val cancelButton by lazy { findViewById<Button>(R.id.activity_ledger_edit_entry_dialog_cancelButton) }
    private val typeSpinner by lazy { findViewById<Spinner>(R.id.activity_ledger_edit_entry_dialog_typeSpinner) }
    private val descriptionBox by lazy { findViewById<EditText>(R.id.activity_ledger_edit_entry_dialog_descriptionBox) }
    private val categorySpinner by lazy { findViewById<Spinner>(R.id.activity_ledger_edit_entry_dialog_categorySpinner) }
    private val amountBox by lazy { findViewById<EditText>(R.id.activity_ledger_edit_entry_dialog_amountBox) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_ledger_edit_entry_dialog)
        setUpBoxes()

        updateButton.setOnClickListener{
            if (validateUserInputFormat()) {
                if (typeSpinner.selectedItem.toString() == "Income") {
                    entry.type = "Income"
                } else {
                    entry.type = "Expense"
                }
                entry.category = categorySpinner.selectedItem.toString()
                entry.description = descriptionBox.text.toString()
                entry.amount = CurrencyConverter.normalizeAmount(amountBox.text.toString().toDouble(),entry.type)
                entry.lastUpdate = Time.getTimestamp()
                entry.isDeleted = false
                parentActivity.viewModel.updateEntry(position,entry)
                parentActivity.adapter.notifyItemChanged(position)
                dismiss()
            }
        }

        cancelButton.setOnClickListener{
            dismiss()
        }
    }

    /**
     * Initializes the values of text fields and spinners in UI.
     */
    private fun setUpBoxes(){
        val typeAdapter = ArrayAdapter(context, R.layout.activity_ledger_dialog_spinner, arrayOf("Income", "Expense"))
        typeSpinner.adapter = typeAdapter
        typeSpinner.setSelection(typeAdapter.getPosition(entry.type))
        val categoryAdapter = ArrayAdapter(context, R.layout.activity_ledger_dialog_spinner, parentActivity.viewModel.getCategories())
        categorySpinner.adapter = categoryAdapter
        categorySpinner.setSelection(categoryAdapter.getPosition(entry.category))
        descriptionBox.setText(entry.description)
        amountBox.setText(entry.amount.toString())
    }

    /**
     * Checks whether the user has filled all the text fields or not.
     */
    private fun validateUserInputFormat(): Boolean {
        var flag = true
        if (descriptionBox.text.toString() == "") {
            descriptionBox.error = "Enter description"
            flag = false
        }
        if (amountBox.text.toString() == "") {
            amountBox.error = "Enter amount"
            flag = false
        }
        return flag
    }
}