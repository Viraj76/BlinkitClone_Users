package com.example.userblinkitclone.utils

interface CartListener {

    fun showCartLayout(itemCount : Int)

    fun savingCartItemCount(itemCount : Int)

    fun hideCartLayout()


}