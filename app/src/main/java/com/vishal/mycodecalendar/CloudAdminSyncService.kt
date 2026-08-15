package com.vishal.mycodecalendar

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Date

/**
 * Data model for cloud-managed study materials & featured roadmap resources.
 */
data class CloudFeaturedMaterial(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "DSA",
    val imageUrl: String = "",
    val redirectUrl: String = "",
    val priority: Int = 1,
    val isActive: Boolean = true
)

/**
 * Data model for custom hackathons / community contests created via the Web Admin CMS.
 */
data class CloudCustomContest(
    val id: String = "",
    val name: String = "",
    val organizer: String = "",
    val bannerUrl: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val registrationUrl: String = "",
    val tags: List<String> = emptyList()
)

/**
 * Data model for global live announcements & broadcast banners created via Admin CMS.
 */
data class CloudBroadcast(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val badge: String = "ALERT",
    val actionUrl: String = "",
    val bannerImageUrl: String = "",
    val isActive: Boolean = true
)

/**
 * Central Cloud Sync Service linking the Android App with the CodeCalendar React Admin Web Portal.
 */
object CloudAdminSyncService {
    private const val TAG = "CloudAdminSyncService"
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Fetches current active announcement / broadcast banner for the Home Screen.
     */
    fun fetchCurrentBroadcast(
        onSuccess: (CloudBroadcast?) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        firestore.collection("broadcasts")
            .whereEqualTo("isActive", true)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    val broadcast = CloudBroadcast(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        subtitle = doc.getString("subtitle") ?: "",
                        badge = doc.getString("badge") ?: "NOTICE",
                        actionUrl = doc.getString("actionUrl") ?: "",
                        bannerImageUrl = doc.getString("bannerImageUrl") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true
                    )
                    onSuccess(broadcast)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to fetch cloud broadcast: ${e.message}")
                onError(e)
            }
    }

    /**
     * Registers / Updates user profile in Firestore `users` collection upon login or platform link.
     */
    fun syncUserProfileToCloud(
        uid: String?,
        displayName: String,
        method: String,
        email: String?,
        photoUrl: String?,
        connectedPlatforms: List<String> = emptyList(),
        connectedAccountsMap: Map<String, String> = emptyMap(),
        currentStreak: Int = 0
    ) {
        val targetUid = uid?.ifBlank { null } ?: email?.replace(".", "_") ?: displayName.replace(" ", "_")
        if (targetUid.isBlank()) return

        val userData = hashMapOf<String, Any>(
            "uid" to targetUid,
            "displayName" to displayName,
            "authProvider" to method,
            "email" to (email ?: ""),
            "photoUrl" to (photoUrl ?: ""),
            "connectedPlatforms" to connectedPlatforms,
            "connectedAccountsMap" to connectedAccountsMap,
            "currentStreak" to currentStreak,
            "streakCount" to currentStreak,
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "appVersion" to "1.0.0"
        )

        firestore.collection("users").document(targetUid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced user profile for $displayName ($method) to Firestore.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync user profile to Firestore: ${e.message}")
            }
    }

    /**
     * Submits an Account & Data Deletion Request to Firestore `deletion_requests` collection.
     * Complies with Google Play Data Safety requirements.
     */
    fun submitAccountDeletionRequest(
        uid: String?,
        email: String?,
        displayName: String?,
        reason: String = "User requested account and data deletion from App Settings",
        onComplete: (Boolean) -> Unit
    ) {
        val targetUid = uid?.ifBlank { null } ?: email?.replace(".", "_") ?: displayName ?: "unknown_user"
        val requestData = hashMapOf(
            "uid" to targetUid,
            "email" to (email ?: "Not provided"),
            "displayName" to (displayName ?: "Developer"),
            "reason" to reason,
            "status" to "PENDING",
            "requestedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("deletion_requests")
            .add(requestData)
            .addOnSuccessListener {
                Log.d(TAG, "Account deletion request created successfully.")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error submitting deletion request: ${e.message}")
                onComplete(false)
            }
    }

    /**
     * Fetches priority-ranked featured study materials and roadmap items from Firestore.
     */
    fun fetchCloudFeaturedMaterials(
        onSuccess: (List<CloudFeaturedMaterial>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        firestore.collection("featured_materials")
            .whereEqualTo("isActive", true)
            .orderBy("priority", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val materials = snapshot.documents.mapNotNull { doc ->
                    try {
                        CloudFeaturedMaterial(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "DSA",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            redirectUrl = doc.getString("redirectUrl") ?: "",
                            priority = doc.getLong("priority")?.toInt() ?: 1,
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onSuccess(materials)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching cloud materials: ${e.message}")
                onError(e)
            }
    }

    /**
     * Fetches custom community hackathons and contests from Firestore.
     */
    fun fetchCloudCustomContests(
        onSuccess: (List<CloudCustomContest>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        firestore.collection("custom_contests")
            .orderBy("startTime", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val contests = snapshot.documents.mapNotNull { doc ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        CloudCustomContest(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            organizer = doc.getString("organizer") ?: "",
                            bannerUrl = doc.getString("bannerUrl") ?: "",
                            startTime = doc.getTimestamp("startTime")?.toDate()?.time
                                ?: doc.getLong("startTime") ?: 0L,
                            endTime = doc.getTimestamp("endTime")?.toDate()?.time
                                ?: doc.getLong("endTime") ?: 0L,
                            registrationUrl = doc.getString("registrationUrl") ?: "",
                            tags = (doc.get("tags") as? List<String>) ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                onSuccess(contests)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error fetching custom contests: ${e.message}")
                onError(e)
            }
    }

    /**
     * Saves a connected platform handle (GitHub, LeetCode, Codeforces, etc.) under the user's Firestore document.
     */
    fun saveConnectedAccountToCloud(
        uid: String?,
        platform: String,
        username: String
    ) {
        val targetUid = uid?.ifBlank { null }
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: FirebaseAuth.getInstance().currentUser?.email?.replace(".", "_")
            ?: return
        if (targetUid.isBlank()) return

        val accountData = hashMapOf(
            "platform" to platform,
            "username" to username,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(targetUid)
            .collection("connected_accounts").document(platform.uppercase(java.util.Locale.ROOT))
            .set(accountData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced $platform ($username) to cloud for $targetUid")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync $platform account to cloud: ${e.message}")
            }

        // Also update the summary map and connectedPlatforms array in the user document
        val summaryUpdate = hashMapOf<String, Any>(
            "connectedAccountsMap.${platform.lowercase(java.util.Locale.ROOT)}" to username,
            "connectedPlatforms" to FieldValue.arrayUnion(platform.uppercase(java.util.Locale.ROOT)),
            "lastSyncedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("users").document(targetUid)
            .update(summaryUpdate)
            .addOnFailureListener {
                // If document does not exist yet, set with merge
                firestore.collection("users").document(targetUid)
                    .set(
                        hashMapOf(
                            "connectedAccountsMap" to mapOf(platform.lowercase(java.util.Locale.ROOT) to username),
                            "connectedPlatforms" to listOf(platform.uppercase(java.util.Locale.ROOT))
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
            }
    }

    /**
     * Deletes a connected platform account from the user's Firestore cloud storage.
     */
    fun deleteConnectedAccountFromCloud(
        uid: String?,
        platform: String
    ) {
        val targetUid = uid?.ifBlank { null }
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: FirebaseAuth.getInstance().currentUser?.email?.replace(".", "_")
            ?: return
        if (targetUid.isBlank()) return

        firestore.collection("users").document(targetUid)
            .collection("connected_accounts").document(platform.uppercase(java.util.Locale.ROOT))
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Deleted $platform account from cloud for $targetUid")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to delete $platform account from cloud: ${e.message}")
            }

        firestore.collection("users").document(targetUid)
            .update(
                mapOf(
                    "connectedAccountsMap.${platform.lowercase(java.util.Locale.ROOT)}" to FieldValue.delete(),
                    "connectedPlatforms" to FieldValue.arrayRemove(platform.uppercase(java.util.Locale.ROOT))
                )
            )
            .addOnFailureListener { /* Non-fatal */ }
    }

    /**
     * Fetches all cloud-linked platform handles for the user (LeetCode, GitHub, Codeforces, etc.)
     * and returns them as a Map<PlatformName, Handle>.
     */
    fun fetchConnectedAccountsFromCloud(
        uid: String?,
        onSuccess: (Map<String, String>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val targetUid = uid?.ifBlank { null }
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: FirebaseAuth.getInstance().currentUser?.email?.replace(".", "_")
            ?: run {
                onSuccess(emptyMap())
                return
            }
        if (targetUid.isBlank()) {
            onSuccess(emptyMap())
            return
        }

        // Fetch from user document first, then enrich with subcollection
        firestore.collection("users").document(targetUid)
            .get()
            .addOnSuccessListener { userDoc ->
                val combined = mutableMapOf<String, String>()
                @Suppress("UNCHECKED_CAST")
                val docMap = (userDoc.get("connectedAccountsMap") as? Map<String, String>) ?: emptyMap()
                for ((k, v) in docMap) {
                    if (v.isNotBlank()) combined[k.uppercase(java.util.Locale.ROOT)] = v
                }

                // Also check connected_accounts subcollection
                firestore.collection("users").document(targetUid)
                    .collection("connected_accounts")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            val platform = (doc.getString("platform") ?: doc.id).uppercase(java.util.Locale.ROOT)
                            val username = doc.getString("username")
                            if (!username.isNullOrBlank()) {
                                combined[platform] = username
                            }
                        }
                        Log.d(TAG, "Fetched ${combined.size} accounts from cloud for $targetUid")
                        onSuccess(combined)
                    }
                    .addOnFailureListener {
                        onSuccess(combined)
                    }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to read user doc, attempting subcollection: ${e.message}")
                firestore.collection("users").document(targetUid)
                    .collection("connected_accounts")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val result = mutableMapOf<String, String>()
                        for (doc in snapshot.documents) {
                            val platform = (doc.getString("platform") ?: doc.id).uppercase(java.util.Locale.ROOT)
                            val username = doc.getString("username")
                            if (!username.isNullOrBlank()) {
                                result[platform] = username
                            }
                        }
                        onSuccess(result)
                    }
                    .addOnFailureListener { err ->
                        onError(err)
                    }
            }
    }

    /**
     * Synchronizes the user's daily coding streak and active calendar dates to Firestore.
     */
    fun syncUserStreakToCloud(
        uid: String?,
        currentStreak: Int,
        activeDates: Set<String>
    ) {
        val targetUid = uid?.ifBlank { null }
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: FirebaseAuth.getInstance().currentUser?.email?.replace(".", "_")
            ?: return
        if (targetUid.isBlank()) return

        val streakData = hashMapOf(
            "currentStreak" to currentStreak,
            "streakCount" to currentStreak,
            "highestStreak" to currentStreak,
            "activeDates" to activeDates.toList(),
            "lastStreakSyncAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(targetUid)
            .set(streakData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Successfully synced streak ($currentStreak days, ${activeDates.size} dates) to cloud for $targetUid")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to sync streak to cloud: ${e.message}")
            }
    }

    /**
     * Fetches user streak and active dates from Firestore to restore on fresh install / multi-device login.
     */
    fun fetchUserStreakFromCloud(
        uid: String?,
        onSuccess: (currentStreak: Int, activeDates: Set<String>) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val targetUid = uid?.ifBlank { null }
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: FirebaseAuth.getInstance().currentUser?.email?.replace(".", "_")
            ?: run {
                onSuccess(0, emptySet())
                return
            }
        if (targetUid.isBlank()) {
            onSuccess(0, emptySet())
            return
        }

        firestore.collection("users").document(targetUid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val streak = doc.getLong("currentStreak")?.toInt()
                        ?: doc.getLong("streakCount")?.toInt()
                        ?: 0
                    @Suppress("UNCHECKED_CAST")
                    val datesList = (doc.get("activeDates") as? List<String>) ?: emptyList()
                    val datesSet = datesList.toSet()
                    Log.d(TAG, "Fetched cloud streak: $streak days, ${datesSet.size} dates for $targetUid")
                    onSuccess(streak, datesSet)
                } else {
                    onSuccess(0, emptySet())
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to fetch streak from cloud: ${e.message}")
                onError(e)
            }
    }
}

