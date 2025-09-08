package spbu.wecalc.project.history

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.historyModule() {
    val repo = ServerHistoryRepository()

    routing {
        route("/api/history") {

            get {
                val userHeader = call.request.headers["X-User-Id"]
                if (userHeader == null || userHeader == "") {
                    call.respond(HttpStatusCode.Unauthorized, "Missing X-User-Id")
                    return@get
                }

                val limitParam = call.request.queryParameters["limit"]
                val limit = try {
                    if (limitParam == null) 10 else Integer.parseInt(limitParam)
                } catch (_: Exception) { 10 }

                val items = repo.recent(userHeader, limit)
                call.respond(HistoryListResponse(items))
            }

            post {
                val userHeader = call.request.headers["X-User-Id"]
                if (userHeader == null || userHeader == "") {
                    call.respond(HttpStatusCode.Unauthorized, "Missing X-User-Id")
                    return@post
                }

                val req = call.receive<HistoryAddRequest>()
                val ts = if (req.timestampMs == null) System.currentTimeMillis() else req.timestampMs
                repo.add(userHeader, req.expression, req.result, ts!!)
                call.respond(HttpStatusCode.Accepted)
            }

            delete {
                val userHeader = call.request.headers["X-User-Id"]
                if (userHeader == null || userHeader == "") {
                    call.respond(HttpStatusCode.Unauthorized, "Missing X-User-Id")
                    return@delete
                }
                repo.clear(userHeader)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}