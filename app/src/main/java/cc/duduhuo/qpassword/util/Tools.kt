package cc.duduhuo.qpassword.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import android.view.View
import cc.duduhuo.qpassword.R
import cc.duduhuo.qpassword.bean.Key
import cc.duduhuo.qpassword.bean.Password
import cc.duduhuo.qpassword.config.Config
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/31 21:02
 * Description: 工具集合
 * Remarks:
 * =======================================================
 */
private const val DAY = (1000 * 60 * 60 * 24).toLong()

private val simpleDateFormatYear = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val simpleDateFormatMonth = SimpleDateFormat("MM-dd", Locale.getDefault())

/**
 * 格式化时间
 * @param context
 * @param ms 毫秒
 */
fun formatDate(context: Context, ms: Long): String {
    val result: String
    val currentTime = System.currentTimeMillis()
    val distance = currentTime - ms
    result = when {
        distance < 0 -> simpleDateFormatYear.format(ms)
        distance < 1000 * 60 -> context.getString(R.string.just)
        distance < 1000 * 60 * 60 -> {
            val dateString = context.getString(R.string.minute_ago)
            String.format(Locale.getDefault(), dateString, distance / (1000 * 60))
        }
        distance < DAY -> {
            val dateString = context.getString(R.string.hour_ago)
            String.format(Locale.getDefault(), dateString, distance / (1000 * 60 * 60))
        }
        distance < DAY * 365 -> simpleDateFormatMonth.format(ms)
        else -> simpleDateFormatYear.format(ms)
    }

    return result
}

/**
 * 置顶排序的规则
 */
val mComparator = Comparator<Password> { lhs, rhs ->
    if (lhs.isTop || rhs.isTop) {
        if (lhs.isTop && rhs.isTop) {
            return@Comparator (rhs.createDate - lhs.createDate).toInt()
        } else if (lhs.isTop) {
            return@Comparator -1
        } else {
            return@Comparator 1
        }
    }
    val value = rhs.createDate - lhs.createDate
    return@Comparator when {
        value > 0 -> 1
        value == 0L -> 0
        else -> -1
    }
}

fun showSnackbar(view: View, @StringRes resId: Int, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(view, resId, duration).show()
}

fun showSnackbar(view: View, text: CharSequence, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(view, text, duration).show()
}

/**
 * 拷贝文本
 *
 * @param context
 * @param text    拷贝的文本
 */
fun copyText(context: Context, text: String) {
    val cmbName = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipDataName = ClipData.newPlainText(null, text)
    cmbName.setPrimaryClip(clipDataName)
}

/**
 * 分享文本
 *
 * @param context
 * @param text    分享的文本
 * @return true：成功打开分享面板；false：无法分享
 */
fun shareText(context: Context, text: String): Boolean {
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.type = "text/*"
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    val componentName = shareIntent.resolveActivity(context.packageManager)
    return if (componentName != null) {
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                context.getString(R.string.share_to)
            )
        )
        true
    } else {
        false
    }
}

/**
 * 打开浏览器
 *
 * @param context
 * @param url     浏览器加载的网址
 * @return true: 成功打开浏览器（或浏览器选择界面）；false：未安装浏览器
 */
fun openBrowser(context: Context, url: String): Boolean {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    val componentName = intent.resolveActivity(context.packageManager)
    return if (componentName != null) {
        context.startActivity(intent)
        true
    } else {
        false
    }
}

/**
 * 发送邮件
 *
 * @param context
 * @param email     邮件发送的地址
 * @param subject 邮件主题
 * @param text 邮件预置正文
 * @return true: 成功打开邮件应用，false：未安装邮件应用
 */
fun sendEmail(context: Context, email: String, subject: String, text: String): Boolean {
    val mailIntent = Intent(Intent.ACTION_SENDTO)
    mailIntent.data = Uri.parse("mailto:$email")
    mailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    mailIntent.putExtra(Intent.EXTRA_TEXT, text)
    val componentName = mailIntent.resolveActivity(context.packageManager)
    return if (componentName != null) {
        context.startActivity(mailIntent)
        true
    } else {
        false
    }
}

/**
 * 是否存在SD卡
 *
 * @return true：存在
 */
fun isExistSDCard(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

/**
 * 主密码 是否被 回收
 * @return true: 已被回收； false: 未被回收
 */
fun keyLost(): Boolean {
    if (Config.mKey == null) {
        return true
    } else if (Config.mKey!!.mode != Key.MODE_NO_KEY && Config.mOriKey == Config.NO_PASSWORD) {
        return true
    }
    return false
}

/**
 * 格式化数据量单位
 *
 * @param size
 * @return
 */
fun getFormatSize(size: Long): String {
    val kiloByte = size / 1024F
    if (kiloByte < 1F) {
        return "${size}B"
    }

    val megaByte = kiloByte / 1024F
    if (megaByte < 1F) {
        val result1 = BigDecimal(kiloByte.toString())
        return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB"
    }

    val gigaByte = megaByte / 1024F
    if (gigaByte < 1F) {
        val result2 = BigDecimal(megaByte.toString())
        return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB"
    }

    val teraBytes = gigaByte / 1024F
    if (teraBytes < 1F) {
        val result3 = BigDecimal(gigaByte.toString())
        return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB"
    }
    val result4 = BigDecimal(teraBytes.toString())
    return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB"
}
