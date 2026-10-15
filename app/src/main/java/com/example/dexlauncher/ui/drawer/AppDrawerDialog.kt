package com.example.dexlauncher.ui.drawer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dexlauncher.R
import com.example.dexlauncher.data.model.AppInfo
import com.example.dexlauncher.ui.taskbar.drawableToBitmap
import com.example.dexlauncher.ui.theme.DeXDarkPalette

@Composable
fun AppDrawerDialog(
    isOpen: Boolean,
    apps: List<AppInfo>,
    hiddenPackages: Set<String>,
    onDismissRequest: () -> Unit,
    onAppClicked: (AppInfo) -> Unit,
    onTogglePinDesktop: (AppInfo) -> Unit,
    onTogglePinTaskbar: (AppInfo) -> Unit,
    onHideApp: (AppInfo) -> Unit
) {
    if (!isOpen) return

    var searchQuery by remember { mutableStateOf("") }
    // Filter out hidden packages
    val visibleApps = remember(apps, hiddenPackages) {
        apps.filter { !hiddenPackages.contains(it.packageName) }
    }

    val filteredApps = remember(searchQuery, visibleApps) {
        if (searchQuery.isBlank()) {
            visibleApps
        } else {
            visibleApps.filter { it.label.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp, top = 24.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Column(
                modifier = Modifier
                    .widthIn(min = 340.dp, max = 560.dp)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeXDarkPalette.SurfaceFloating)
                    .border(1.dp, DeXDarkPalette.Border, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_apps),
                            color = DeXDarkPalette.TextSecondary,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = DeXDarkPalette.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = DeXDarkPalette.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeXDarkPalette.PrimaryBlue,
                        unfocusedBorderColor = DeXDarkPalette.Border,
                        focusedTextColor = DeXDarkPalette.TextPrimary,
                        unfocusedTextColor = DeXDarkPalette.TextPrimary,
                        cursorColor = DeXDarkPalette.ActiveCyan
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 78.dp),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        AppDrawerItem(
                            app = app,
                            onClick = {
                                onAppClicked(app)
                                onDismissRequest()
                            },
                            onTogglePinDesktop = { onTogglePinDesktop(app) },
                            onTogglePinTaskbar = { onTogglePinTaskbar(app) },
                            onHideApp = { onHideApp(app) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerItem(
    app: AppInfo,
    onClick: () -> Unit,
    onTogglePinDesktop: () -> Unit,
    onTogglePinTaskbar: () -> Unit,
    onHideApp: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val bitmap = remember(app.icon) {
        app.icon?.let { drawableToBitmap(it) }
    }

    Box(contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(78.dp)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = Color.White),
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(vertical = 6.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(46.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = app.label,
                color = DeXDarkPalette.TextPrimary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(DeXDarkPalette.CardBackground)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (app.isPinnedToDesktop) "Bỏ khỏi Desktop" else "Thêm vào Desktop",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (app.isPinnedToDesktop) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = DeXDarkPalette.ActiveCyan,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    onTogglePinDesktop()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text(
                        if (app.isPinnedToTaskbar) "Bỏ ghim Taskbar" else "Ghim Taskbar",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = DeXDarkPalette.PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    onTogglePinTaskbar()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Ẩn ứng dụng", color = Color(0xFFFBBF24), fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    onHideApp()
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = {
                    Text("Gỡ cài đặt", color = Color(0xFFEF4444), fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                        data = Uri.parse("package:${app.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(uninstallIntent)
                }
            )

            DropdownMenuItem(
                text = {
                    Text(stringResource(R.string.app_info), color = Color.White, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = DeXDarkPalette.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                },
                onClick = {
                    showMenu = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", app.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}
