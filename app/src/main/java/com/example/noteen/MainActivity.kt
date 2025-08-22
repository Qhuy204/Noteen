package com.example.noteen

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.example.noteen.ui.theme.NoteenTheme
import com.example.noteen.data.LocalFileManager.FileManager

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingLoader.init(this)
        FileManager.init(applicationContext)
        TextEditorEngine.init(this)

//        deleteDatabase("noteen_database")

        enableEdgeToEdge()
        setContent {
            NoteenTheme {
                App()
            }
        }
    }
}