package spbu.wecalc.project.history

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

// Минимальный JSON-конфиг
private val jsonFmt = Json { ignoreUnknownKeys = true }
private const val KEY = "wecalc.history"

// --- JS interop без dynamic и без kotlinx.browser.window ---

// Date.now() -> Double
@JsFun("() => Date.now()")
external fun jsNowMs(): Double

// localStorage.getItem(key) -> String|null
@JsFun("(k) => (globalThis && globalThis.localStorage) ? globalThis.localStorage.getItem(k) : null")
external fun lsGetItem(k: String): String?

// localStorage.setItem(key, value) -> void
@JsFun("(k, v) => { if (globalThis && globalThis.localStorage) globalThis.localStorage.setItem(k, v); }")
external fun lsSetItem(k: String, v: String): Unit

// localStorage.removeItem(key) -> void
@JsFun("(k) => { if (globalThis && globalThis.localStorage) globalThis.localStorage.removeItem(k); }")
external fun lsRemoveItem(k: String): Unit

private class LocalStorageHistoryStore : HistoryStore {
    override suspend fun addEntry(expression: String, result: String) {
        val list = readAll().toMutableList()
        val ts = jsNowMs().toLong()
        list.add(0, HistoryEntry(expression, result, ts))
        if (list.size > 500) {
            list.subList(500, list.size).clear()
        }
        lsSetItem(KEY, jsonFmt.encodeToString(list))
    }

    override suspend fun getRecent(limit: Int): List<HistoryEntry> {
        val all = readAll()
        return if (all.size <= limit) all else all.subList(0, limit)
    }

    override suspend fun clear() {
        lsRemoveItem(KEY)
    }

    private fun readAll(): List<HistoryEntry> = try {
        val raw = lsGetItem(KEY) ?: "[]"
        jsonFmt.decodeFromString(raw)
    } catch (_: Throwable) {
        emptyList()
    }
}

actual object HistoryStoreProvider {
    actual fun provide(): HistoryStore = LocalStorageHistoryStore()
}