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
import android.widget.ImageView
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var strokeInput: EditText
    private lateinit var strokeResultText: TextView

    private val names = listOf("大安", "留连", "速喜", "赤口", "小吉", "空亡")
    private val descriptions = listOf(
        "静止，心安，吉祥之意。",
        "晦暗不明，拖延，纠缠。",
        "快速，喜庆，时机已到。",
        "惊恐，凶险，口舌是非。",
        "和合顺遂，好事渐近。",
        "忧虑，不吉，无结果。"
    )
    private val advice = listOf(
        "宜：规划、稳步推进、处理日常事务。\n不宜：急于求成、频繁变动。",
        "宜：复盘、等待信息、处理收尾工作。\n不宜：仓促决定、催促尚未成熟的事情。",
        "宜：沟通、拜访、表达想法、推进有时效的事项。\n不宜：因一时兴奋而忽略细节。",
        "宜：谨慎核对、冷静沟通、避免争执。\n不宜：冲动谈判、口头争辩、冒险行事。",
        "宜：合作、协调、拜访朋友、推进人际事务。\n不宜：忽视流程或过度依赖他人承诺。",
        "宜：整理计划、核实信息、暂缓重大决定。\n不宜：盲目投入、贸然承诺或冲动消费。"
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

        // 小六壬手掌图放在首页最上方，按比例缩放，避免拉伸。
        root.addView(ImageView(this).apply {
            setImageResource(com.example.xiaoliuren.R.drawable.shouzhang)
            contentDescription = "小六壬六宫手掌图"
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setPadding(0, 0, 0, dp(12))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(300)))

        root.addView(TextView(this).apply {
            text = "吉时测算"
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

        root.addView(TextView(this).apply {
            text = "笔画测算"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(0xFF27352B.toInt())
            setPadding(0, dp(18), 0, dp(6))
        })
        root.addView(TextView(this).apply {
            text = "输入汉字后自动统计笔画，并按总笔画除以 6 取余。"
            textSize = 14f
            setTextColor(0xFF68736A.toInt())
        })
        strokeInput = EditText(this).apply {
            hint = "请输入要测算的文字，例如：平安顺遂"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            textSize = 16f
            setSingleLine(true)
        }
        root.addView(strokeInput, fullParams())
        strokeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { calculateStrokes() }
            override fun afterTextChanged(s: Editable?) {}
        })
        strokeResultText = TextView(this).apply {
            textSize = 16f
            setTextColor(0xFF27352B.toInt())
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        root.addView(strokeResultText, fullParams())

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
                explanationText.text = "$name：${descriptions[index]}\n\n${advice[index]}"
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
        calculationText.text = "计算过程\n（月 + 日 + 时）- 2\n= ($month + $day + $hourBranch) - 2\n= $total\n$total ÷ 6，余数 $remainder\n\n余数对应：${remainder} = ${names[index]}"

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


    // 常用汉字笔画表。遇到表外汉字时会提示，不会猜测笔画数。
    private val strokeMap: Map<Char, Int> = buildMap {
        val entries = mapOf(
            1 to "一乙", 2 to "二十丁厂七卜人入八九几了力乃刀又",
            3 to "三干于亏士工土才寸下大丈与万上小口山巾千乞川亿个勺久么义丸夕",
            4 to "不中丰王开井天夫无元云专丐扎艺木五支厅比互切瓦止少日曰中贝水火文方六为斗忆订计",
            5 to "玉正生用由田甲申白目石示必民平东业本术禾半句古右可左占业册冬鸟乐",
            6 to "年朱先舌竹米羊并关兴字安守宅宇百有存而页至此光当早吐回因吃各名多色齐衣充产交亥",
            7 to "里李我你他她住作低体何但位伴伸近还进远连运花苏芳芦芯声走足串男困园围时明吧听呢别",
            8 to "国学金林松果空雨青事来到长门间明知画直和奇放法波注河油治沿泪浅服朋股",
            9 to "语说要是美重看春秋香星亮度思急品咬哈响相查树面风飞食信",
            10 to "家高海笑请起能原留时病真书铁钱热爱旅展特校珠酒消流浪",
            11 to "做得清理常眼船猫猪银动情现黄梦雪章麻菜爽深细累",
            12 to "顺喜然最量道发晴智景富童等笔画黑答",
            13 to "新照想意感福爱解满路跳数",
            14 to "歌算静", 15 to "德趣", 16 to "懂", 17 to "声"
        )
        entries.forEach { (count, chars) -> chars.forEach { put(it, count) } }
    }

    private fun calculateStrokes() {
        val raw = strokeInput.text.toString().trim()
        if (raw.isEmpty()) {
            strokeResultText.text = "输入文字后，将自动计算总笔画和测算结果。"
            return
        }
        val chars = raw.filter { it in strokeMap.keys }
        val unknown = raw.filter { it.isLetter() && it !in strokeMap.keys }.distinct()
        if (chars.isEmpty() || unknown.isNotEmpty()) {
            strokeResultText.text = if (unknown.isNotEmpty()) {
                "暂时无法自动识别这些字的笔画：${unknown.joinToString("、")}\n请先使用常用汉字，或补充这些字的笔画数据。"
            } else {
                "请输入汉字进行测算。"
            }
            return
        }
        val details = chars.map { "$it(${strokeMap[it]}画)" }
        val total = chars.sumOf { strokeMap[it] ?: 0 }
        val remainder = total % 6
        val index = remainderToIndex[remainder] ?: 5
        strokeResultText.text = "测算文字：${chars.joinToString("")}\n笔画明细：${details.joinToString(" + ")}\n总笔画：$total\n计算：$total ÷ 6，余数 $remainder\n结果：${names[index]}\n${descriptions[index]}\n\n${advice[index]}\n\n提示：汉字笔画按内置常用字表统计，民俗结果仅供参考。"
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
