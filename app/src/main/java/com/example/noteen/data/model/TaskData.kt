package com.example.noteen.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class MainTask(
    @SerializedName("title") val title: String,
    @SerializedName("isCompleted") val isCompleted: Boolean,
    @SerializedName("subtasks") val subtasks: List<SubTask>
)

data class SubTask(
    @SerializedName("title") val title: String,
    @SerializedName("isCompleted") val isCompleted: Boolean
)

fun parseMainTasks(jsonString: String): List<MainTask> {
    val type = object : TypeToken<List<MainTask>>() {}.type
    return Gson().fromJson(jsonString, type)
}

// 1 nhiệm vụ
val jsonString1 = """
[
  {
    "title": "Dọn phòng",
    "isCompleted": false,
    "subtasks": []
  }
]
""".trimIndent()

// 3 nhiệm vụ
val jsonString3 = """
[
  {
    "title": "Học Compose",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Đi siêu thị",
    "isCompleted": true,
    "subtasks": []
  },
  {
    "title": "Đọc sách 30 phút",
    "isCompleted": false,
    "subtasks": []
  }
]
""".trimIndent()

// 5 nhiệm vụ
val jsonString5 = """
[
  {
    "title": "Học Kotlin",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Viết API login",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Fix bug màn hình Home",
    "isCompleted": true,
    "subtasks": []
  },
  {
    "title": "Đi chợ",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Nấu ăn tối",
    "isCompleted": false,
    "subtasks": []
  }
]
""".trimIndent()

// 6 nhiệm vụ
val jsonString6 = """
[
  {
    "title": "Làm báo cáo tuần",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Học Jetpack Compose",
    "isCompleted": true,
    "subtasks": []
  },
  {
    "title": "Đi họp dự án",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Tập thể dục",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Dọn nhà bếp",
    "isCompleted": false,
    "subtasks": []
  },
  {
    "title": "Mua quà sinh nhật",
    "isCompleted": false,
    "subtasks": []
  }
]
""".trimIndent()
