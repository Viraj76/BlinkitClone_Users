package com.example.userblinkitclone.models

import java.util.UUID

data class Bestseller(
    val id : String ? = null ,
    val productType : String ? = null,
    val products : ArrayList<Product> ? = null
)
