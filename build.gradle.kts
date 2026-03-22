import com.gtnewhorizons.retrofuturagradle.minecraft.RunMinecraftTask
import java.util.regex.Pattern

plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

// workaround an OOM on GH actions during the checkStyleMain task
tasks.withType<Checkstyle>().configureEach {
    maxHeapSize = "1g"
}

tasks.test.configure {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

fun parseGtVersion(versionStr: String): Triple<Int, Int, Int> {
    val components = versionStr.split(Pattern.compile("[.-]"), 5)
    val vMajor = 500 + components[1].toInt()
    val vMinor = components[2].toInt()
    val vPatch = components[3].toInt()
    return Triple(vMajor, vMinor, vPatch)
}

var gtNbtVersion: Triple<Int, Int, Int> = Triple(509, 44, 0)
var logLevel = LogLevel.INFO
try {
    val verStr = project.version.toString()
    gtNbtVersion = parseGtVersion(verStr)
} catch (_: Exception) {
    try {
        val verStr = providers.exec {
            commandLine("git", "describe")
        }.standardOutput.asText.get()
        gtNbtVersion = parseGtVersion(verStr)
        logLevel = LogLevel.LIFECYCLE
    } catch (_: Exception) {
        logger.error("Cannot automatically determine NBT version. Using defaults hardcoded in buildscript. This could break your world!")
        logLevel = LogLevel.WARN
    }
}

minecraft {
    injectedTags.put("VERSION_MAJOR", gtNbtVersion.first)
    injectedTags.put("VERSION_MINOR", gtNbtVersion.second)
    injectedTags.put("VERSION_PATCH", gtNbtVersion.third)
    logger.log(logLevel, "Using {} as NBT version", gtNbtVersion)
}