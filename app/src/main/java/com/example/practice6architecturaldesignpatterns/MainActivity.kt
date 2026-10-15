package com.example.practice6architecturaldesignpatterns

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.practice6architecturaldesignpatterns.mvc.view.MvcActivity
import com.example.practice6architecturaldesignpatterns.mvp.view.MvpActivity
import com.example.practice6architecturaldesignpatterns.mvvm.view.MvvmActivity
import com.example.practice6architecturaldesignpatterns.mvi.view.MviActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Weather Patterns Launcher",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MvcActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVC Pattern")
                        }

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MvpActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVP Pattern")
                        }

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MvvmActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVVM Pattern")
                        }

                        Button(
                            onClick = { startActivity(Intent(this@MainActivity, MviActivity::class.java)) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text("MVI Pattern")
                        }
                    }
                }
            }
        }
    }
}
