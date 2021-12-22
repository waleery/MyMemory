package pl.edu.pb.mymemory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.edu.pb.mymemory.models.BoardSize
import pl.edu.pb.mymemory.models.MemoryGame

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
    }

    //will be set in onCreate method
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView

    //choosing game mode
    private var boardSize: BoardSize = BoardSize.HARD


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        //making object MemoryGame
        val memoryGame = MemoryGame(boardSize)


        //Adapter provide a binding for the data set to the views of the RecyclerView | adapt each piece of data into a view
                                            //MainActivity is context, how many elements is in our grid, list of pictures
        rvBoard.adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                Log.i(TAG, "Card clicked $position")
            }

        } )

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvBoard.setHasFixedSize(true)

        //LayoutManager measures and positions item views
                                                    //MainActivity is context, number of columns
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())


    }
}