package com.example.lifehelper.ui.transaction

/** 记账分类常量 */
object TransactionCategories {
    val expense = listOf("餐饮", "交通", "购物", "娱乐", "医疗", "学习", "其他")
    val income = listOf("工资", "兼职", "理财", "其他")

    fun categoriesFor(type: String): List<String> =
        if (type == "INCOME") income else expense
}
