package com.example.equipoOcho.view.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.RemoteViews
import com.example.equipoOcho.R
import com.example.equipoOcho.data.InventoryDB
import com.example.equipoOcho.view.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat

class InventoryWidgetProvider : AppWidgetProvider() {

    companion object {

        const val ACTION_TOGGLE_VISIBILITY =
            "com.example.equipoOcho.action.TOGGLE_VISIBILITY"

        private const val PREFS_NAME = "inventory_widget_prefs"
        private const val KEY_PREFIX = "show_balance_"

        private fun prefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        private fun isBalanceVisible(context: Context, appWidgetId: Int): Boolean =
            prefs(context).getBoolean(KEY_PREFIX + appWidgetId, false)

        private fun setBalanceVisible(
            context: Context,
            appWidgetId: Int,
            visible: Boolean
        ) {
            prefs(context).edit()
                .putBoolean(KEY_PREFIX + appWidgetId, visible)
                .apply()
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            Log.d("InventoryWidget", "updateAppWidget id=$appWidgetId")

            // 1. Obtener total de inventario desde Room
            val total = runBlocking(Dispatchers.IO) {
                val dao = InventoryDB.getDatabase(context).inventoryDao()
                dao.getTotalInventoryValue() ?: 0.0
            }

            val views = RemoteViews(context.packageName, R.layout.widget_inventory)

            // 2. Textos fijos
            views.setTextViewText(R.id.tvQuestion, "¿Cuánto tengo de inventario?")
            views.setTextViewText(R.id.tvInventoryTitle, "Inventory")
            views.setTextViewText(R.id.tvManage, "Gestionar inventario")

            // 3. Formateo del saldo
            val formatter = DecimalFormat("#,##0.00").apply {
                decimalFormatSymbols = decimalFormatSymbols.apply {
                    groupingSeparator = '.'
                    decimalSeparator = ','
                }
            }
            val formatted = formatter.format(total)

            // 4. Mostrar saldo o **** según preferencia
            val showBalance = isBalanceVisible(context, appWidgetId)

            if (showBalance) {
                views.setTextViewText(R.id.tvBalance, "$ $formatted")
                views.setImageViewResource(R.id.ivEye, R.drawable.ic_eye_closed_white)
            } else {
                views.setTextViewText(R.id.tvBalance, "$ ****")
                views.setImageViewResource(R.id.ivEye, R.drawable.ic_eye_open_white)
            }

            // 5. CLICK EN EL OJO → broadcast para alternar visibilidad
            val toggleIntent = Intent(context, InventoryWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_VISIBILITY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId, // requestCode único por widget
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.ivEye, togglePendingIntent)
            // (opcional) views.setOnClickPendingIntent(R.id.tvBalance, togglePendingIntent)

            // 6. CLICK EN “Gestionar inventario” → abrir MainActivity directamente
            val manageIntent = Intent(context, MainActivity::class.java).apply {
                // Muy importante: marcar que viene del widget
                putExtra("open_login_from_widget", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val managePendingIntent = PendingIntent.getActivity(
                context,
                10_000 + appWidgetId, // requestCode distinto al del ojo
                manageIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.ivManageIcon, managePendingIntent)
            views.setOnClickPendingIntent(R.id.tvManage, managePendingIntent)

            // 7. Actualizar el widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("InventoryWidget", "onReceive action=$action")

        if (action == ACTION_TOGGLE_VISIBILITY) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, InventoryWidgetProvider::class.java)

            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val current = isBalanceVisible(context, appWidgetId)
                setBalanceVisible(context, appWidgetId, !current)
                updateAppWidget(context, appWidgetManager, appWidgetId)
            } else {
                val ids = appWidgetManager.getAppWidgetIds(thisWidget)
                ids.forEach { id ->
                    val current = isBalanceVisible(context, id)
                    setBalanceVisible(context, id, !current)
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
            return
        }

        super.onReceive(context, intent)
    }
}