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

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        val packageName = sbn?.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""
        val fullText = "$title $text"

        // 1. Text Extraction & Parsing (Regex / Rule-based)
        val parsedExpense = ExpenseParser.parse(fullText)

        if (parsedExpense != null) {
            Log.d("ExpenseListener", "Detected Expense: Amount = ${parsedExpense.amount}, Merchant = ${parsedExpense.merchant}")

            val currentTime = System.currentTimeMillis()
            

            // 2. Local Storage: Save to Room Database
            val database = AppDatabase.getDatabase(applicationContext)
            val roomExpense = Expense(
                amount = parsedExpense.amount.toString(),
                merchant = parsedExpense.merchant,
                date = currentTime
            )

            CoroutineScope(Dispatchers.IO).launch {
                database.expenseDao().insertExpense(roomExpense)
                Log.d("ExpenseListener", "Saved to Room Database!")
            }

            // 3. Backend & Security: Check Firebase Auth before cloud sync
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val firestoreData = hashMapOf(
                    "userId" to currentUser.uid,
                    "amount" to parsedExpense.amount,
                    "merchant" to parsedExpense.merchant,
                    "date" to currentTime
                )

                // Sync to user's private firestore collection
                firestore.collection("users")
                    .document(currentUser.uid)
                    .collection("expenses")
                    .add(firestoreData)
                    .addOnSuccessListener {
                        Log.d("ExpenseListener", "Successfully synced to Firebase Firestore!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ExpenseListener", "Error syncing to Firebase", e)
                    }
            } else {
                Log.w("ExpenseListener", "User not logged in. Saved locally to Room only.")
            }
        }
    }
}