package cc.duduhuo.qpassword.util

import cc.duduhuo.util.digest.Digest

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 23:27
 * Description: 扩展工具集合
 * Remarks:
 * =======================================================
 */
fun String.sha1Hex(): String {
    return Digest.sha1Hex(this)
}