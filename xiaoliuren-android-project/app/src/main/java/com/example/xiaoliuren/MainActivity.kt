package com.example.xiaoliuren

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.nlf.calendar.Lunar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : Activity() {
    private var selectedDateTime: LocalDateTime = LocalDateTime.now()
    private lateinit var dateTimeText: TextView
    private lateinit var lunarText: TextView
    private lateinit var resultText: TextView
    private lateinit var calculationText: TextView
    private lateinit var explanationText: TextView
    private lateinit var resultButtons: LinearLayout

    private val names = listOf("大安", "留连", "速喜", "赤口", "小吉", "空亡")
    private val descriptions = listOf(
        "静止，心安，吉祥之意。",
        "晦暗不明，拖延，纠缠。",
        "快速，喜庆，时机已到。",
        "惊恐，凶险，口舌是非。",
        "家，回归，好运即将到来。",
        "忧虑，不吉，无结果。"
    )
    // 图片中的余数映射：1大安、2留连、3速喜、4赤口、5小吉、0空亡
    private val remainderToIndex = mapOf(1 to 0, 2 to 1, 3 to 2, 4 to 3, 5 to 4, 0 to 5)
    private val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        calculate()
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(28))
            setBackgroundColor(0xFFF7F5F0.toInt())
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(TextView(this).apply {
            text = "小六壬测算"
            textSize = 28f
            setTextColor(0xFF27352B.toInt())
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "按农历月、日与十二时辰计算"
            textSize = 14f
            setTextColor(0xFF68736A.toInt())
            setPadding(0, dp(4), 0, dp(18))
        })

        dateTimeText = TextView(this).apply {
            textSize = 18f
            setTextColor(0xFF202820.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(dateTimeText)

        val pickRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        pickRow.addView(makeButton("选择日期") { pickDate() }, weightedParams())
        pickRow.addView(makeButton("选择时间") { pickTime() }, weightedParams())
        root.addView(pickRow)

        root.addView(makeButton("使用当前时间") {
            selectedDateTime = LocalDateTime.now()
            calculate()
        }.apply {
            setBackgroundColor(0xFF356B4A.toInt())
            setTextColor(0xFFFFFFFF.toInt())
        }, fullParams())

        lunarText = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFF3C5142.toInt())
            setPadding(0, dp(14), 0, dp(10))
        }
        root.addView(lunarText)

        resultText = TextView(this).apply {
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(0xFF356B4A.toInt())
            setPadding(0, dp(14), 0, dp(6))
        }
        root.addView(resultText)

        explanationText = TextView(this).apply {
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(0xFF39433A.toInt())
            setPadding(0, 0, 0, dp(14))
        }
        root.addView(explanationText)

        calculationText = TextView(this).apply {
            textSize = 15f
            setTextColor(0xFF343A35.toInt())
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        root.addView(calculationText, fullParams())

        root.addView(TextView(this).apply {
            text = "六种结果说明（点击查看）"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF27352B.toInt())
            setPadding(0, dp(22), 0, dp(10))
        })

        resultButtons = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(resultButtons, fullParams())
        names.forEachIndexed { index, name ->
            val button = makeButton("$name  ·  ${descriptions[index]}") {
                explanationText.text = "$name：${descriptions[index]}"
                // 点击只查看该结果的解释，不覆盖当前时间计算出的命中结果。
            }
            button.tag = index
            resultButtons.addView(button, fullParams())
        }

        root.addView(TextView(this).apply {
            text = "说明：本工具按图中公式进行民俗文化计算，仅供个人参考与娱乐。"
            textSize = 12f
            setTextColor(0xFF7B817B.toInt())
            setPadding(0, dp(20), 0, 0)
        })
    }

    private fun calculate() {
        val lunar = Lunar.fromDate(java.util.Date.from(
            selectedDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
        ))
        val month = lunar.month
        val day = lunar.day
        val hourBranch = hourBranchNumber(selectedDateTime.hour)
        val hourName = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")[(hourBranch - 1)]
        val total = month + day + hourBranch - 2
        val remainder = ((total % 6) + 6) % 6
        val index = remainderToIndex[remainder] ?: 5

        dateTimeText.text = "公历：${selectedDateTime.format(formatter)}"
        lunarText.text = "农历：${lunar.year}年${lunar.month}月${lunar.day}日  ·  ${hourName}时（时序 $hourBranch）"
        resultText.text = names[index]
        explanationText.text = descriptions[index]
        calculationText.text = "计算过程\\n（月 + 日 + 时）- 2\\n= ($month + $day + $hourBranch) - 2\\n= $total\\n$total ÷ 6，余数 $remainder\\n\\n余数对应：${remainder} = ${names[index]}"

        for (i in 0 until resultButtons.childCount) {
            val button = resultButtons.getChildAt(i) as Button
            if (i == index) {
                button.setBackgroundColor(0xFF356B4A.toInt())
                button.setTextColor(0xFFFFFFFF.toInt())
            } else {
                button.setBackgroundColor(0xFFE6EAE4.toInt())
                button.setTextColor(0xFF27352B.toInt())
            }
        }
    }

    // 子时=1，丑时=2……亥时=12。按传统双小时区间划分，23:00至00:59为子时。
    private fun hourBranchNumber(hour: Int): Int = when (hour) {
        23, 0 -> 1
        1, 2 -> 2
        3, 4 -> 3
        5, 6 -> 4
        7, 8 -> 5
        9, 10 -> 6
        11, 12 -> 7
        13, 14 -> 8
        15, 16 -> 9
        17, 18 -> 10
        19, 20 -> 11
        else -> 12 // 21:00、22:00
    }

    private fun pickDate() {
        DatePickerDialog(this, { _, year, month, day ->
            selectedDateTime = selectedDateTime.withYear(year).withMonth(month + 1).withDayOfMonth(day)
            calculate()
        }, selectedDateTime.year, selectedDateTime.monthValue - 1, selectedDateTime.dayOfMonth).show()
    }

    private fun pickTime() {
        TimePickerDialog(this, { _, hour, minute ->
            selectedDateTime = selectedDateTime.withHour(hour).withMinute(minute)
            calculate()
        }, selectedDateTime.hour, selectedDateTime.minute, true).show()
    }

    private fun makeButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun fullParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(8) }
    private fun weightedParams() = LinearLayout.LayoutParams(0, dp(52), 1f).apply {
        marginEnd = dp(6)
        bottomMargin = dp(8)
    }
}
