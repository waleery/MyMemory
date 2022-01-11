package pl.edu.pb.mymemory.models

class MemoryCard(
    val identifier: Int,
    val imageUrl: String? = null, //optional - to custom game
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)