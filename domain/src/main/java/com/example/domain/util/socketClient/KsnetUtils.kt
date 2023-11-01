package com.example.domain.util.socketClient

import java.util.Random
import kotlin.experimental.and


class KsnetUtils {
    private val reqData = arrayOf(
        "4|길이",
        "1|STX",
        "2|거래구분",
        "2|업무구분",
        "4|전문구분",
        "1|거래형태",
        "10|단말기번호",
        "4|업체정보",
        "12|전문일련번호",
        "1|POSEntryMode",
        "20|거래고유번호",
        "20|암호화하지않은카드번호",
        "1|암호화여부",
        "16|SW모델번호",
        "16|CAT",
        "40|암호화정보",
        "37|TRACEII",
        "1|FS",
        "2|구분",
        "12|총금액",
        "12|봉사료",
        "12|세금",
        "12|공급금액",
        "12|면세금액",
        "2|working",
        "16|비밀번호",
        "12|원거래승인번호",
        "6|원거래승인일자",
        "13|사용자정보",
        "2|가맹점ID",
        "30|가맹점사용필드",
        "4|Reserv",
        "20|KSNET Re",
        "1|동글구분",
        "1|매체구분",
        "1|이통사구분",
        "1|신용카드종류",
        "30|filter",
        "60|DCC환율"
    )
    private val respData = arrayOf(
        "4|길이|Length",
        "1|STX|STX",
        "2|거래구분|Classification",
        "2|업무구분|Approve",
        "4|전문구분|TelegramType",
        "1|거래형태|N",
        "10|단말기번호|Dpt_Id",
        "4|업체정보|Enterprise_Info",
        "12|전문일련번호|Full_Text_Num",
        "1|Status|Status",
        "4|KSNET응답코드|KsnetCode",
        "4|카드사응답코드|CardCord",
        "12|거래일시|Authdate",
        "1|카드TYPE|CardType",
        "16|카드종류/거절이유|Message1",
        "16|OK/거절이유|Message2",
        "12|승인번호|AuthNum",
        "20|거래고유번호|OriNumber",
        "15|가맹점번호|FranchiseID",
        "2|발급자코드|IssueCode",
        "16|카드종류명|CardName",
        "2|매입사코드|PurchaseCode",
        "16|메입사명|PurchaseName",
        "2|Working|Working",
        "16|Working Key|Working Key",
        "9|잔액|Remain",
        "9|포인트|point1",
        "9|포인트2|point2",
        "9|포인트3|point3",
        "20|Notice1|notice1",
        "40|Notice2|notice2",
        "5|KSNETR",
        "30|fillter|fillter"
    )

    fun generateString(length: Int): String {
        val rnd = Random()
        val buf = StringBuffer()
        for (i in 0 until length) {
            if (rnd.nextBoolean()) {
                buf.append((rnd.nextInt(26) as Int + 97).toChar())
            } else {
                buf.append(rnd.nextInt(10))
            }
        }
        return buf.toString()
    }

    fun reqDataPrint(buf: ByteArray?) {
        var start = 0
        var size = 0
        for (i in reqData.indices) {
            val dst = DwStringTokenizer(reqData[i], "|")
            val _buf = ByteArray(dst.nextToken()?.toInt()!!)
            size = _buf.size
            System.arraycopy(buf, start, _buf, 0, size)
            start += size
            try {
                println(
                    "reqDataPrint : " + (i + 1) + "=" + dst.nextToken() + "/" + String(_buf, Charsets.UTF_8)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun respGetHashData(buf: ByteArray): HashMap<String, String> {
        val map = HashMap<String, String>()
        var start = 0
        var size = 0
        map.clear()
        for (i in respData.indices) {
            val dst = DwStringTokenizer(respData.get(i), "|")
            val _size: String = dst.nextToken()
            val _notice1 = dst.nextToken()
            val _notice2 = dst.nextToken()
            val _buf = ByteArray(_size.toInt())
            if (_buf != null) size = _buf.size
            System.arraycopy(buf, start, _buf, 0, size)
            start += size
            try {
                println("respDataPrint : " + (i + 1) + "=" + _notice1 + "/" + _buf?.let { String(it, Charsets.UTF_8) })
                map[_notice2] = String(_buf, Charsets.UTF_8)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return map
    }

    fun byteToSubByte(buf: ByteArray, start: Int, length: Int): ByteArray {
        val _buf = ByteArray(length)
        if (start + length > buf.size) {
            return _buf
        }
        System.arraycopy(buf, start, _buf, 0, length)
        return _buf
        //   return  Arrays.copyOfRange(buf, start, end);
    }

    fun make2ByteLengh(i: Int): ByteArray? {
        val bArr = ByteArray(2)
        bArr[1] = i.toByte()
        bArr[0] = (i ushr 8).toByte()
        return bArr
    }

    fun byteToString(buf: ByteArray, start: Int, size: Int): String {
        try {
            return String(buf!!, start, size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun byte2Int(src: Byte): Int {
        return src.toInt() and 0xFF
    }

    fun byteToInt(b: Byte): Int {
        return b.toInt() and 0xFF
    }

    fun toHex(buf: ByteArray?, idx: Int): String? {
        val HEX = "0123456789ABCDEF"
        if (buf == null) {
            return ""
        }
        val result = StringBuffer(buf.size * 2)
        for (i in 0 until idx) {
            result.append(HEX[buf[i].toInt() shr 4 and 15]).append(HEX[(buf[i] and 15).toInt()])
        }
        return result.toString()
    }


}