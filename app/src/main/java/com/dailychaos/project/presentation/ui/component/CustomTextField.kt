package com.dailychaos.project.presentation.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailychaos.project.presentation.theme.DailyChaosTheme

/**
 * Custom Text Field Components
 *
 * "Input yang indah seperti UI KonoSuba - tapi lebih functional!"
 */

@Composable
fun ChaosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    hint: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String = "",
    maxLines: Int = 1,
    minLines: Int = 1,
    maxLength: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    autoFocus: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto focus
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }

    // Animated colors
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(200),
        label = "border_color"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "background_color"
    )

    Column(modifier = modifier) {
        // Label
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Text field container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = if (maxLines == 1) Alignment.CenterVertically else Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Leading icon
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 12.dp),
                        tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text field
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (maxLength == null || newValue.length <= maxLength) {
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onFocusChanged { isFocused = it.isFocused },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = maxLines,
                        minLines = minLines,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = imeAction,
                            capitalization = capitalization
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() },
                            onNext = { /* Handle next */ }
                        ),
                        visualTransformation = visualTransformation,
                        enabled = enabled,
                        readOnly = readOnly
                    )

                    // Hint text
                    if (value.isEmpty() && hint.isNotEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Trailing icon or clear button
                if (trailingIcon != null) {
                    IconButton(
                        onClick = onTrailingIconClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (value.isNotEmpty() && !readOnly) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Character count and error message
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Error message
            if (isError && errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Character count
            if (maxLength != null) {
                Text(
                    text = "${value.length}/$maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (value.length > maxLength * 0.9) {
                        MaterialTheme.colorScheme.warning
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Chaos Text Area - untuk long form writing
 */
@Composable
fun ChaosTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    hint: String = "",
    maxLength: Int = 1000,
    minLines: Int = 5,
    maxLines: Int = 10,
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true
) {
    ChaosTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        hint = hint,
        isError = isError,
        errorMessage = errorMessage,
        maxLines = maxLines,
        minLines = minLines,
        maxLength = maxLength,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Default,
        capitalization = KeyboardCapitalization.Sentences,
        enabled = enabled
    )
}

/**
 * Chaos Search Field - untuk search functionality
 */
@Composable
fun ChaosSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "Search your chaos...",
    onSearchClick: () -> Unit = {},
    enabled: Boolean = true
) {
    ChaosTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        hint = hint,
        leadingIcon = androidx.compose.material.icons.Icons.Default.Search,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search,
        capitalization = KeyboardCapitalization.None,
        enabled = enabled
    )
}

/**
 * Tag Input Field - untuk adding tags
 */
@Composable
fun TagInputField(
    currentTag: String,
    onTagChange: (String) -> Unit,
    onTagAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
    existingTags: List<String> = emptyList(),
    maxTags: Int = 5
) {
    val canAddTag = currentTag.isNotBlank() &&
            !existingTags.contains(currentTag.lowercase()) &&
            existingTags.size < maxTags

    Column(modifier = modifier) {
        ChaosTextField(
            value = currentTag,
            onValueChange = onTagChange,
            label = "Tags",
            hint = "Add a tag...",
            trailingIcon = if (canAddTag) androidx.compose.material.icons.Icons.Default.Add else null,
            onTrailingIconClick = {
                if (canAddTag) {
                    onTagAdd(currentTag.trim())
                }
            },
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.None,
            maxLength = 20
        )

        // Tag suggestions atau existing tags
        if (existingTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(existingTags) { tag ->
                    TagChip(
                        tag = tag,
                        onTagClick = { /* Handle tag click */ },
                        onRemoveClick = { /* Handle remove */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(
    tag: String,
    onTagClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {}
) {
    AssistChip(
        onClick = onTagClick,
        label = { Text("#$tag") },
        trailingIcon = {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Remove tag",
                    modifier = Modifier.size(12.dp)
                )
            }
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * Mini Win Input Field
 */
@Composable
fun MiniWinInputField(
    currentWin: String,
    onWinChange: (String) -> Unit,
    onWinAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
    existingWins: List<String> = emptyList(),
    maxWins: Int = 5
) {
    val canAddWin = currentWin.isNotBlank() && existingWins.size < maxWins

    Column(modifier = modifier) {
        ChaosTextField(
            value = currentWin,
            onValueChange = onWinChange,
            label = "🏆 Mini Wins",
            hint = "What went well today?",
            trailingIcon = if (canAddWin) androidx.compose.material.icons.Icons.Default.Add else null,
            onTrailingIconClick = {
                if (canAddWin) {
                    onWinAdd(currentWin.trim())
                }
            },
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            maxLength = 100
        )

        // Existing wins
        if (existingWins.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            existingWins.forEach { win ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = win,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { /* Handle remove win */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// Extension property untuk warning color
private val ColorScheme.warning: androidx.compose.ui.graphics.Color
    get() = androidx.compose.ui.graphics.Color(0xFFFF9800)

@Preview(showBackground = true)
@Composable
fun ChaosTextFieldPreview() {
    DailyChaosTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var text1 by remember { mutableStateOf("") }
            var text2 by remember { mutableStateOf("Sample text with error") }
            var text3 by remember { mutableStateOf("") }

            ChaosTextField(
                value = text1,
                onValueChange = { text1 = it },
                label = "Title",
                hint = "What happened today?",
                leadingIcon = androidx.compose.material.icons.Icons.Default.Title,
                maxLength = 50
            )

            ChaosTextField(
                value = text2,
                onValueChange = { text2 = it },
                label = "Email",
                hint = "Enter your email",
                isError = true,
                errorMessage = "Invalid email format",
                keyboardType = KeyboardType.Email
            )

            ChaosTextArea(
                value = text3,
                onValueChange = { text3 = it },
                label = "Description",
                hint = "Tell us about your chaos...",
                maxLength = 500
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagInputFieldPreview() {
    DailyChaosTheme {
        var currentTag by remember { mutableStateOf("") }
        val existingTags = listOf("work", "family", "adventure")

        TagInputField(
            currentTag = currentTag,
            onTagChange = { currentTag = it },
            onTagAdd = {
                // Add tag logic
                currentTag = ""
            },
            existingTags = existingTags,
            modifier = Modifier.padding(16.dp)
        )
    }
}