package cc.duduhuo.qpassword.util

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/12/2 11:35
 * Description:
 * Remarks:
 * =======================================================
 */
fun IntArray.containsOnly(num: Int): Boolean = filter { it == num }.isNotEmpty()