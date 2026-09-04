package point.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform