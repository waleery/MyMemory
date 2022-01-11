package pl.edu.pb.mymemory.models

import com.google.firebase.firestore.PropertyName

data class UserImageList(
        //image is key on firestore need to map to list
    @PropertyName("images") val images: List<String>? = null
)
