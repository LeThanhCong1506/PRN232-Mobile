package com.example.scamazon_frontend.core.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generates VietQR payment QR codes locally using the EMVCo QR Code standard.
 *
 * This eliminates the dependency on the external img.vietqr.io service,
 * which may be unreachable from the Android emulator.
 *
 * The generated QR follows the NAPAS/VietQR EMVCo specification and can be
 * scanned by all Vietnamese banking apps (MB Bank, Vietcombank, TPBank, etc.).
 *
 * Payment verification is handled separately via backend SePay webhook polling.
 */
object VietQRGenerator {

    /**
     * Generate EMVCo QR code content string for VietQR payment.
     *
     * @param bankBin Bank BIN code (e.g., "970422" for MB Bank)
     * @param accountNumber Bank account number
     * @param amount Payment amount in VND (whole number)
     * @param addInfo Transfer description / payment reference
     * @return EMVCo-compliant QR data string
     */
    fun generateQRContent(
        bankBin: String,
        accountNumber: String,
        amount: Long,
        addInfo: String
    ): String {
        // Build Beneficiary Organization (nested TLV inside field 01 of Merchant Account Info)
        // Sub-field 00: Acquirer ID (Bank BIN)
        // Sub-field 01: Merchant ID (Account Number)
        val beneficiaryOrg = tlv("00", bankBin) + tlv("01", accountNumber)

        // Build Merchant Account Information - NAPAS (field 38)
        // Sub-field 00: Globally Unique Identifier for NAPAS
        // Sub-field 01: Beneficiary Organization
        // Sub-field 02: Service Code (QRIBFTTA = QR Interbank Fund Transfer to Account)
        val merchantAccountInfo = tlv("00", "A000000727") +
                tlv("01", beneficiaryOrg) +
                tlv("02", "QRIBFTTA")

        // Build Additional Data Field Template (field 62)
        // Sub-field 08: Purpose of Transaction (transfer description)
        val additionalData = tlv("08", addInfo)

        // Assemble main QR payload (without CRC)
        val payload = tlv("00", "01") +              // Payload Format Indicator
                tlv("01", "12") +                     // Point of Initiation Method (12 = Dynamic QR)
                tlv("38", merchantAccountInfo) +      // Merchant Account Info (NAPAS)
                tlv("53", "704") +                    // Transaction Currency (704 = VND)
                tlv("54", amount.toString()) +        // Transaction Amount
                tlv("58", "VN") +                     // Country Code
                tlv("62", additionalData)             // Additional Data

        // Append CRC field ID + length placeholder, then calculate CRC-16/CCITT-FALSE
        val withCrcPlaceholder = payload + "6304"
        val crc = calculateCRC16(withCrcPlaceholder)

        return withCrcPlaceholder + crc
    }

    /**
     * Generate a QR code Bitmap from the given content string.
     *
     * @param content The QR code content (EMVCo data string)
     * @param size Width and height of the QR image in pixels
     * @return Bitmap containing the QR code, or null if generation fails
     */
    fun generateQRBitmap(content: String, size: Int = 800): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * TLV (Tag-Length-Value) encoding for EMVCo fields.
     * ID: 2-character field identifier
     * Length: 2-character zero-padded length of value
     * Value: the actual data
     */
    private fun tlv(id: String, value: String): String {
        val length = String.format("%02d", value.length)
        return "$id$length$value"
    }

    /**
     * Calculate CRC-16/CCITT-FALSE checksum as specified by EMVCo.
     * Polynomial: 0x1021, Initial value: 0xFFFF
     *
     * @param data The data string to calculate CRC for
     * @return 4-character uppercase hex string of the CRC value
     */
    private fun calculateCRC16(data: String): String {
        var crc = 0xFFFF
        for (byte in data.toByteArray(Charsets.UTF_8)) {
            crc = crc xor ((byte.toInt() and 0xFF) shl 8)
            for (i in 0 until 8) {
                crc = if (crc and 0x8000 != 0) {
                    (crc shl 1) xor 0x1021
                } else {
                    crc shl 1
                }
                crc = crc and 0xFFFF
            }
        }
        return String.format("%04X", crc)
    }
}
