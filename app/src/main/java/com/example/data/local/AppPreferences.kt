package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bakri_print_prefs", Context.MODE_PRIVATE)

    private val _isPinEnabled = MutableStateFlow(prefs.getBoolean(KEY_PIN_ENABLED, false))
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _pinCode = MutableStateFlow(prefs.getString(KEY_PIN_CODE, "1234") ?: "1234")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _officeName = MutableStateFlow(prefs.getString(KEY_OFFICE_NAME, "بكري برنت للدعاية والإعلان") ?: "بكري برنت للدعاية والإعلان")
    val officeName: StateFlow<String> = _officeName.asStateFlow()

    private val _themeMode = MutableStateFlow(
        runCatching { AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name) }
            .getOrDefault(AppThemeMode.SYSTEM)
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Message Templates
    private val _templateReceived = MutableStateFlow(
        prefs.getString(
            KEY_TEMPLATE_RECEIVED,
            "مرحباً عميلنا العزيز {CLIENT}، تم استلام طلبكم بنجاح لدى {OFFICE}. الخدمة: {SERVICE} بمقاس {DIMENSIONS}. جاري التجهيز والطباعة بإتقان."
        ) ?: ""
    )
    val templateReceived: StateFlow<String> = _templateReceived.asStateFlow()

    private val _templateReady = MutableStateFlow(
        prefs.getString(
            KEY_TEMPLATE_READY,
            "مرحباً عميلنا العزيز {CLIENT}، يسعدنا إبلاغكم بأن طلبكم ({SERVICE} بمقاس {DIMENSIONS}) جاهز للاستلام الآن لدى {OFFICE}. المبلغ المتبقي: {BALANCE}. أهلاً وسهلاً بكم!"
        ) ?: ""
    )
    val templateReady: StateFlow<String> = _templateReady.asStateFlow()

    private val _templateInvoice = MutableStateFlow(
        prefs.getString(
            KEY_TEMPLATE_INVOICE,
            "🧾 تفاصيل الفاتورة - {OFFICE}\nالعميل: {CLIENT}\nالطلب: {SERVICE}\nالمقاس: {DIMENSIONS}\nالمساحة: {AREA} م²\nالإجمالي: {TOTAL}\nالمدفوع: {PAID}\nالمتبقي: {BALANCE}"
        ) ?: ""
    )
    val templateInvoice: StateFlow<String> = _templateInvoice.asStateFlow()

    fun setPinEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
        _isPinEnabled.value = enabled
    }

    fun setPinCode(code: String) {
        prefs.edit().putString(KEY_PIN_CODE, code).apply()
        _pinCode.value = code
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun setOfficeName(name: String) {
        prefs.edit().putString(KEY_OFFICE_NAME, name).apply()
        _officeName.value = name
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setTemplateReceived(text: String) {
        prefs.edit().putString(KEY_TEMPLATE_RECEIVED, text).apply()
        _templateReceived.value = text
    }

    fun setTemplateReady(text: String) {
        prefs.edit().putString(KEY_TEMPLATE_READY, text).apply()
        _templateReady.value = text
    }

    fun setTemplateInvoice(text: String) {
        prefs.edit().putString(KEY_TEMPLATE_INVOICE, text).apply()
        _templateInvoice.value = text
    }

    companion object {
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_PIN_CODE = "key_pin_code"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_OFFICE_NAME = "key_office_name"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_TEMPLATE_RECEIVED = "key_template_received"
        private const val KEY_TEMPLATE_READY = "key_template_ready"
        private const val KEY_TEMPLATE_INVOICE = "key_template_invoice"
    }
}
