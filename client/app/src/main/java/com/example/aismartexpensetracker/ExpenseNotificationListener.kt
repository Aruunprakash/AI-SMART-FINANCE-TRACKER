package com.example.aismartexpensetracker

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpenseNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "ExpenseListener"

        /**
         * Only notifications from these apps are parsed.
         *
         * Without this filter every notification on the device is scanned, so a
         * chat message containing "₹500" becomes a logged expense. Section 4.1
         * of the implementation plan requires this allowlist.
         *
         * Messaging apps are included deliberately: in India most bank alerts
         * arrive as SMS and surface through the default Messages app rather
         * than a bank app. ExpenseParser requires a transaction verb, which is
         * what keeps ordinary SMS out.
         */
        val PAYMENT_APP_PACKAGES = setOf(
            // UPI apps
            "com.google.android.apps.nbu.paisa.user",  // Google Pay (India)
            "com.phonepe.app",
            "net.one97.paytm",
            "in.org.npci.upiapp",                      // BHIM
            "in.amazon.mShop.android.shopping",        // Amazon Pay
            // Messaging apps (bank SMS alerts)
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms",
            // Bank apps
            "com.snapwork.hdfc",                       // HDFC
            "com.csam.icici.bank.imobile",             // ICICI iMobile
            "com.sbi.lotusintouch",                    // SBI YONO
            "com.sbi.SBIFreedomPlus",
            "com.msf.kbank.mobile",                    // Kotak
            "com.axis.mobile",                         // Axis
            "com.bankofbaroda.mconnect"
        )
    }

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName = sbn?.packageName ?: return
        if (packageName !in PAYMENT_APP_PACKAGES) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getString("android.title").orEmpty()
        // Long bank SMS get truncated in android.text; bigText has the full body.
        val body = extras.getString("android.bigText")
            ?: extras.getString("android.text").orEmpty()
        val fullText = "$title $body"

        // 1. Text extraction & parsing (regex / rule-based)
        val parsed = ExpenseParser.parse(fullText) ?: return

        // This is an expense tracker: money going out. Credits (salary, refunds)
        // are recognised by the parser but not stored as expenses, otherwise a
        // salary credit would inflate the spending total.
        if (parsed.type != "debit") {
            Log.d(TAG, "Ignoring ${parsed.type} of ${parsed.amount} from $packageName")
            return
        }

        Log.d(TAG, "Detected expense: ${parsed.amount} at ${parsed.merchant} (from $packageName)")

        // 2. Save locally and enrich, exactly as the manual "+" button does.
        val dao = AppDatabase.getDatabase(applicationContext).expenseDao()
        CoroutineScope(Dispatchers.IO).launch {
            ExpenseRepository.captureExpense(
                dao = dao,
                merchant = parsed.merchant,
                amount = parsed.amount
            )
            syncToFirestore(parsed)
        }
    }

    /**
     * Best-effort cloud sync. Every Firebase call is guarded because Firebase
     * throws if it was never initialised (missing or misconfigured
     * google-services.json), and losing cloud sync must never cost us the
     * locally captured transaction.
     */
    private fun syncToFirestore(parsed: ParsedExpense) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.i(TAG, "Not signed in; transaction saved locally only.")
                return
            }

            val data = hashMapOf(
                "userId" to currentUser.uid,
                "amount" to parsed.amount,
                "merchant" to parsed.merchant,
                "type" to parsed.type,
                "date" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(currentUser.uid)
                .collection("expenses")
                .add(data)
                .addOnSuccessListener { Log.d(TAG, "Synced to Firestore") }
                .addOnFailureListener { e -> Log.e(TAG, "Firestore sync failed", e) }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase unavailable; transaction saved locally only (${e.message})")
        }
    }
}
