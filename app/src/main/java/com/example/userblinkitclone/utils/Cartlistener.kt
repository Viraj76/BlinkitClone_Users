package com.example.userblinkitclone

interface CartListener {

    fun showCartLayout(itemCount : Int)

    fun savingCartItemCount(itemCount : Int)

    fun hideCartLayout()


}