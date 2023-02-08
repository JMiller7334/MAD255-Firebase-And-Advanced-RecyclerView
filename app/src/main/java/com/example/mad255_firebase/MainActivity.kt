package com.example.mad255_firebase

/*CONFIG STEPS
1. tools>firebase>realtime database
2. connect database; created firebase project for app
3. add realtime database
4. check gradle modules to ensure dependencies are good.
5. on console.firebase.google.com - under realtime fire base
    create database; config location, lock mode.
   A. in new database on firebase console under rules tab
    for now change read, write to true. THESE RULES ARE NOT OPTIMAL[NOTE]
   B. SAVE CHANGES.
*/

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationAttributes
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mad255_firebase.classes.RecyclerAdapter
import com.example.mad255_firebase.classes.SwipeGesture
import com.example.mad255_firebase.classes.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var rvMain: RecyclerView
    lateinit var rvAdapter: RecyclerAdapter

    private lateinit var database: DatabaseReference //this points to database in its entirety.
    private lateinit var database_userId: DatabaseReference
    private var database_array = mutableListOf<User>() //The list that is loaded with database data + used by recyclerview
    /*Below - create an object of Value Event listener
        implementation of members/override func required.

        this listener readers and writes to an arraylist when the data
        in the database has changed.
     */
    val database_changeListener: ValueEventListener = object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.hasChildren()){ //ensure not null
                database_array.clear()
                for (c in snapshot.children) {//for loop; looping through children
                    /*below attempts to place the data in c into the User class.*/
                    Log.i("database", c.key!!)
                    Log.i("database", c.getValue().toString())

                    val classHolder = mutableMapOf(
                        "id" to "nil",
                        "firstName" to "nil",
                        "lastName" to "nil",
                        "address" to "nil",
                        "street" to "nil")

                    for (value in c.children){
                        Log.i("database", "inserting ${value.key!!}")
                        classHolder[value.key!!] = value.getValue().toString()
                    }
                    val userToAdd = classHolder["id"]?.let {
                        User(
                            it, classHolder["firstName"],
                            classHolder["lastName"], classHolder["address"], classHolder["email"])
                    }
                    if (userToAdd != null) {
                        database_array.add(userToAdd)
                        rvAdapter.notifyDataSetChanged()// REMEMBER TO NOTIFY CHANGES ON ADAPTER!!!
                        Log.i("database", "rvList: ${database_array}")
                    }
                    //BELOW DOES NOT WORK; UNSURE WHY.
                    /*val dataHolder = c.getValue(User::class.java)
                    if (dataHolder != null) {
                        database_array.add(dataHolder)
                    }*/
                }
            }
        }
        override fun onCancelled(error: DatabaseError) {
            //Do something
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //FUNCTIONAL RECYCLERVIEW//
        rvMain = findViewById(R.id.rvUsers)
        rvAdapter = RecyclerAdapter(database_array)
        val swipeGesture = object : SwipeGesture(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){
                    //fires depending on the dir the user swipes.
                    ItemTouchHelper.LEFT ->{
                        val data2delete = database_array[viewHolder.adapterPosition].id
                        Log.i("database", "delete: ${data2delete}")
                        database_userId.child(data2delete).removeValue()
                        rvAdapter.deleteItem(viewHolder.adapterPosition)
                    }
                    ItemTouchHelper.RIGHT ->{
                        val data2delete = database_array[viewHolder.adapterPosition].id
                        Log.i("database", "delete: ${data2delete}")
                        database_userId.child(data2delete).removeValue()
                        rvAdapter.deleteItem(viewHolder.adapterPosition)
                    }
                }
                // super.onSwiped(viewHolder, direction) basecode: not used for this.
            }

            override fun onMove( //originally handled in the object class/kt file -
                //handled here on implementation of re-order feature.
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val positionFrom = viewHolder.adapterPosition
                val positionTo = target.adapterPosition

                Collections.swap(database_array, positionFrom, positionTo)//actively swaps items.
                rvAdapter.notifyItemMoved(positionFrom, positionTo) //informs the adapter of change.
                return false
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(rvMain)
        rvMain.adapter = rvAdapter
        //onClick Listener for entire list-item.
        rvAdapter.setOnItemClickListener(object: RecyclerAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                //do something
                Toast.makeText(this@MainActivity, "id: ${database_array[position].id}", Toast.LENGTH_SHORT).show()
            }
        })

        //DATABASE STUFF//
        //-----------------------------------------------------------------------
        //LESSON CODE/NOTES
        //set the database to JSON file that was created during firebase config.
        database = Firebase.database.reference

        //below points to just the users child in the database
        database_userId = Firebase.database.getReference("users/")


        //below creates a value to the database in a basic way
        //database.child("user").setValue("Jacob")


        //var currentUser = User("001", "jacob",
            //"miller", "001 street rd", "email@example.com")

        //creates row in the database placing the user class under a parent of the
        //user class id property.
        //database.child("users").child(currentUser.id).setValue(currentUser)

        database_userId.addValueEventListener(database_changeListener) //config database change listener.

        //CODE FOR FIELD//
        val buttonEnter = findViewById<Button>(R.id.btnEnter)
        val editTextMain = findViewById<EditText>(R.id.etMainField)

        val phases = arrayListOf("First Name", "Last Name", "Email", "Address")
        var currentId = 0//postion that will be edited
        var phase = 0
        var inputData = mutableListOf<String>() //a holder for the data

        editTextMain.hint = "Enter ${phases[phase]} for user id ${currentId+1}"
        buttonEnter.setOnClickListener {
            if (currentId > database_array.count()){
                currentId = database_array.count()
                editTextMain.hint = "Enter ${phases[phase]} for user id ${currentId+1}"
            }
            if (editTextMain.text.isNotEmpty()){
                inputData.add(editTextMain.text.toString())// insert input data into array
                Log.i("database", inputData[phase])
                phase = phase + 1
                if (phase >= 4){
                    phase = 0
                    currentId = currentId + 1
                    var newUser = User(currentId.toString(), inputData[0], inputData[1],
                        inputData[2], inputData[3])
                    database.child("users").child(newUser.id).setValue(newUser)
                    inputData.clear()
                    // Only runs if there is a view that is currently focused
                    this.currentFocus?.let { view ->
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                }
                editTextMain.text.clear()
                editTextMain.hint = "Enter ${phases[phase]} for user id ${currentId+1}"
            }
        }

        rvMain.layoutManager = LinearLayoutManager(this)
    }
    //for the database it is important to remove the listener here.
    override fun onDestroy() {
        super.onDestroy()
        database_userId.removeEventListener(database_changeListener)
    }
}