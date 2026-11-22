package com.example.engine

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import com.example.data.local.BrowserRepository
import com.example.data.local.DownloadEntity
import com.example.data.local.DownloadStatus
import java.io.File

class NativeDownloadController(
    private val context: Context,
    private val repository: BrowserRepository
) {
    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    suspend fun enqueueDownload(
        url: String,
        userAgent: String? = null,
        contentDisposition: String? = null,
        mimeType: String? = null,
        showNotification: Boolean = true
    ): Result<DownloadEntity> {
        return runCatching {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                .ifBlank { "download_${System.currentTimeMillis()}" }
            val resolvedMime = mimeType?.takeIf { it.isNotBlank() } ?: "*/*"

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("Downloading via Rauf Browser")
                setMimeType(resolvedMime)
                val cookies = CookieManager.getInstance().getCookie(url)
                if (!cookies.isNullOrBlank()) {
                    addRequestHeader("Cookie", cookies)
                }
                if (!userAgent.isNullOrBlank()) {
                    addRequestHeader("User-Agent", userAgent)
                }
                setNotificationVisibility(
                    if (showNotification) {
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    } else {
                        DownloadManager.Request.VISIBILITY_HIDDEN
                    }
                )
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)

                // Zero broad-storage-permission compliant destination
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                } else {
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                }
            }

            val systemId = downloadManager.enqueue(request)
            val entity = DownloadEntity(
                systemDownloadId = systemId,
                fileName = fileName,
                url = url,
                mimeType = resolvedMime,
                status = DownloadStatus.RUNNING.name
            )
            val rowId = repository.insertDownload(entity)
            entity.copy(id = rowId)
        }
    }

    suspend fun syncActiveDownloads() {
        val active = repository.getActiveDownloads()
        for (item in active) {
            if (item.systemDownloadId <= 0L) continue
            val query = DownloadManager.Query().setFilterById(item.systemDownloadId)
            val cursor = runCatching { downloadManager.query(query) }.getOrNull()
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val statusIdx = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val downloadedIdx = c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIdx = c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val localUriIdx = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                    val dmStatus = if (statusIdx >= 0) c.getInt(statusIdx) else DownloadManager.STATUS_RUNNING
                    val downloaded = if (downloadedIdx >= 0) c.getLong(downloadedIdx) else item.downloadedBytes
                    val total = if (totalIdx >= 0) c.getLong(totalIdx) else item.totalBytes
                    val localUri = if (localUriIdx >= 0) c.getString(localUriIdx) else item.localUri

                    val mappedStatus = when (dmStatus) {
                        DownloadManager.STATUS_PENDING -> DownloadStatus.PENDING
                        DownloadManager.STATUS_RUNNING -> DownloadStatus.RUNNING
                        DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                        else -> DownloadStatus.RUNNING
                    }

                    if (mappedStatus.name != item.status ||
                        downloaded != item.downloadedBytes ||
                        total != item.totalBytes ||
                        localUri != item.localUri
                    ) {
                        repository.updateDownload(
                            item.copy(
                                status = mappedStatus.name,
                                downloadedBytes = downloaded,
                                totalBytes = total,
                                localUri = localUri
                            )
                        )
                    }
                } else {
                    repository.updateDownload(item.copy(status = DownloadStatus.CANCELLED.name))
                }
            }
        }
    }

    suspend fun cancelDownload(item: DownloadEntity) {
        if (item.systemDownloadId > 0L) {
            runCatching { downloadManager.remove(item.systemDownloadId) }
        }
        repository.updateDownload(item.copy(status = DownloadStatus.CANCELLED.name))
    }

    suspend fun deleteDownload(item: DownloadEntity) {
        if (item.systemDownloadId > 0L) {
            runCatching { downloadManager.remove(item.systemDownloadId) }
        }
        repository.deleteDownloadRecord(item.id)
    }

    fun openDownloadedFile(item: DownloadEntity): Result<Unit> {
        return runCatching {
            val contentUri: Uri? = if (item.systemDownloadId > 0L) {
                downloadManager.getUriForDownloadedFile(item.systemDownloadId)
            } else null

            val targetUri = contentUri ?: item.localUri?.let { raw ->
                val parsed = Uri.parse(raw)
                if (parsed.scheme == "file" && parsed.path != null) {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        File(parsed.path!!)
                    )
                } else {
                    parsed
                }
            } ?: error("Downloaded file URI is unavailable.")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(targetUri, item.mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open ${item.fileName}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
