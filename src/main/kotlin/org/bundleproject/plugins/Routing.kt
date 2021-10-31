package org.bundleproject.plugins

import guru.zoroark.ratelimit.rateLimited
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import java.nio.file.Paths
import kotlinx.coroutines.launch
import org.bundleproject.json.request.ModRequest
import org.bundleproject.json.responses.BulkModResponse
import org.bundleproject.json.responses.ModResponse
import org.bundleproject.json.responses.ModResponseData
import org.bundleproject.json.responses.VersionResponse
import org.bundleproject.utils.AssetsCache
import org.bundleproject.utils.getBulkMods
import org.bundleproject.utils.getModFromRequest
import org.bundleproject.utils.resolveUrl

fun Application.configureRouting() {
    // Preload assets in background
    launch { AssetsCache.modAssets }
    // Configure routes
    routing {
        static("static") { resources("static") }
        get("/") {
            call.respondFile(
                Paths.get(this::class.java.getResource("/static/docs.html")!!.toURI()).toFile()
            )
        }
        get("/docs") {
            call.respondFile(
                Paths.get(this::class.java.getResource("/static/docs.html")!!.toURI()).toFile()
            )
        }
        rateLimited {
            get("/v1/mods/{id}/{platform}/{minecraftVersion}/{version}") {
                val modData = getModFromRequest(ModRequest(call))
                call.respond(
                    HttpStatusCode.OK,
                    ModResponse(
                        data =
                            ModResponseData(
                                url = resolveUrl(modData),
                                version = modData.version,
                                metadata = modData.metadata,
                            )
                    )
                )
            }
            get("/v1/mods/{id}/{platform}/{minecraftVersion}/{version}/download") {
                val modData = getModFromRequest(ModRequest(call))
                call.respondRedirect(permanent = true, url = resolveUrl(modData))
            }
            get("/v1/mods/bulk/{bulk}") {
                call.respond(HttpStatusCode.OK, BulkModResponse(mods = getBulkMods(call)))
            }

            get("/v1/bundle/version") {
                call.respond(HttpStatusCode.OK, VersionResponse(data = AssetsCache.versionAssets))
            }
        }
    }
}
