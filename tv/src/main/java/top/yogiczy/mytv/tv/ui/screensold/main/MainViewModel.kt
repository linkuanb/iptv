package top.yogiczy.mytv.tv.ui.screensold.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import top.yogiczy.mytv.core.data.entities.channel.ChannelGroupList
import top.yogiczy.mytv.core.data.entities.channel.ChannelGroupList.Companion.channelList
import top.yogiczy.mytv.core.data.entities.channel.ChannelList
import top.yogiczy.mytv.core.data.entities.epg.EpgList
import top.yogiczy.mytv.core.data.repositories.epg.EpgRepository
import top.yogiczy.mytv.core.data.repositories.iptv.IptvRepository
import top.yogiczy.mytv.core.data.utils.ChannelUtil
import top.yogiczy.mytv.core.data.utils.Constants
import top.yogiczy.mytv.tv.ui.material.Snackbar
import top.yogiczy.mytv.tv.ui.material.SnackbarType
import top.yogiczy.mytv.tv.ui.utils.Configs

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            refreshChannel()
            refreshEpg()
        }
    }

    private suspend fun refreshChannel() {
        flow {
            emit(
                IptvRepository(Configs.iptvSourceCurrent).getChannelGroupList(cacheTime = Configs.iptvSourceCacheTime)
            )
        }
            .retryWhen { _, attempt ->
                if (attempt >= Constants.NETWORK_RETRY_COUNT) return@retryWhen false

                _uiState.value =
                    MainUiState.Loading("获取远程直播源(${attempt + 1}/${Constants.NETWORK_RETRY_COUNT})...")
                delay(Constants.NETWORK_RETRY_INTERVAL)
                true
            }
            .catch {
                _uiState.value = MainUiState.Error(it.message)
            }
            .map { hybridChannel(it) }
            .map {
                _uiState.value = MainUiState.Ready(channelGroupList = it)
                it
            }
            .collect()
    }

    private fun hybridChannel(channelGroupList: ChannelGroupList): ChannelGroupList {
        val hybridMode = Configs.iptvHybridMode
        return when (hybridMode) {
            Configs.IptvHybridMode.DISABLE -> channelGroupList
            Configs.IptvHybridMode.IPTV_FIRST -> {
                ChannelGroupList(channelGroupList.map { group ->
                    group.copy(channelList = ChannelList(group.channelList.map { channel ->
                        channel.copy(
                            urlList = channel.urlList.plus(
                                ChannelUtil.getHybridWebViewUrl(channel.name) ?: emptyList()
                            )
                        )
                    }))
                })
            }

            Configs.IptvHybridMode.HYBRID_FIRST -> {
                ChannelGroupList(channelGroupList.map { group ->
                    group.copy(channelList = ChannelList(group.channelList.map { channel ->
                        channel.copy(
                            urlList = (ChannelUtil.getHybridWebViewUrl(channel.name) ?: emptyList())
                                .plus(channel.urlList)
                        )
                    }))
                })
            }
        }
    }

    private suspend fun refreshEpg() {
        if (!Configs.epgEnable) return

        if (_uiState.value is MainUiState.Ready) {
            val channelGroupList = (_uiState.value as MainUiState.Ready).channelGroupList

            flow {
                emit(
                    EpgRepository(Configs.epgSourceCurrent).getEpgList(
                        filteredChannels = channelGroupList.channelList.map { it.epgName },
                        refreshTimeThreshold = Configs.epgRefreshTimeThreshold,
                    )
                )
            }
                .retry(Constants.NETWORK_RETRY_COUNT) { delay(Constants.NETWORK_RETRY_INTERVAL); true }
                .catch {
                    emit(EpgList())
                    Snackbar.show("节目单获取失败，请检查网络连接", type = SnackbarType.ERROR)
                }
                .map { epgList ->
                    _uiState.value = (_uiState.value as MainUiState.Ready).copy(epgList = epgList)
                }
                .collect()
        }
    }
}

sealed interface MainUiState {
    data class Loading(val message: String? = null) : MainUiState
    data class Error(val message: String? = null) : MainUiState
    data class Ready(
        val channelGroupList: ChannelGroupList = ChannelGroupList(),
        val epgList: EpgList = EpgList(),
    ) : MainUiState
}