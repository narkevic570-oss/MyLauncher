package app.mylauncher

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Вход через Microsoft: device code -> Xbox Live -> XSTS -> Minecraft.
// Нужен Client ID приложения Azure, одобренный Mojang (aka.ms/mce-reviewappid).
object MsAuth {
    private const val SCOPE = "XboxLive.signin offline_access"
    private const val BASE = "https://login.microsoftonline.com/consumers/oauth2/v2.0"
    private const val FORM = "application/x-www-form-urlencoded"
    private const val JSON = "application/json"

    class DeviceCode(
        val deviceCode: String,
        val userCode: String,
        val url: String,
        val interval: Int,
        val expiresIn: Int
    )

    private fun form(vararg p: Pair<String, String>): String =
        p.joinToString("&") { URLEncoder.encode(it.first, "UTF-8") + "=" + URLEncoder.encode(it.second, "UTF-8") }

    private fun request(
        method: String, url: String, body: String?, contentType: String?, bearer: String? = null
    ): Pair<Int, String> {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = method
        c.connectTimeout = 15000
        c.readTimeout = 30000
        c.setRequestProperty("Accept", "application/json")
        if (bearer != null) c.setRequestProperty("Authorization", "Bearer $bearer")
        if (body != null) {
            c.doOutput = true
            c.setRequestProperty("Content-Type", contentType ?: JSON)
            c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
        return code to text
    }

    private fun errorText(text: String, fallback: String): String = try {
        val j = JSONObject(text)
        j.optString("error_description").ifBlank { j.optString("error", fallback) }
    } catch (e: Exception) {
        fallback
    }

    fun requestDeviceCode(clientId: String): DeviceCode {
        val (code, text) = request(
            "POST", "$BASE/devicecode",
            form("client_id" to clientId, "scope" to SCOPE), FORM
        )
        if (code != 200) throw Exception(errorText(text, "Не удалось получить код входа"))
        val j = JSONObject(text)
        return DeviceCode(
            j.getString("device_code"), j.getString("user_code"), j.getString("verification_uri"),
            j.optInt("interval", 5), j.optInt("expires_in", 900)
        )
    }

    // Возвращает пару (access_token, refresh_token).
    fun pollToken(clientId: String, dc: DeviceCode, cancelled: () -> Boolean): Pair<String, String> {
        var interval = dc.interval
        val deadline = System.currentTimeMillis() + dc.expiresIn * 1000L
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(interval * 1000L)
            if (cancelled()) throw Exception("cancelled")
            val (code, text) = request(
                "POST", "$BASE/token",
                form(
                    "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
                    "client_id" to clientId,
                    "device_code" to dc.deviceCode
                ), FORM
            )
            val j = JSONObject(text)
            if (code == 200) return j.getString("access_token") to j.optString("refresh_token")
            when (j.optString("error")) {
                "authorization_pending" -> {}
                "slow_down" -> interval += 5
                else -> throw Exception(errorText(text, "Вход отклонён"))
            }
        }
        throw Exception("Время ввода кода истекло")
    }

    fun refresh(clientId: String, refreshToken: String): Pair<String, String> {
        val (code, text) = request(
            "POST", "$BASE/token",
            form(
                "grant_type" to "refresh_token",
                "client_id" to clientId,
                "refresh_token" to refreshToken,
                "scope" to SCOPE
            ), FORM
        )
        if (code != 200) throw Exception(errorText(text, "Нужно войти заново"))
        val j = JSONObject(text)
        return j.getString("access_token") to j.optString("refresh_token", refreshToken)
    }

    fun xboxChain(ms: String, refreshToken: String): Account {
        val xblBody = JSONObject()
            .put(
                "Properties",
                JSONObject().put("AuthMethod", "RPS").put("SiteName", "user.auth.xboxlive.com")
                    .put("RpsTicket", "d=$ms")
            )
            .put("RelyingParty", "http://auth.xboxlive.com")
            .put("TokenType", "JWT").toString()
        val (c1, t1) = request("POST", "https://user.auth.xboxlive.com/user/authenticate", xblBody, JSON)
        if (c1 != 200) throw Exception("Ошибка Xbox Live ($c1)")
        val xblToken = JSONObject(t1).getString("Token")

        val xstsBody = JSONObject()
            .put(
                "Properties",
                JSONObject().put("SandboxId", "RETAIL").put("UserTokens", JSONArray().put(xblToken))
            )
            .put("RelyingParty", "rp://api.minecraftservices.com/")
            .put("TokenType", "JWT").toString()
        val (c2, t2) = request("POST", "https://xsts.auth.xboxlive.com/xsts/authorize", xstsBody, JSON)
        if (c2 != 200) {
            val xerr = try { JSONObject(t2).optLong("XErr") } catch (e: Exception) { 0L }
            throw Exception(
                when (xerr) {
                    2148916233L -> "У этого Microsoft-аккаунта нет профиля Xbox. Создай его на xbox.com и повтори."
                    2148916238L -> "Детский аккаунт: его нужно добавить в семейную группу Microsoft."
                    else -> "Ошибка XSTS ($c2)"
                }
            )
        }
        val xsts = JSONObject(t2)
        val uhs = xsts.getJSONObject("DisplayClaims").getJSONArray("xui").getJSONObject(0).getString("uhs")
        val xstsToken = xsts.getString("Token")

        val mcBody = JSONObject().put("identityToken", "XBL3.0 x=$uhs;$xstsToken").toString()
        val (c3, t3) = request(
            "POST", "https://api.minecraftservices.com/authentication/login_with_xbox", mcBody, JSON
        )
        if (c3 != 200) {
            throw Exception(
                if (t3.contains("Invalid app registration"))
                    "Mojang не одобрил этот Client ID (Invalid app registration)."
                else "Ошибка входа в Minecraft ($c3)"
            )
        }
        val mcToken = JSONObject(t3).getString("access_token")

        val (c4, t4) = request("GET", "https://api.minecraftservices.com/minecraft/profile", null, null, mcToken)
        if (c4 == 404) throw Exception("На этом аккаунте не куплен Minecraft Java Edition.")
        if (c4 != 200) throw Exception("Не удалось получить профиль ($c4)")
        val p = JSONObject(t4)
        val id = p.getString("id").replaceFirst(
            Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"), "\$1-\$2-\$3-\$4-\$5"
        )
        return Account("microsoft", p.getString("name"), id, mcToken, refreshToken)
    }
}
