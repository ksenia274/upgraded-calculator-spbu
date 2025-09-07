package spbu.wecalc.project.history

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val expression: String,
    val result: String,
    val timestampMs: Long
)

interface HistoryStore {
    suspend fun addEntry(expression: String, result: String)
    suspend fun getRecent(limit: Int = 10): List<HistoryEntry>
    suspend fun clear()
}

expect object HistoryStoreProvider {
    fun provide(): HistoryStore
}