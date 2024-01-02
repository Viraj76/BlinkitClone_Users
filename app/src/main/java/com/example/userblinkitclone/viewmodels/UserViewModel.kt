package com.example.userblinkitclone.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.userblinkitclone.utils.Constants
import com.example.userblinkitclone.utils.Utils
import com.example.userblinkitclone.api.ApiUtilities
import com.example.userblinkitclone.models.Bestseller
import com.example.userblinkitclone.models.Notification
import com.example.userblinkitclone.models.NotificationData
import com.example.userblinkitclone.models.Orders
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomdb.CartProductDao
import com.example.userblinkitclone.roomdb.CartProductTable
import com.example.userblinkitclone.roomdb.CartProductsDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewModel(application: Application) : AndroidViewModel(application) {

    //initializations
    val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("My_Pref", MODE_PRIVATE)
    val cartProductDao: CartProductDao =
        CartProductsDatabase.getDatabaseInstance(application).cartProductsDao()

    private val _paymentStatus = MutableStateFlow<Boolean>(false)
    val  paymentStatus = _paymentStatus

    // Room DB
    suspend fun insertCartProduct(products: CartProductTable) {
        cartProductDao.insertCartProduct(products)
    }

    fun getAll(): LiveData<List<CartProductTable>> {
        return cartProductDao.getAllCartProducts()
    }

    suspend fun deleteCartProducts(){
        cartProductDao.deleteCartProducts()
    }

    suspend fun updateCartProduct(products: CartProductTable) {
        cartProductDao.updateCartProduct(products)
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductDao.deleteCartProduct(productId)
    }


    // Firebase call
    fun fetchAllTheProducts(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }
    }

    fun getAllOrders() : Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").orderByChild("orderStatus")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = ArrayList<Orders>()
                for(orders in snapshot.children){
                    val order = orders.getValue(Orders::class.java)

                    if(order?.orderingUserId == Utils.getCurrentUserId()){
                        orderList.add(order)
                    }

                }
                trySend(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }
    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins")
            .child("ProductCategory/${category}")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        db.addValueEventListener(eventListener)

        awaitClose {
            db.removeEventListener(eventListener)
        }

    }

    fun getOrderedProducts(orderId : String) : Flow<List<CartProductTable>> = callbackFlow{
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orderId)
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}

    }

    fun updateItemCount(product: Product , itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)
    }

    fun saveProductsAfterOrder(stock : Int , product: CartProductTable){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productId}").child("itemCount").setValue(0)


        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productId}").child("productStock").setValue(stock)

        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productId}").child("productStock").setValue(stock)

    }

    fun saveUserAddress(address : String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)
    }

    fun getUserAddress(callback : (String?) -> Unit){
        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress")

        db.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val address = snapshot.getValue(String::class.java)
                    callback(address)
                }
                else{
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }

        })
    }
    fun saveAddress(address: String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)
    }

    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

    fun saveOrderedProducts(orders: Orders){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orders.orderId!!).setValue(orders)
    }

    fun fetchProductTypes() : Flow<List<Bestseller>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins/ProductType")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val productTypeList = ArrayList<Bestseller>()
                for(productType in snapshot.children){

                    val productTypeName = productType.key

                    val productList = ArrayList<Product>()

                    for(products in productType.children){
                        val product = products.getValue(Product::class.java)
                        productList.add(product!!)
                    }
                    val bestseller = Bestseller(productType = productTypeName , products = productList)
                    productTypeList.add(bestseller)
                }
                trySend(productTypeList)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        db.addValueEventListener(eventListener)
        awaitClose{db.removeEventListener(eventListener)}
    }



    // shared preferences
    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalCartItemCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddressStatus() {
        sharedPreferences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getAddressStatus() : MutableLiveData<Boolean>{
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    // retrofit

    suspend fun checkPayment(headers : Map<String, String>){
        val res = ApiUtilities.statusAPi.checkStatus(headers , Constants.MERCHANT_ID , Constants.merchantTransactionId)
        _paymentStatus.value = res.body() != null && res.body()!!.success
    }

    suspend fun sendNotification(adminUid : String , tittle : String , message : String){
        val getToken = FirebaseDatabase.getInstance().getReference("Admins").child("AdminInfo").child(adminUid).child("adminToken").get()
        getToken.addOnCompleteListener {task ->
        val token = task.result.getValue(String::class.java)
            val notification = Notification(token , NotificationData(tittle , message))
            ApiUtilities.notificationApi.sendNotification(notification).enqueue(object : Callback<Notification>{
                override fun onResponse(
                    call: Call<Notification>,
                    response: Response<Notification>
                ) {
                    if (response.isSuccessful){
                        Log.d("GGG" , "Sent Notification")
                    }
                }

                override fun onFailure(call: Call<Notification>, t: Throwable) {
                    TODO("Not yet implemented")
                }

            })

        }


    }
}