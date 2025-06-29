package org.vm.notifier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
//import androidx.media3.common.util.UnstableApi
//import dagger.hilt.android.UnstableApi

//@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun ShowProgressOrContent(progressTitle: String, content: @Composable () -> Unit){

    LaunchedEffect(progressTitle) {
        while(progressTitle != ""){
            //Log.d("CRT", "from ShowProgress")
            delay(1000)
        }
    }

    if(progressTitle != ""){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                Text(
                    text = progressTitle,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }else {
        content()
    }
}
