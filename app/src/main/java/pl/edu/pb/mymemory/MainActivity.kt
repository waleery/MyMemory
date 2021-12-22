package pl.edu.pb.mymemory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    //will be set in onCreate metod
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        //Adapter provide a binding for the data set to the views of the RecyclerView | adapt each piece of data into a view
                                            //MainActivity is context, how many elements is in our grid
        rvBoard.adapter = MemoryBoardAdapter(this, 8)

        //optimisation - size of adapter always be defined as soon as the app boots up
        rvBoard.setHasFixedSize(true)

        //LayoutManager measures and positions item views
                                                    //MainActivity is context, number of columns
        rvBoard.layoutManager = GridLayoutManager(this, 2)


    }
}