package com.tweener.firebase.firestore.model

import kotlinx.serialization.Serializable

/**
 * @author Vivien Mahe
 * @since 09/02/2024
 */

@Serializable
sealed class FirestoreModel {
    abstract var id: String // Firestore ID
}
