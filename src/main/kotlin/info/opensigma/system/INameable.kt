package info.opensigma.system

interface INameable {
    val name: String
    @Deprecated("Use the name field instead", replaceWith = ReplaceWith("name"))
    fun getName(): String
}