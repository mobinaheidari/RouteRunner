pluginManagement {
    repositories {
        // --- مخازن کمکی (ضد تحریم) برای پلاگین‌ها ---
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // --- مخازن اصلی (به عنوان پشتیبان) ---
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // --- مخازن کمکی (ضد تحریم) برای کتابخانه‌ها ---
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }

        // --- مخازن اصلی (به عنوان پشتیبان) ---
        google()
        mavenCentral()
        // مخزن Jitpack هم معمولاً لازم می‌شود، اگر خواستید فعالش کنید:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RouteRunner"
include(":app")
include(":location")
