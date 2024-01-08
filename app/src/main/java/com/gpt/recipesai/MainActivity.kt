package com.gpt.recipesai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gpt.recipesai.data.models.Message
import com.gpt.recipesai.data.network.GptCommunicator
import com.gpt.recipesai.ui.theme.RecipesAITheme
import com.gpt.recipesai.ui.theme.color1
import com.gpt.recipesai.ui.theme.color2
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val gpt = GptCommunicator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipesAITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val prompt = "Przepis na dziś: \n"
//                    var gptResponse by remember {
//                        mutableStateOf("")
//                    }

                    val scope = rememberCoroutineScope()
                    val messages by gpt.historymessages.collectAsState()
                    var loading by remember {
                        mutableStateOf(false)
                    }

                    MainUI(
                        messages = messages,
                        loading = loading,
                        onButtonClick = {
                            loading = true
                            scope.launch {
                                val result = gpt.fetchGptResponse(prompt)
                                loading = false
                            }
                        }
                    )

//                    LaunchedEffect(key1 = Unit) {
//                        val result = gpt.fetchGptResponse(prompt)
//
//                        if (result.isFailure) return@LaunchedEffect
//
//                        val mess = result.getOrNull()
//                            ?.choices
//                            ?.first()
//                            ?.message
//                            ?: return@LaunchedEffect
//
//                        val content = mess.content ?: return@LaunchedEffect
//                        gptResponse = content
//                    }

//                    LazyColumn {
//                        item {
//                            Column(
//                                modifier = Modifier
//                                    .height(100.dp)
//
//                            ) {
//                                Text(text = prompt)
//                                Text(text = gptResponse)
//                            }
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun MainUI(
    messages: List<Message>,
    loading: Boolean = false,
    onButtonClick: () -> Unit = {}
) {

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Blue)) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = stringResource(id = R.string.command),
//                fontSize = 20.sp,
//                modifier = Modifier
//                    .background(color1, RoundedCornerShape(16.dp))
//                    .padding(8.dp)
//                    .height(24.dp),
//                textAlign = TextAlign.Center
//            )
//            IconButton(
//                onClick = { onButtonClick() },
//                modifier = Modifier.padding(start = 8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Send,
//                    contentDescription = null,
//                    tint = color2
//                )
//            }
//        }

        SegmentedControl()

        if (loading) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
            }
            return
        } else {
            val filteredMessages = messages.filter { it.role != "system" }
            LazyColumn {
                items(filteredMessages) {
//                items(messages) {
                    it.content ?: return@items
                    Text(
                        text = it.content,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedControl(
    modifier: Modifier = Modifier,
    items: List<String> = stringArrayResource(id = R.array.meals_array).toList(),
    defaultSelectedItemIndex: Int = 0,
    useFixedWidth: Boolean = false,
    itemWidth: Dp = 120.dp,
    cornerRadius: Int = 24,
    onItemSelection: (selectedItemIndex: Int) -> Unit = {}
) {
    val selectedIndex = remember { mutableStateOf(defaultSelectedItemIndex) }
    val itemIndex = remember { mutableStateOf(defaultSelectedItemIndex) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedIndex.value == itemIndex.value) {

                MaterialTheme.colorScheme.background
            } else {

                MaterialTheme.colorScheme.secondary
            }
        ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(color1),
            horizontalArrangement = Arrangement.Center
        ) {
            items.forEachIndexed { index, item ->
                itemIndex.value = index
                Card(
                    modifier = modifier
                        .weight(1f)
                        .padding(2.dp),
                    onClick = {
                        selectedIndex.value = index
                        onItemSelection(selectedIndex.value)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedIndex.value == index) {
                            color2
                        } else {
                            color1
                        },
                        contentColor = if (selectedIndex.value == index)
                            color2
                        else
                            color1
                    ),
                    shape = when (index) {
                        0 -> RoundedCornerShape(
                            topStartPercent = cornerRadius,
                            topEndPercent = cornerRadius,
                            bottomStartPercent = cornerRadius,
                            bottomEndPercent = cornerRadius
                        )

                        items.size - 1 -> RoundedCornerShape(
                            topStartPercent = cornerRadius,
                            topEndPercent = cornerRadius,
                            bottomStartPercent = cornerRadius,
                            bottomEndPercent = cornerRadius
                        )

                        else -> RoundedCornerShape(
                            topStartPercent = 0,
                            topEndPercent = 0,
                            bottomStartPercent = 0,
                            bottomEndPercent = 0
                        )
                    },
                ) {
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(
                            text = item,
                            style = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontWeight = if (selectedIndex.value == index)
                                    LocalTextStyle.current.fontWeight
                                else
                                    FontWeight.Normal,
                                color = if (selectedIndex.value == index)
                                    MaterialTheme.colorScheme.scrim
                                else
                                    MaterialTheme.colorScheme.onSecondary
                            ),
                            textAlign = TextAlign.Center,

                            )
                    }
                }
            }
        }
    }

}

//@Preview
//@Composable
//fun MainUIPreview() {
//    MainUI(messages = emptyList())
//}

@Preview
@Composable
fun SegmentPreview() {
    SegmentedControl()
}

