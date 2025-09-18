package com.flxholle.forensiceye.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flxholle.forensiceye.R

/**
 * Composable function to display the title bar with the app icon.
 *
 * @param modifier Modifier to be applied to the Row layout.
 * @param text1 The first part of the title text (e.g., "Forensic").
 * @param text2 The second part of the title text (e.g., "Eye").
 * @param appIcon ImageBitmap representing the app icon.
 */
@Composable
fun TitleBar(modifier: Modifier = Modifier, text1: String, text2: String, appIcon: ImageBitmap) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Bottom) {
            val autoSize = TextAutoSize.StepBased(
                minFontSize = 24.sp,
                maxFontSize = 48.sp,
                stepSize = 2.sp
            )

            // Display the "Forensic" text with custom font and style
            Text(
                text = text1,
                fontFamily = FontFamily(Font(R.font.rubikglitch, FontWeight.Normal)),
                modifier = Modifier
                    .height(54.dp),
                style = MaterialTheme.typography.headlineLarge,
                autoSize = autoSize
            )
            // Display the "Eye" text with custom font and style, aligned to the end
            Text(
                text = text2,
                fontFamily = FontFamily(Font(R.font.aldrich, FontWeight.Normal)),
                modifier = Modifier
                    .height(54.dp)
                    .fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.End,
                autoSize = autoSize
            )
        }
        // Display the app icon image
        Image(
            appIcon,
            contentDescription = stringResource(R.string.image),
            modifier = Modifier
                .size(108.dp) //Optional, but keeps the image reasonably small
                .padding(
                    top = 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 8.dp
                )
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.height(24.dp), verticalAlignment = Alignment.CenterVertically) {
        // Display the "by_me" text
        Text(
            stringResource(R.string.by_me),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        val openDialog = remember { mutableStateOf(false) }
        // Display the "Credits" button
        OutlinedButton(
            onClick = { openDialog.value = true },
            modifier = Modifier.fillMaxHeight(),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                "Credits", autoSize = TextAutoSize.StepBased(
                    minFontSize = 1.sp,
                    maxFontSize = 12.sp,
                    stepSize = 1.sp,
                ), color = Color.White
            )
        }
        // Display the credits dialog if the button is clicked
        if (openDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                title = {
                    Text(text = "Credits")
                },
                text = {
                    Text(
                        stringResource(R.string.thank_you_msg)
                    )
                },
                confirmButton = {},
                dismissButton = {}
            )
        }
    }
}

/**
 * Composable function to display an action icon with text.
 *
 * @param iconId Resource ID of the icon to be displayed.
 * @param contentDescription Description of the icon for accessibility.
 * @param text Text to be displayed below the icon.
 * @param modifier Modifier to be applied to the Column layout.
 */
@Composable
fun ActionIcon(
    iconId: Int,
    contentDescription: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        // Display the icon
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
        // Display the text below the icon
        Text(text)
    }
}

/**
 * Composable function to display a status column with an icon and text.
 *
 * @param iconId Resource ID of the icon to be displayed.
 * @param tint Color to tint the icon.
 * @param text Text to be displayed below the icon.
 * @param modifier Modifier to be applied to the Column layout.
 * @param background Background color for the icon.
 */
@Composable
fun StatusColumn(
    iconId: Int,
    tint: Color,
    text: String,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.background
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(56.dp)
    ) {
        // Display the icon with tint and background
        Icon(
            painter = painterResource(id = iconId),
            tint = tint,
            modifier = Modifier.size(36.dp),//.background(background),
            contentDescription = text
        )
        // Display the text below the icon
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 8.sp
        )
    }
}

/**
 * Overwrites the standard Text composable to include the new AutoSize feature.
 *
 * @param text The text to be displayed.
 * @param modifier Modifier to be applied to the BasicText.
 * @param color Color of the text.
 * @param fontSize Size of the text.
 * @param fontStyle Style of the text (italic, normal, etc.).
 * @param fontWeight Weight of the text (bold, normal, etc.).
 * @param fontFamily Font family of the text.
 * @param letterSpacing Letter spacing of the text.
 * @param textDecoration Decoration of the text (underline, line-through, etc.).
 * @param textAlign Alignment of the text.
 * @param lineHeight Line height of the text.
 * @param overflow How to handle text overflow.
 * @param softWrap Whether the text should wrap when it reaches the end of a line.
 * @param maxLines Maximum number of lines for the text.
 * @param minLines Minimum number of lines for the text.
 * @param onTextLayout Callback to be invoked when the text layout is calculated.
 * @param style Style configuration for the text.
 * @param autoSize Configuration for auto-sizing the text.
 */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
    autoSize: TextAutoSize? = null,
) {
    val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

    // Use BasicText to display the text with merged styles and auto-size configuration
    BasicText(
        text,
        modifier,
        style.merge(
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing
        ),
        onTextLayout,
        overflow,
        softWrap,
        maxLines,
        minLines,
        autoSize = autoSize
    )
}