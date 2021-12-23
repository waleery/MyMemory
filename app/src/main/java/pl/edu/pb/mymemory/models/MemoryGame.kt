package pl.edu.pb.mymemory.models

import pl.edu.pb.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val  boardSize: BoardSize){

    val cards: List<MemoryCard>
    var numPairsFound = 0

    private var numCardFlips = 0
    private var indexOfSingleSelectedCard: Int? = null

    init {
        //chose images and doubling amount
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomizedImages = (chosenImages + chosenImages).shuffled()
        cards = randomizedImages.map{MemoryCard(it)}
    }

    //logic
    fun flipCard(position: Int): Boolean {
        numCardFlips++
        val card = cards[position]
        var foundMatch = false
        //Thre cases:
        // 0 crads previously flipped over => restore cards + flip over selected card
        // 1 card previously flipped over => flip over selected card + check if the images match
        // 2 cards previously flipped over => restore cards + flip over selected card

        if (indexOfSingleSelectedCard == null){
            //0 or 2 cards previously flipped over
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            // exactly 1 car previously flipped over
            foundMatch = checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int): Boolean {
        if (cards[position1].identifier != cards[position2].identifier){
            return false
        }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    //flip cards whose arent matched
    private fun restoreCards() {
        for (card in cards) {
            if(!card.isMatched){
                card.isFaceUp = false
            }
        }
    }

    //checking that user have won the game
    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }


    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return  numCardFlips / 2
    }
}

