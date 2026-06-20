package com.nox.app.utils

object BlockedDomains {

    // Comprehensive list of blocked adult/pornographic domains
    val blockedDomains = setOf(
        // Major adult sites
        "pornhub.com", "www.pornhub.com",
        "xvideos.com", "www.xvideos.com",
        "xnxx.com", "www.xnxx.com",
        "xhamster.com", "www.xhamster.com",
        "redtube.com", "www.redtube.com",
        "youporn.com", "www.youporn.com",
        "tube8.com", "www.tube8.com",
        "pornhub.net",
        "brazzers.com", "www.brazzers.com",
        "bangbros.com",
        "naughtyamerica.com",
        "realitykings.com",
        "mofos.com",
        "twistys.com",
        "digitalplayground.com",
        "adultfriendfinder.com",
        "ashleymadison.com",
        "onlyfans.com", "www.onlyfans.com",
        "fansly.com",
        "chaturbate.com", "www.chaturbate.com",
        "cam4.com",
        "livejasmin.com",
        "myfreecams.com",
        "stripchat.com",
        "bongacams.com",
        "jasmin.com",
        "camsoda.com",
        "flirt4free.com",
        "streamate.com",

        // Search bypass attempts
        "rule34.xxx",
        "e621.net",
        "gelbooru.com",
        "sankakucomplex.com",
        "nhentai.net",
        "hentai2read.com",
        "fakku.net",

        // Adult content CDNs
        "phncdn.com",
        "xvideos-cdn.com",
        "xnxx-cdn.com",

        // Arabic adult sites
        "aflam-sxs.com",
        "arabsex.com",
        "xarabic.com",
        "sex-arab.com",
        "arab-sex.net",
    )

    // Blocked apps package names
    val blockedApps = setOf(
        "com.onlyfans.onlyfans",
        "com.chaturbate.chaturbate",
        "mobi.porn",
        "com.xnxx.xnxxapp",
        "com.xvideo.xvideo",
        "com.pornhub.pornhub",
        "air.com.adultempiremedia.adriveapp",
        "com.fling.fling"
    )

    fun isDomainBlocked(domain: String): Boolean {
        val lowerDomain = domain.lowercase().trim()
        return blockedDomains.any { blocked ->
            lowerDomain == blocked || lowerDomain.endsWith(".$blocked")
        }
    }

    fun isAppBlocked(packageName: String): Boolean {
        return blockedApps.contains(packageName)
    }
}
