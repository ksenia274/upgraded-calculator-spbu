package spbu.wecalc.project.history

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.locks.ReentrantLock

private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

private class FileHistoryStore : HistoryStore {

    private val lock = ReentrantLock()
    private val file: File

    init {
        val home = System.getProperty("user.home") ?: "."
        val dir = File(home, ".wecalc")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val f = File(dir, "history.json")
        if (!f.exists()) {
            writeTextToFile(f, "[]")
        }
        file = f
    }

    override suspend fun addEntry(expression: String, result: String) {
        lock.lock()
        try {
            val list = ArrayList<HistoryEntry>(readAll())
            val now = System.currentTimeMillis()
            // кладём новую запись в начало
            list.add(0, HistoryEntry(expression, result, now))
            // ограничиваем до 500 элементов
            while (list.size > 500) {
                list.removeAt(list.size - 1)
            }
            writeTextToFile(file, json.encodeToString(list))
        } finally {
            lock.unlock()
        }
    }

    override suspend fun getRecent(limit: Int): List<HistoryEntry> {
        lock.lock()
        return try {
            val all = readAll()
            if (all.size <= limit) all else all.subList(0, limit)
        } finally {
            lock.unlock()
        }
    }

    override suspend fun clear() {
        lock.lock()
        try {
            writeTextToFile(file, "[]")
        } finally {
            lock.unlock()
        }
    }

    private fun readAll(): List<HistoryEntry> {
        return try {
            val content = readTextFromFile(file)
            json.decodeFromString(content)
        } catch (e: Exception) {
            // на всякий случай — пустой список
            Collections.emptyList()
        }
    }

    // ----------- утилиты на Java IO (никаких kotlin-расширений) -----------

    private fun writeTextToFile(target: File, text: String) {
        var writer: OutputStreamWriter? = null
        try {
            writer = OutputStreamWriter(FileOutputStream(target, false), StandardCharsets.UTF_8)
            writer.write(text)
            writer.flush()
        } finally {
            try { writer?.close() } catch (_: Exception) {}
        }
    }

    private fun readTextFromFile(target: File): String {
        var reader: BufferedReader? = null
        val sb = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(FileInputStream(target), StandardCharsets.UTF_8))
            var line: String?
            while (true) {
                line = reader.readLine() ?: break
                sb.append(line).append('\n')
            }
        } finally {
            try { reader?.close() } catch (_: Exception) {}
        }
        return sb.toString()
    }
}

actual object HistoryStoreProvider {
    actual fun provide(): HistoryStore = FileHistoryStore()
}