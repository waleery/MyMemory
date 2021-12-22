package pl.edu.pb.mymemory.models

import pl.edu.pb.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val  boardSize: BoardSize){

    val cards: List<MemoryCard>
    val numPairsFound = 0

    init {
        //chose images and doubling amount
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map{MemoryCard(it)}
    }

    //logic
    fun flipCard(position: Int) {
        val card = cards[position]
        card.isFaceUp = !card.isFaceUp
    }
}

