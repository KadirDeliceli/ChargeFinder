package com.kadirdeliceli.chargefinder.ui.map

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.toColorInt
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Firma adına göre sabit bir renk döndürür.
// Bilinen Türk operatörleri için elle seçilmiş renkler, bilinmeyenler için
// isimden türetilen tutarlı bir renk (aynı firma her zaman aynı renk).
fun operatorColor(operatorName: String?): Int {
    if (operatorName.isNullOrBlank()) return "#6B7280".toColorInt() // gri (bilinmeyen)

    return when (operatorName.trim().lowercase()) {
        "zes" -> "#E11D48".toColorInt()          // kırmızı-pembe
        "eşarj", "esarj" -> "#2563EB".toColorInt() // mavi
        "voltrun" -> "#7C3AED".toColorInt()       // mor
        "sharz", "şarj" -> "#059669".toColorInt() // yeşil
        "trugo" -> "#EA580C".toColorInt()         // turuncu
        "astor" -> "#0891B2".toColorInt()         // camgöbeği
        "beefull" -> "#CA8A04".toColorInt()       // altın
        else -> {
            // Bilinmeyen firmalar için isimden tutarlı renk üret
            val hue = (operatorName.hashCode().rem(360).let { if (it < 0) it + 360 else it }).toFloat()
            android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.65f, 0.85f))
        }
    }
}

// Verilen renkte, "pin + içinde şimşek" şeklinde özel bir marker ikonu çizer.
fun createStationMarker(color: Int, isOperational: Boolean): BitmapDescriptor {
    val width = 84
    val height = 110
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val mainColor = if (isOperational) color else "#9CA3AF".toColorInt() // arızalıysa soluk gri

    // Gölge
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.argb(40, 0, 0, 0)
    }
    canvas.drawOval(RectF(22f, 96f, 62f, 108f), shadowPaint)

    // Pin gövdesi (damla şekli: üstte daire, altta sivri uç)
    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = mainColor
        style = Paint.Style.FILL
    }
    val radius = 34f
    val centerX = width / 2f
    val centerY = 38f

    // Üst daire
    canvas.drawCircle(centerX, centerY, radius, pinPaint)

    // Alt sivri uç (üçgen yol)
    val path = Path().apply {
        moveTo(centerX - 22f, centerY + 22f)
        lineTo(centerX, 100f)
        lineTo(centerX + 22f, centerY + 22f)
        close()
    }
    canvas.drawPath(path, pinPaint)

    // İç beyaz daire (şimşeğin zemini)
    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = Color.WHITE
    }
    canvas.drawCircle(centerX, centerY, radius * 0.62f, innerPaint)

    // Şimşek ikonu
    val boltPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = mainColor
        style = Paint.Style.FILL
    }
    val bolt = Path().apply {
        moveTo(centerX + 4f, centerY - 16f)
        lineTo(centerX - 10f, centerY + 3f)
        lineTo(centerX - 1f, centerY + 3f)
        lineTo(centerX - 4f, centerY + 16f)
        lineTo(centerX + 11f, centerY - 4f)
        lineTo(centerX + 1f, centerY - 4f)
        close()
    }
    canvas.drawPath(bolt, boltPaint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}