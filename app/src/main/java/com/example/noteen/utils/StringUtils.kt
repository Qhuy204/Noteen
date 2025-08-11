package com.example.noteen.utils

import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val json = "{\"type\":\"doc\",\"content\":[{\"type\":\"image\",\"attrs\":{\"src\":\"https://myapp.local/external/1000003986.jpg\",\"alt\":null,\"title\":null,\"width\":null,\"height\":null}},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"Đây là kỷ niệm \"},{\"type\":\"text\",\"marks\":[{\"type\":\"highlight\"}],\"text\":\"gánh team kinh khủng tởm\"},{\"type\":\"text\",\"text\":\" của \"},{\"type\":\"text\",\"marks\":[{\"type\":\"code\"}],\"text\":\"Chườn-cun\"}]},{\"type\":\"table\",\"content\":[{\"type\":\"tableRow\",\"content\":[{\"type\":\"tableHeader\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"Column 1\"}]}]},{\"type\":\"tableHeader\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"Column 2\"}]}]},{\"type\":\"tableHeader\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"Column 3\"}]}]}]},{\"type\":\"tableRow\",\"content\":[{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}]},{\"type\":\"tableRow\",\"content\":[{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}]},{\"type\":\"tableRow\",\"content\":[{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}]},{\"type\":\"tableRow\",\"content\":[{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}]},{\"type\":\"tableRow\",\"content\":[{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]},{\"type\":\"tableCell\",\"attrs\":{\"colspan\":1,\"rowspan\":1,\"colwidth\":null},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}]}]},{\"type\":\"taskList\",\"content\":[{\"type\":\"taskItem\",\"attrs\":{\"checked\":false},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"Yes\"}]}]},{\"type\":\"taskItem\",\"attrs\":{\"checked\":false},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"No\"}]}]},{\"type\":\"taskItem\",\"attrs\":{\"checked\":true},\"content\":[{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null},\"content\":[{\"type\":\"text\",\"text\":\"Not sure\"}]}]}]},{\"type\":\"codeBlock\",\"attrs\":{\"language\":null}},{\"type\":\"paragraph\",\"attrs\":{\"textAlign\":null}}]}"

val json1 = "Đây là cách bạn có thể tạo **hiệu ứng xuất hiện rõ dần (fade-in)** cho một `Box` trong Jetpack Compose:\n" +
        "\n" +
        "---\n" +
        "\n" +
        "### ✅ **Cách đơn giản với `AnimatedVisibility` + `fadeIn()`**\n" +
        "\n" +
        "```kotlin\n" +
        "@Composable\n" +
        "fun FadeInBoxExample() {\n" +
        "    var visible by remember { mutableStateOf(false) }\n" +
        "\n" +
        "    // Bấm để bật/tắt\n" +
        "    LaunchedEffect(Unit) {\n" +
        "        delay(500) // Chờ 0.5s rồi mới hiển thị\n" +
        "        visible = true\n" +
        "    }\n" +
        "\n" +
        "    AnimatedVisibility(\n" +
        "        visible = visible,\n" +
        "        enter = fadeIn(animationSpec = tween(durationMillis = 1000)) // 1 giây\n" +
        "    ) {\n" +
        "        Box(\n" +
        "            modifier = Modifier\n" +
        "                .size(200.dp)\n" +
        "                .background(Color.Red)\n" +
        "        )\n" +
        "    }\n" +
        "}\n" +
        "```\n" +
        "\n" +
        "---\n" +
        "\n" +
        "### \uD83D\uDCCC Nếu bạn muốn **toàn quyền kiểm soát alpha** bằng `Animatable`\n" +
        "\n" +
        "```kotlin\n" +
        "@Composable\n" +
        "fun FadeInBoxManual() {\n" +
        "    val alpha = remember { Animatable(0f) }\n" +
        "\n" +
        "    LaunchedEffect(Unit) {\n" +
        "        alpha.animateTo(\n" +
        "            targetValue = 1f,\n" +
        "            animationSpec = tween(durationMillis = 1000)\n" +
        "        )\n" +
        "    }\n" +
        "\n" +
        "    Box(\n" +
        "        modifier = Modifier\n" +
        "            .size(200.dp)\n" +
        "            .graphicsLayer { this.alpha = alpha.value }\n" +
        "            .background(Color.Blue)\n" +
        "    )\n" +
        "}\n" +
        "```\n" +
        "\n" +
        "---\n" +
        "\n" +
        "### \uD83D\uDCDD Gợi ý dùng cái nào?\n" +
        "\n" +
        "| Mục đích                      | Gợi ý dùng                           |\n" +
        "| ----------------------------- | ------------------------------------ |\n" +
        "| Hiệu ứng đơn giản, rõ ràng    | `AnimatedVisibility` + `fadeIn()`    |\n" +
        "| Tùy chỉnh alpha, kết hợp thêm | `Animatable` + `graphicsLayer.alpha` |\n" +
        "\n" +
        "---\n" +
        "\n" +
        "Bạn muốn thêm các hiệu ứng khác như **di chuyển, phóng to nhỏ, scale** khi xuất hiện không? Mình có thể mở rộng cho bạn.\n"

