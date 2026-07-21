package com.penguenlabs.pushnote.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -----------------------------------------------------------
// Neubrutalism design tokens
// -----------------------------------------------------------
val neuBorderWidth = 3.dp
val neuShadowOffsetX = 4.dp
val neuShadowOffsetY = 4.dp
val neuCornerRadius = 0.dp
val neuShape = RoundedCornerShape(neuCornerRadius)

/**
 * Applies a hard, offset shadow behind the composable. The caller is responsible for stacking.
 * This modifier is meant to be used inside a Box where the shadow layer sits behind the content.
 */
fun Modifier.neuShadow(
    color: Color,
    offsetX: Dp = neuShadowOffsetX,
    offsetY: Dp = neuShadowOffsetY,
    shape: Shape = neuShape
): Modifier = this
    .offset(offsetX, offsetY)
    .clip(shape)
    .background(color)

/**
 * Applies the classic Neubrutalist border.
 */
fun Modifier.neuBorder(
    color: Color,
    width: Dp = neuBorderWidth,
    shape: Shape = neuShape
): Modifier = this
    .clip(shape)
    .border(width, color, shape)

@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    // The content container must wrap its content so the outer Box gets a definite size.
    // The shadow layer then matches that size.
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuShadow(color = shadowColor)
        )
        Box(
            modifier = Modifier
                .neuBorder(color = borderColor)
                .background(backgroundColor)
        ) {
            content()
        }
    }
}

@Composable
fun NeuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuShadow(color = if (enabled) shadowColor else shadowColor.copy(alpha = 0.3f))
        )
        Row(
            modifier = Modifier
                .matchParentSize()
                .neuBorder(color = if (enabled) borderColor else borderColor.copy(alpha = 0.3f))
                .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color = contentColor),
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun NeuOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    NeuButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        borderColor = borderColor,
        shadowColor = shadowColor,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun NeuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    contentDescription: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = modifier.size(48.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuShadow(color = shadowColor)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuBorder(color = borderColor)
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color = contentColor),
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                content?.invoke()
            }
        }
    }
}

@Composable
fun NeuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    textStyle: TextStyle = TextStyle(
        fontFamily = latoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
) {
    val borderColor = when {
        isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    val shadowColor = when {
        isError -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Box(modifier = modifier.height(56.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuShadow(color = shadowColor)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .matchParentSize()
                .neuBorder(color = borderColor)
                .background(MaterialTheme.colorScheme.surface),
            textStyle = textStyle,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder != null) {
                        placeholder()
                    }
                    innerTextField()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeuFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    selectedBackgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSecondary,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline
) {
    val interactionSource = remember { MutableInteractionSource() }
    val bg = if (selected) selectedBackgroundColor else backgroundColor
    val fg = if (selected) selectedContentColor else contentColor

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Box(modifier = modifier.height(34.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .neuShadow(color = shadowColor, offsetX = 3.dp, offsetY = 3.dp)
            )
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .neuBorder(color = borderColor, width = 2.5.dp)
                    .background(bg)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ProvideTextStyle(
                    value = TextStyle(
                        fontFamily = latoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = fg,
                        textAlign = TextAlign.Center
                    )
                ) {
                    label()
                }
            }
        }
    }
}

@Composable
fun NeuSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedTrackColor: Color = MaterialTheme.colorScheme.secondary,
    uncheckedTrackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    thumbColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline
) {
    val width = 52.dp
    val height = 30.dp
    val thumbSize = 22.dp
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.size(width, height)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuShadow(color = shadowColor, offsetX = 3.dp, offsetY = 3.dp)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .neuBorder(color = borderColor, width = 2.5.dp)
                .background(if (checked) checkedTrackColor else uncheckedTrackColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onCheckedChange(!checked) }
                )
        )
        Box(
            modifier = Modifier
                .size(thumbSize)
                .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 3.dp)
                .neuBorder(color = borderColor, width = 2.dp)
                .background(thumbColor)
        )
    }
}

@Composable
fun NeuTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        onBackClick?.let { click ->
            NeuIconButton(
                onClick = click,
                painter = null,
                contentDescription = null,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Text(
                    text = "←",
                    fontSize = 22.sp,
                    fontFamily = latoFontFamily,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}
