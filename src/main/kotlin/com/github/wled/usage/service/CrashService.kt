package com.github.wled.usage.service

import com.github.wled.usage.dto.CrashReportRequest
import com.github.wled.usage.dto.MapFileUploadRequest
import com.github.wled.usage.entity.CrashInstance
import com.github.wled.usage.entity.CrashReport
import com.github.wled.usage.entity.MapFile
import com.github.wled.usage.repository.CrashInstanceRepository
import com.github.wled.usage.repository.CrashReportRepository
import com.github.wled.usage.repository.DeviceRepository
import com.github.wled.usage.repository.MapFileRepository
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class CrashService(
    val crashReportRepository: CrashReportRepository,
    val crashInstanceRepository: CrashInstanceRepository,
    val mapFileRepository: MapFileRepository,
    val deviceRepository: DeviceRepository
) {
    
    /**
     * Process a crash report from an ESP32 device.
     * Creates or updates a unique crash report and records this instance.
     */
    fun processCrashReport(request: CrashReportRequest, countryCode: String?) {
        // Generate a hash of the stack trace to identify unique crashes
        val stackTraceHash = generateStackTraceHash(request.stackTrace)
        
        // Find or create the crash report
        val crashReport = crashReportRepository.findByStackTraceHash(stackTraceHash)
            .orElseGet {
                // Try to decode the stack trace if a map file exists
                val decodedStackTrace = decodeStackTrace(request.stackTrace, request.version)
                
                CrashReport(
                    stackTraceHash = stackTraceHash,
                    rawStackTrace = request.stackTrace,
                    decodedStackTrace = decodedStackTrace,
                    exceptionCause = request.exceptionCause
                )
            }
        
        // Save the crash report
        val savedCrashReport = crashReportRepository.save(crashReport)
        
        // Find the device if deviceId is provided
        val device = request.deviceId?.let { deviceRepository.findById(it).orElse(null) }
        
        // Create a crash instance
        val crashInstance = CrashInstance(
            crashReport = savedCrashReport,
            device = device,
            version = request.version,
            chip = request.chip,
            countryCode = countryCode
        )
        
        crashInstanceRepository.save(crashInstance)
    }
    
    /**
     * Upload a map file for crash decoding.
     */
    fun uploadMapFile(request: MapFileUploadRequest) {
        val existingMapFile = mapFileRepository.findByVersion(request.version)
        
        if (existingMapFile.isPresent) {
            // Update existing map file
            val mapFile = existingMapFile.get()
            val updatedMapFile = mapFile.copy(
                releaseName = request.releaseName,
                chip = request.chip,
                content = request.content
            )
            mapFileRepository.save(updatedMapFile)
        } else {
            // Create new map file
            val mapFile = MapFile(
                version = request.version,
                releaseName = request.releaseName,
                chip = request.chip,
                content = request.content
            )
            mapFileRepository.save(mapFile)
        }
    }
    
    /**
     * Generate a SHA-256 hash of the stack trace.
     * This is used to identify unique crashes.
     */
    private fun generateStackTraceHash(stackTrace: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(stackTrace.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Decode a stack trace using the map file for the given version.
     * This is a simplified implementation - a full decoder would parse ELF files
     * and resolve addresses to function names and line numbers.
     */
    private fun decodeStackTrace(stackTrace: String, version: String): String? {
        val mapFile = mapFileRepository.findByVersion(version).orElse(null) ?: return null
        
        return try {
            // Extract addresses from the stack trace
            val addressPattern = Regex("0x[0-9a-fA-F]{8}")
            val addresses = addressPattern.findAll(stackTrace).map { it.value }.toList()
            
            if (addresses.isEmpty()) {
                return null
            }
            
            val decoded = StringBuilder(stackTrace)
            decoded.append("\n\n=== Decoded Stack Trace ===\n")
            
            // For each address, try to find it in the map file
            addresses.forEach { address ->
                val symbolInfo = findSymbolForAddress(address, mapFile.content)
                if (symbolInfo != null) {
                    decoded.append("$address: $symbolInfo\n")
                } else {
                    decoded.append("$address: <unknown>\n")
                }
            }
            
            decoded.toString()
        } catch (e: Exception) {
            // If decoding fails, return null
            null
        }
    }
    
    /**
     * Find symbol information for a given address in the map file.
     * This is a basic implementation that looks for function names near the address.
     */
    private fun findSymbolForAddress(address: String, mapContent: String): String? {
        try {
            val addressValue = address.substring(2).toLong(16)
            
            // Look for lines in the map file that might contain symbol information
            // Map files typically have format: address symbol_name
            val lines = mapContent.lines()
            var closestSymbol: String? = null
            var closestDistance = Long.MAX_VALUE
            
            for (line in lines) {
                // Try to find address patterns in the map file
                val mapAddressPattern = Regex("0x[0-9a-fA-F]+|[0-9a-fA-F]{8}")
                val match = mapAddressPattern.find(line) ?: continue
                
                val mapAddress = try {
                    if (match.value.startsWith("0x")) {
                        match.value.substring(2).toLong(16)
                    } else {
                        match.value.toLong(16)
                    }
                } catch (e: Exception) {
                    continue
                }
                
                // If this address is before our target address and closer than previous
                if (mapAddress <= addressValue) {
                    val distance = addressValue - mapAddress
                    if (distance < closestDistance) {
                        closestDistance = distance
                        // Extract the symbol name from the rest of the line
                        val parts = line.trim().split(Regex("\\s+"))
                        if (parts.size > 1) {
                            closestSymbol = parts.drop(1).joinToString(" ")
                        }
                    }
                }
            }
            
            return if (closestSymbol != null && closestDistance < 0x1000) {
                "$closestSymbol+0x${closestDistance.toString(16)}"
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }
}
