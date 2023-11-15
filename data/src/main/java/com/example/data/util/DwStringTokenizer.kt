package com.example.data.util

class DwStringTokenizer(var data: String, val deli: String){
    fun nextToken(): String {
        lateinit var buf: String
        if (data == null || deli == null) return ""
        val idx = data!!.indexOf(deli!!)
        if (idx < 0) {
            buf = data
            data = ""
            return buf!!
        }
        buf = data.substring(0, idx)
        data = data.substring(idx + deli?.length!!)
        return buf
    }
}