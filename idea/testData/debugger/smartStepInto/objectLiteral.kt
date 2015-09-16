fun foo() {
    <caret>val a = object: A {
        val a = 1
        fun foo() = 1
        fun bar(): Int {
            return 1
        }
    }
}

open class A {
    fun fooBase() = 1
}

// EXISTS: foo(), bar()