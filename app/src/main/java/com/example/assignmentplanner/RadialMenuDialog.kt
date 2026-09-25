package com.example.assignmentplanner

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window

class RadialMenuDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_radial_menu)
        
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        findViewById<View>(R.id.radial_root).setOnClickListener {
            dismiss()
        }

        findViewById<View>(R.id.fab_close).setOnClickListener {
            dismiss()
        }

        findViewById<View>(R.id.btn_action_modules).setOnClickListener {
            context.startActivity(Intent(context, ModulesActivity::class.java))
            dismiss()
        }

        findViewById<View>(R.id.btn_action_add_module).setOnClickListener {
            // Reusing Module Details for module addition flow 
            context.startActivity(Intent(context, ModuleDetailsActivity::class.java))
            dismiss()
        }

        findViewById<View>(R.id.btn_action_add_assignment).setOnClickListener {
            context.startActivity(Intent(context, AddAssignmentActivity::class.java))
            dismiss()
        }
    }
}
