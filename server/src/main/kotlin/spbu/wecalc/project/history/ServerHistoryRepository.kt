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
import java.util.regex.Pattern

private val jsonFmt = Json { ignoreUnknownKeys = true }

class ServerHistoryRepository {

    private val rootDir: File
    private val lock = ReentrantLock()

    init {
        val home = System.getProperty("user.home") ?: "."
        val dir = File(home, ".wecalc/server")
        if (!dir.exists()) dir.mkdirs()
        rootDir = dir
    }

    fun add(userId: String, expression: String, result: String, ts: Long) {
        lock.lock()
        try {
            val file = ensureFile(userId)
            val list = ArrayList(readAll(file))
            list.add(0, HistoryItemResponse(expression, result, ts))
            while (list.size > 1000) list.removeAt(list.size - 1)
            writeJson(file, HistoryListResponse(list))
        } finally {
            lock.unlock()
        }
    }

    fun recent(userId: String, limit: Int): List<HistoryItemResponse> {
        lock.lock()
        return try {
            val file = ensureFile(userId)
            val all = readAll(file)
            if (all.size <= limit) all else all.subList(0, limit)
        } finally {
            lock.unlock()
        }
    }

    fun clear(userId: String) {
        lock.lock()
        try {
            val file = ensureFile(userId)
            writeJson(file, HistoryListResponse(Collections.emptyList()))
        } finally {
            lock.unlock()
        }
    }

    // -------------------- utils --------------------

    private fun fileFor(userId: String): File {
        val safe = sanitize(userId)
        return File(rootDir, "$safe.json")
    }

    private fun ensureFile(userId: String): File {
        val f = fileFor(userId)
        if (!f.exists()) {
            f.parentFile?.mkdirs()
            writeJson(f, HistoryListResponse(Collections.emptyList()))
        }
        return f
    }

    private fun writeJson(target: File, payload: HistoryListResponse) {
        var writer: OutputStreamWriter? = null
        try {
            writer = OutputStreamWriter(FileOutputStream(target, false), StandardCharsets.UTF_8)
            writer.write(jsonFmt.encodeToString(payload))
            writer.flush()
        } finally {
            try { writer?.close() } catch (_: Exception) {}
        }
    }

    private fun readAll(file: File): List<HistoryItemResponse> {
        return try {
            val raw = readText(file)
            jsonFmt.decodeFromString<HistoryListResponse>(raw).items
        } catch (_: Exception) {
            Collections.emptyList()
        }
    }

    private fun readText(file: File): String {
        var reader: BufferedReader? = null
        val sb = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(FileInputStream(file), StandardCharsets.UTF_8))
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

    private fun sanitize(s: String): String {
        // заменяем всё, кроме a-zA-Z0-9._- на '_'
        val p = Pattern.compile("[^a-zA-Z0-9._-]")
        return p.matcher(s).replaceAll("_")
    }
}