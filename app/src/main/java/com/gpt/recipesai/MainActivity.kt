package com.gpt.recipesai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gpt.recipesai.data.network.GptCommunicator
import com.gpt.recipesai.ui.theme.RecipesAITheme

class MainActivity : ComponentActivity() {

    private val gpt = GptCommunicator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipesAITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prompt = "Przepis na dziś:"
                    var gptResponse by remember {
                        mutableStateOf("")
                    }
                    LaunchedEffect(key1 = Unit) {
                        val result = gpt.fetchGptResponse(prompt)

                        if (result.isFailure) return@LaunchedEffect

                        val mess = result.getOrNull()
                            ?.choices
                            ?.first()
                            ?.message
                            ?: return@LaunchedEffect

                        val content = mess.content ?: return@LaunchedEffect
                        gptResponse = content
                    }

                    Column {
                        Text(text = prompt)
                        Text(text = gptResponse)
                    }
                }
            }
        }
    }
}
