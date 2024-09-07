package top.yogiczy.mytv.tv.ui.screen.settings.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import top.yogiczy.mytv.core.util.utils.humanizeMs
import top.yogiczy.mytv.tv.ui.rememberChildPadding
import top.yogiczy.mytv.tv.ui.screen.components.AppScreen
import top.yogiczy.mytv.tv.ui.screen.settings.components.SettingsListItem
import top.yogiczy.mytv.tv.ui.screensold.settings.SettingsViewModel
import top.yogiczy.mytv.tv.ui.theme.MyTvTheme
import top.yogiczy.mytv.tv.ui.utils.Configs
import java.text.DecimalFormat

@Composable
fun SettingsUiScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
    toUiTimeShowModeScreen: () -> Unit = {},
    toUiScreenAutoCloseDelayScreen: () -> Unit = {},
    toUiDensityScaleRatioScreen: () -> Unit = {},
    toUiFontScaleRatioScreen: () -> Unit = {},
    onBackPressed: () -> Unit = {},
) {
    val childPadding = rememberChildPadding()

    AppScreen(
        modifier = modifier.padding(top = 10.dp),
        header = { Text("设置 / 界面") },
        canBack = true,
        onBackPressed = onBackPressed,
    ) {
        LazyColumn(
            contentPadding = childPadding.copy(top = 10.dp).paddingValues,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                val showProgress = settingsViewModel.uiShowEpgProgrammeProgress

                SettingsListItem(
                    headlineContent = "节目进度",
                    supportingContent = "在频道底部显示当前节目进度条",
                    trailingContent = { Switch(showProgress, null) },
                    onSelected = {
                        settingsViewModel.uiShowEpgProgrammeProgress = !showProgress
                    },
                )
            }

            item {
                val showProgress = settingsViewModel.uiShowEpgProgrammePermanentProgress

                SettingsListItem(
                    headlineContent = "常驻底部节目进度",
                    supportingContent = "在播放器底部显示当前节目进度条",
                    trailingContent = {
                        Switch(showProgress, null)
                    },
                    onSelected = {
                        settingsViewModel.uiShowEpgProgrammePermanentProgress = !showProgress
                    },
                )
            }

            item {
                val showChannelLogo = settingsViewModel.uiShowChannelLogo

                SettingsListItem(
                    headlineContent = "台标显示",
                    trailingContent = {
                        Switch(showChannelLogo, null)
                    },
                    onSelected = {
                        settingsViewModel.uiShowChannelLogo = !showChannelLogo
                    },
                )
            }

            item {
                val showChannelPreview = settingsViewModel.uiShowChannelPreview

                SettingsListItem(
                    headlineContent = "频道预览",
                    trailingContent = {
                        Switch(showChannelPreview, null)
                    },
                    onSelected = {
                        settingsViewModel.uiShowChannelPreview = !showChannelPreview
                    },
                )
            }

            item {
                val useClassicPanelScreen = settingsViewModel.uiUseClassicPanelScreen

                SettingsListItem(
                    headlineContent = "经典选台界面",
                    supportingContent = "将选台界面替换为经典三段式结构",
                    trailingContent = {
                        Switch(useClassicPanelScreen, null)
                    },
                    onSelected = {
                        settingsViewModel.uiUseClassicPanelScreen = !useClassicPanelScreen
                    },
                )
            }

            item {
                val timeShowMode = settingsViewModel.uiTimeShowMode

                SettingsListItem(
                    headlineContent = "时间显示",
                    trailingContent = {
                        Text(
                            when (timeShowMode) {
                                Configs.UiTimeShowMode.HIDDEN -> "隐藏"
                                Configs.UiTimeShowMode.ALWAYS -> "常显"
                                Configs.UiTimeShowMode.EVERY_HOUR -> "整点"
                                Configs.UiTimeShowMode.HALF_HOUR -> "半点"
                            }
                        )
                    },
                    onSelected = toUiTimeShowModeScreen,
                    link = true,
                )
            }

            item {
                val delay = settingsViewModel.uiScreenAutoCloseDelay

                SettingsListItem(
                    headlineContent = "超时自动关闭界面",
                    trailingContent = when (delay) {
                        Long.MAX_VALUE -> "不关闭"
                        else -> delay.humanizeMs()
                    },
                    onSelected = toUiScreenAutoCloseDelayScreen,
                    link = true,
                )
            }

            item {
                val scaleRatio = settingsViewModel.uiDensityScaleRatio

                SettingsListItem(
                    headlineContent = "界面整体缩放比例",
                    trailingContent = when (scaleRatio) {
                        0f -> "自适应"
                        else -> "×${DecimalFormat("#.#").format(scaleRatio)}"
                    },
                    onSelected = toUiDensityScaleRatioScreen,
                    link = true,
                )
            }

            item {
                val scaleRatio = settingsViewModel.uiFontScaleRatio

                SettingsListItem(
                    headlineContent = "界面字体缩放比例",
                    trailingContent = "×${DecimalFormat("#.#").format(scaleRatio)}",
                    onSelected = toUiFontScaleRatioScreen,
                    link = true,
                )
            }

            item {
                val focusOptimize = settingsViewModel.uiFocusOptimize

                SettingsListItem(
                    headlineContent = "焦点优化",
                    supportingContent = "关闭后可解决触摸设备在部分场景下闪退",
                    trailingContent = {
                        Switch(focusOptimize, null)
                    },
                    onSelected = {
                        settingsViewModel.uiFocusOptimize = !focusOptimize
                    },
                )
            }
        }
    }
}

@Preview(device = "id:Android TV (720p)")
@Composable
private fun SettingsUiScreenPreview() {
    MyTvTheme {
        SettingsUiScreen()
    }
}