val json2 = "Đoạn mã bạn cung cấp là một `object` singleton có tên `TextEditorEngine`, đóng vai trò là một engine cầu nối giữa Jetpack Compose và một WebView chứa trình soạn thảo văn bản (có lẽ là một editor như TipTap, Quill, v.v.) chạy trong WebView được load từ `assets`.\n" +
        "\n" +
        "### \uD83C\uDFAF Câu hỏi của bạn:\n" +
        "\n" +
        "> Có cấu hình nào trong `TextEditorEngine` khiến quá trình layout với UI của Compose bị chậm khi render `WebView` không?\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## ✅ Tổng kết nhanh:\n" +
        "\n" +
        "Có **một số cấu hình và thiết kế hiện tại** có **tiềm năng gây ảnh hưởng đến hiệu suất** trong quá trình render hoặc layout `WebView` trong UI của Compose. Dưới đây là các điểm bạn cần xem xét và cách tối ưu.\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## ⚠\uFE0F 1. **Dùng `WebView` không gắn vào View Hierarchy Compose đúng cách**\n" +
        "\n" +
        "Bạn đang khởi tạo `WebView` trong `TextEditorEngine` dưới dạng singleton (tĩnh) và không gắn trực tiếp vào Compose qua `AndroidView`:\n" +
        "\n" +
        "```kotlin\n" +
        "_webView = WebView(appContext)\n" +
        "```\n" +
        "\n" +
        "➡\uFE0F **Vấn đề**: Dùng `applicationContext` khi khởi tạo `WebView` là không phù hợp để render trong giao diện. `WebView` nên được gắn vào `Activity Context`, và được tạo trong `AndroidView` để Compose có thể layout hiệu quả.\n" +
        "\n" +
        "> ❌ **`applicationContext` + `WebView` không thể đo đạc và render hợp lý khi Compose layout lại.**\n" +
        "\n" +
        "### ✅ Gợi ý tối ưu:\n" +
        "\n" +
        "* Không nên giữ `WebView` trong một singleton `object`.\n" +
        "* Thay vào đó, tạo `WebView` bên trong `AndroidView` Compose như sau:\n" +
        "\n" +
        "```kotlin\n" +
        "AndroidView(\n" +
        "    factory = {\n" +
        "        WebView(it).apply {\n" +
        "            // Setup tại đây\n" +
        "        }\n" +
        "    },\n" +
        "    update = { webView ->\n" +
        "        // Cập nhật nội dung nếu cần\n" +
        "    },\n" +
        "    modifier = Modifier.fillMaxSize()\n" +
        ")\n" +
        "```\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## ⚠\uFE0F 2. **Trạng thái toàn cục bằng `mutableStateOf` có thể gây recomposition không cần thiết**\n" +
        "\n" +
        "Bạn đang dùng `mutableStateOf` để lưu:\n" +
        "\n" +
        "* `jsonContent`\n" +
        "* `plainTextContent`\n" +
        "* `titleString`\n" +
        "* `buttonStatesJson`\n" +
        "* `undoStatesJson`\n" +
        "* `shouldShowToolbar`\n" +
        "\n" +
        "➡\uFE0F **Vấn đề**:\n" +
        "\n" +
        "* Nếu những state này bị cập nhật quá thường xuyên từ WebView qua JavaScript bridge, Compose UI sẽ liên tục recompose.\n" +
        "* Đặc biệt khi `buttonStatesJson` hoặc `undoStatesJson` thay đổi mỗi lần người dùng gõ phím trong WebView, điều này có thể gây lag hoặc jank.\n" +
        "\n" +
        "### ✅ Gợi ý tối ưu:\n" +
        "\n" +
        "* **Không cần dùng `State` nếu chỉ lưu trữ nội bộ** và không liên kết trực tiếp với giao diện.\n" +
        "* Chỉ dùng `mutableStateOf` nếu **UI phụ thuộc trực tiếp vào giá trị đó**.\n" +
        "\n" +
        "> ✅ Có thể thay thế một số state bằng biến thường (`var`) hoặc kết hợp với `LaunchedEffect`/`derivedStateOf`.\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## ⚠\uFE0F 3. `WebView` được khởi tạo và load nội dung **ngay từ đầu**\n" +
        "\n" +
        "Bạn đang `loadUrl()` trong `init()` khi `WebView` vừa khởi tạo:\n" +
        "\n" +
        "```kotlin\n" +
        "loadUrl(\"https://appassets.androidplatform.net/index.html\")\n" +
        "```\n" +
        "\n" +
        "➡\uFE0F **Vấn đề**:\n" +
        "\n" +
        "* Việc load HTML từ `assets` trong khi chưa hiển thị WebView có thể chặn layout hoặc gây delay nếu WebView chưa thực sự được render trong giao diện.\n" +
        "\n" +
        "### ✅ Gợi ý tối ưu:\n" +
        "\n" +
        "* Đợi WebView được hiển thị trên UI (hoặc gọi từ `LaunchedEffect` sau khi `AndroidView` được hiển thị).\n" +
        "* Không nên load nội dung nếu `WebView` chưa nằm trong hierarchy.\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## ⚠\uFE0F 4. Không dùng `WebView.setLayerType` dẫn đến hiệu năng chậm khi render nội dung phức tạp\n" +
        "\n" +
        "➡\uFE0F **Vấn đề**:\n" +
        "\n" +
        "* WebView mặc định sẽ sử dụng phần cứng (`hardware acceleration`) nếu khả dụng, nhưng có thể bị tắt trong một số thiết bị/lỗi.\n" +
        "* Khi rendering biểu thức toán học hoặc rich content, `software rendering` sẽ rất chậm.\n" +
        "\n" +
        "### ✅ Gợi ý tối ưu:\n" +
        "\n" +
        "```kotlin\n" +
        "_webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)\n" +
        "```\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## ✅ Tổng kết: Những điểm cần làm để tối ưu WebView + Compose\n" +
        "\n" +
        "| Vấn đề                                                   | Cách tối ưu                                   |\n" +
        "| -------------------------------------------------------- | --------------------------------------------- |\n" +
        "| ❌ Dùng WebView singleton với `applicationContext`        | ✅ Tạo WebView trực tiếp trong `AndroidView`   |\n" +
        "| ❌ Dùng `mutableStateOf` cho mọi dữ liệu                  | ✅ Giới hạn state chỉ ở phần Compose phụ thuộc |\n" +
        "| ❌ Load WebView sớm                                       | ✅ Chờ WebView hiển thị trước khi `loadUrl()`  |\n" +
        "| ❌ Không set layer type                                   | ✅ Gọi `setLayerType(LAYER_TYPE_HARDWARE)`     |\n" +
        "| ❌ Giao tiếp WebView quá thường xuyên (JS interface spam) | ✅ Giới hạn tần suất update từ WebView         |\n" +
        "\n" +
        "---\n" +
        "\n" +
        "## \uD83C\uDFAF Bạn nên làm gì tiếp theo?\n" +
        "\n" +
        "* Di chuyển khởi tạo `WebView` vào `AndroidView(factory = { WebView(...) })`\n" +
        "* Chuyển toàn bộ logic `init()` và bridge vào bên trong `factory`\n" +
        "* Dùng `remember` hoặc `rememberWebViewWithLifecycle()` (từ accompanist nếu có)\n" +
        "* Giảm tần suất giao tiếp JS (có debounce hoặc chỉ cập nhật state thật sự cần thiết)\n" +
        "\n" +
        "---\n" +
        "\n" +
        "Nếu bạn cần, mình có thể **viết lại phần `AndroidView` chuẩn trong Compose** để thay thế logic hiện tại bạn đang dùng với `TextEditorEngine`. Bạn có muốn điều đó không?\n"

fun String.escapedForJS(): String {
    return JSONObject.quote(this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatNoteDate(dateString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)
        val inputDate = dateTime.toLocalDate()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        when {
            inputDate == today -> "Today ${dateTime.format(timeFormatter)}"
            inputDate == yesterday -> "Yesterday ${dateTime.format(timeFormatter)}"
            inputDate.year == today.year -> dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
            else -> dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    } catch (e: Exception) {
        "Invalid date"
    }
}

