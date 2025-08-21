package com.example.noteen.modulartest

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.noteen.ui.screen.base.DrawingScreenBase
import com.example.noteen.ui.screen.base.NotesScreenBase
import com.example.noteen.ui.theme.NoteenTheme

class NotesActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteenTheme {
                DrawingScreenBase()
            }
        }
    }
}
