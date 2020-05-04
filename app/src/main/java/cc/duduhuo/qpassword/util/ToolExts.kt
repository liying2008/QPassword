package cc.duduhuo.qpassword.util

import cc.duduhuo.util.crypto.AES
import cc.duduhuo.util.digest.Digest

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/11/12 23:27
 * Description: 扩展工具集合
 * Remarks:
 * =======================================================
 */

/**
 * 得到字符串的 SHA-1 值
 */
fun String.sha1Hex(): String = Digest.sha1Hex(this)

/**
 * 将字符串采用 AES 加密
 */
fun String.aesEncrypt(seed: String): String = AES.encrypt(this, seed)

/**
 * 将加密字符串采用 AES 解密
 */
fun String.aesDecrypt(seed: String): String = AES.decrypt(this, seed)