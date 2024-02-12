package com.gpt.recipesai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gpt.recipesai.data.models.text.Message
import com.gpt.recipesai.data.network.GptCommunicator
import com.gpt.recipesai.ui.theme.RecipesAITheme
import com.gpt.recipesai.ui.theme.color0
import com.gpt.recipesai.ui.theme.color1
import com.gpt.recipesai.ui.theme.color2
import com.gpt.recipesai.ui.theme.color6
import com.gpt.recipesai.ui.theme.color7
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val gpt = GptCommunicator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipesAITheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = color6
                ) {

                    val scope = rememberCoroutineScope()
                    val messages by gpt.historymessages.collectAsState()

                    val imageUrl by gpt.imageUrl.collectAsState()

                    var loading by remember {
                        mutableStateOf(false)
                    }
                    var loading2 by remember {
                        mutableStateOf(false)
                    }

                    val mealsList = stringArrayResource(id = R.array.meals_array).toList()
                    var selectedMealIndex by remember { mutableStateOf(mealsList.size - 1) }


                    var excludedProducts by remember {
                        mutableStateOf("")
                    }

                    var mealCharacteristics by remember {
                        mutableStateOf("")
                    }

                    val context = LocalContext.current

                    MainUI(
                        messages = messages,
                        imageUrl = imageUrl,
                        loading = loading,
                        loading2 = loading2,
                        defaultMealIndex = selectedMealIndex,
                        meals = mealsList,
                        onGenerateClick = {
                            var prompt = context.getString(R.string.create_recipe)
                            if (selectedMealIndex != mealsList.size - 1)
                                prompt += context.getString(R.string.for_) + mealsList.get(
                                    selectedMealIndex
                                )
                            prompt += "."
                            if (excludedProducts.length != 0)
                                prompt += context.getString(R.string.exclusions) + "\"\n\t + $excludedProducts \n\"."
                            if (mealCharacteristics.length != 0)
                                prompt += context.getString(R.string.meal_properties) + "\"\n\t + $mealCharacteristics \n\"."
                            prompt += context.getString(R.string.reply_english) + "\n " +
                                    context.getString(R.string.reply_foreign) + " \n" + context.getString(
                                R.string.rest_prompt
                            )
                            loading = true
                            scope.launch {
                                val result = gpt.fetchGptResponse(prompt)
                                val imagePrompt = messages.last().toString().substringBefore('\n')
                                val result2 =
                                    gpt.fetchImageGeneration(prompt = "Meal: $imagePrompt")
//                                val result2 =
//                                    gpt.fetchImageGeneration(prompt = "Spaghetti with shrimp and asparagus")
                                loading2 = true
                            }
                        },
                        onMealSelection = {
                            selectedMealIndex = it
                        },
                        onExcludedInsert = {
                            excludedProducts = it
                        },
                        onFeaturesInserted = {
                            mealCharacteristics = it
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainUI(
    messages: List<Message>,
    imageUrl: String,
    meals: List<String>,
    defaultMealIndex: Int,
    loading: Boolean = false,
    loading2: Boolean = false,
    onGenerateClick: () -> Unit = {},
    onMealSelection: (selectedMealIndex: Int) -> Unit,
    onExcludedInsert: (String) -> Unit,
    onFeaturesInserted: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color6)
            .padding(8.dp)
    ) {


        SegmentedControl(
            defaultSelectedItemIndex = defaultMealIndex,
            items = meals,
            onMealSelection = onMealSelection
        )

        ExcludedProduct(onExcludedInsert = onExcludedInsert)
        MealsFeatures(onFeaturesInserted = onFeaturesInserted)

        val controller = LocalSoftwareKeyboardController.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp)
                .clickable {
                    onGenerateClick()
                    controller?.hide()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(id = R.string.generate),
                color = color1,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onGenerateClick() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    tint = color1,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (loading) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(color = color1)
            }
            return
        } else {
            val filteredMessages = messages.filter { it.role != "system" && it.role != "user" }
            LazyColumn {
                items(filteredMessages) {
//                items(messages) {
                    it.content ?: return@items

                    // test
                    Text(text = it.content + "\n")
                    Text(
                        text = it.content.substringAfter("\n").substringBefore("\n"),
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading2) {
                            CircularProgressIndicator(color = color1)
                        } else {
                            AsyncImage(
                                model = imageUrl, contentDescription = null,
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .size(300.dp)
                            )
                        }
                    }
//                    Text(text = imageUrl)
                    Text(
                        text = it.content.substringAfter("\n").substringAfter("\n"),
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
    defaultSelectedItemIndex: Int,
    useFixedWidth: Boolean = false,
    itemWidth: Dp = 120.dp,
    cornerRadius: Int = 24,
    onMealSelection: (selectedMealIndex: Int) -> Unit
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
                        onMealSelection(selectedIndex.value)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedIndex.value == index) {
                            color6
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
                            topEndPercent = 0,
                            bottomStartPercent = cornerRadius,
                            bottomEndPercent = 0
                        )

                        items.size - 1 -> RoundedCornerShape(
                            topStartPercent = 0,
                            topEndPercent = cornerRadius,
                            bottomStartPercent = 0,
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
                    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = item,
                            style = LocalTextStyle.current.copy(
                                fontSize = 12.sp,
                                fontWeight = if (selectedIndex.value == index)
                                    LocalTextStyle.current.fontWeight
                                else
                                    FontWeight.Normal,
                                color = if (selectedIndex.value == index)
                                    color0
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

@Composable
fun ExcludedProduct(
    onExcludedInsert: (String) -> Unit

) {
    var input by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            shape = ShapeDefaults.Medium,
            modifier = Modifier
                .fillMaxWidth(),
            value = input,
            onValueChange = {
                onExcludedInsert(it)
                input = it
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color1,
                unfocusedBorderColor = color1,
                cursorColor = color1,
                disabledLabelColor = color1,
                focusedLabelColor = color1,
                focusedContainerColor = color7,
                unfocusedContainerColor = color7,
                focusedTextColor = color0,
                unfocusedTextColor = color0
            ),
            label = {
                Text(
                    text = stringResource(id = R.string.excluded),
                    color = color0,
                    fontSize = 14.sp,
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        input = ""
                        onExcludedInsert("")
                    },
                    tint = color0
                )
            },
            maxLines = 3
        )
    }
}

@Composable
fun MealsFeatures(
    onFeaturesInserted: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        OutlinedTextField(
            shape = ShapeDefaults.Medium,
            modifier = Modifier
                .fillMaxWidth(),
            value = input,
            onValueChange = {
                onFeaturesInserted(it)
                input = it
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color1,
                unfocusedBorderColor = color1,
                cursorColor = color1,
                disabledLabelColor = color1,
                focusedLabelColor = color1,
                focusedContainerColor = color7,
                unfocusedContainerColor = color7,
                focusedTextColor = color0,
                unfocusedTextColor = color0
            ),
            label = {
                Text(
                    text = stringResource(id = R.string.properties),
                    color = color0,
                    fontSize = 14.sp
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        input = ""
                        onFeaturesInserted("")
                    },
                    tint = color0
                )
            },
            maxLines = 3
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainUIPreview() {
    MainUI(
        messages = emptyList(),
        imageUrl = "",
        meals = listOf("Breakfast", "Dinner", "Any"),
        defaultMealIndex = 1,
        onExcludedInsert = {},
        onFeaturesInserted = {},
        onMealSelection = {},
        onGenerateClick = {}
    )
}

//@Preview
//@Composable
//fun SegmentPreview() {
//    SegmentedControl(
//        defaultSelectedItemIndex = 3,
//        onMealSelection = {}
//    )
//}

//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun ExcludedProductPreview() {
//    ExcludedProduct(onExcludedInsert = {})
//}