package au.com.robin.sms.service

interface Connection {
    fun start()
    fun close()
}