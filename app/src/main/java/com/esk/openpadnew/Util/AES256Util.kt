package com.esk.openpadnew.Util

import org.apache.commons.codec.binary.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * AES256 암호화를 해주는 클래스
 * @param key 키
 */
class AES256Util(key: String) {
    private var iv: String = key.substring(0, 16)   // IV 값
    private lateinit var keySpec: Key               // KeySpec 값

    init {
        var keyBytes = ByteArray(16)
        val b: ByteArray = key.toByteArray()
        var len: Int = b.size
        if (len > keyBytes.size) {
            len = keyBytes.size
        }

        keyBytes = b.copyOf(len)
        keySpec = SecretKeySpec(keyBytes, "AES")
    }

    /**
     * 암호화
     * @param str 암호화 시킬 문장
     */
    fun aesEncode(str: String): String {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv.toByteArray()))
        val encrypted = cipher.doFinal(str.toByteArray())
        return String(Base64.encodeBase64(encrypted))
    }

    /**
     * 복호화
     * @param str 복호화 시킬 문장
     */
    fun aesDecode(str: String): String {
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv.toByteArray()))
        val byteStr = Base64.decodeBase64(str.toByteArray())
        return String(cipher.doFinal(byteStr), Charsets.UTF_8)
    }
}