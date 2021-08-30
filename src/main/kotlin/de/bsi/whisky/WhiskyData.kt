package de.bsi.whisky

data class WhiskyData(val name: String, val wishCount: Int, val collectionCount: Int) {
    override fun toString() = "$name is in $wishCount wishlists and in $collectionCount collections."
}
