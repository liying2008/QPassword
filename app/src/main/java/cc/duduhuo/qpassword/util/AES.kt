package cc.duduhuo.qpassword.util

import android.annotation.SuppressLint
import android.util.Base64
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

/**
 * =======================================================
 * Author: liying - liruoer2008@yeah.net
 * Datetime: 2017/10/28 18:37
 * Description: AES加密工具
 * Remarks:
 * =======================================================
 */
object AES {
    /**
     * AES加密
     */
    @Throws(Exception::class)
    fun encrypt(cleartext: String, seed: ByteArray): String {
        System.out.println("加密密钥：" + Arrays.toString(seed))
        val rawKey = getRawKey(seed)
        val result = encrypt(rawKey, cleartext.toByteArray())
        return Base64.encodeToString(result, 0)
    }

    /**
     * AES解密
     */
    @Throws(Exception::class)
    fun decrypt(encrypted: String, seed: ByteArray): String {
        System.out.println("解密密钥：" + Arrays.toString(seed))
        val rawKey = getRawKey(seed)
        val enc = Base64.decode(encrypted, 0)
        val result = decrypt(rawKey, enc)
        return String(result)
    }

    @SuppressLint("TrulyRandom")
    @Throws(Exception::class)
    private fun getRawKey(seed: ByteArray): ByteArray {
        val kgen = KeyGenerator.getInstance("AES")
        val sr = SecureRandom.getInstance("SHA1PRNG", "Crypto")
        sr.setSeed(seed)
        kgen.init(128, sr)
        val skey = kgen.generateKey()
        return skey.encoded
    }

    @Throws(Exception::class)
    private fun encrypt(raw: ByteArray, clear: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        return cipher.doFinal(clear)
    }

    @Throws(Exception::class)
    private fun decrypt(raw: ByteArray, encrypted: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        return cipher.doFinal(encrypted)
    }
}
