package pl.edu.pb.mymemory

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import pl.edu.pb.mymemory.models.BoardSize
import pl.edu.pb.mymemory.models.MemoryGame

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }



    //will be set in onCreate method
    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter

    //choosing game mode
    private var boardSize: BoardSize = BoardSize.EASY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        //setting default textcolor
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))

        //making property MemoryGame
        memoryGame = MemoryGame(boardSize)


        //Adapter provide a binding for the data set to the views of the RecyclerView | adapt each piece of data into a view
                                            //MainActivity is context, how many elements is in our grid, list of pictures
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                Log.i(TAG, "Card clicked $position")
                updateGameWithFlip(position)
            }
        } )
        rvBoard.adapter = adapter

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvBoard.setHasFixedSize(true)

        //LayoutManager measures and positions item views
                                                    //MainActivity is context, number of columns
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())


    }
    //updating the memory game with attempted flip at this position
    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if (memoryGame.haveWonGame()){
            //Alert the user of an invalid move
            Snackbar.make(clRoot, "You have already won!",  Snackbar.LENGTH_SHORT).show()

            return
        }
        if (memoryGame.isCardFaceUp(position)){
            //Alert the user of an invalid move
            Snackbar.make(clRoot, "Invalid move!",  Snackbar.LENGTH_SHORT).show()
            return
        }

        //Flipping card - return boolean
        if (memoryGame.flipCard(position)){
            Log.i(TAG, "Found a match! Num pairs found: ${memoryGame.numPairsFound}")

            //changing textColor
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full),
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"


            if(memoryGame.haveWonGame())
            {
                Snackbar.make(clRoot, "You won!",  Snackbar.LENGTH_LONG).show()
            }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"

        //notifying MemoryBoardAdapt about data change
        adapter.notifyDataSetChanged()
    }
}