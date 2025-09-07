package spbu.wecalc.project.history

import kotlinx.serialization.Serializable

@Serializable
data class HistoryAddRequest(
    val expression: String,
    val result: String,
    val timestampMs: Long? = null // сервер подставит сам, если не пришло
)

@Serializable
data class HistoryItemResponse(
    val expression: String,
    val result: String,
    val timestampMs: Long
)

@Serializable
data class HistoryListResponse(
    val items: List<HistoryItemResponse>
)