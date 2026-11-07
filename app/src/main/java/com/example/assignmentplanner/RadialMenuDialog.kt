package com.example.assignmentplanner

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window

class RadialMenuDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_radial_menu)

        // Configure full-width transparent window at the bottom
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }

        setupButtons()
    }

    private fun setupButtons() {
        val radialRoot = findViewById<View>(R.id.radial_root)
        val btnClose = findViewById<View>(R.id.fab_close)
        val btnModules = findViewById<View>(R.id.btn_action_modules)
        val btnAddModule = findViewById<View>(R.id.btn_action_add_module)
        val btnAddAssignment = findViewById<View>(R.id.btn_action_add_assignment)

        radialRoot?.setOnClickListener {
            dismiss()
        }

        btnClose?.setOnClickListener {
            dismiss()
        }

        // 1. Modules List Button
        btnModules?.setOnClickListener { view ->
            safeLaunchActivity(view.context) {
                Intent(it, ModulesActivity::class.java)
            }
        }

        // 2. Add Module Button (Routes to ModulesActivity with dialog trigger)
        btnAddModule?.setOnClickListener { view ->
            safeLaunchActivity(view.context) {
                Intent(it, ModulesActivity::class.java).apply {
                    putExtra("OPEN_ADD_MODULE_DIALOG", true)
                }
            }
        }

        // 3. Add Assignment Button
        btnAddAssignment?.setOnClickListener { view ->
            safeLaunchActivity(view.context) {
                Intent(it, AddAssignmentActivity::class.java)
            }
        }
    }

    private inline fun safeLaunchActivity(ctx: Context, createIntent: (Context) -> Intent) {
        try {
            val intent = createIntent(ctx)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
            dismiss()
        } catch (e: Exception) {
            Log.e("RadialMenuDialog", "Navigation failed: ${e.message}", e)
        }
    }
}
