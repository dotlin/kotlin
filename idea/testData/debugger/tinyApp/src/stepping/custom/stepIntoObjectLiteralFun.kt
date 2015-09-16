package stepIntoObjectLiteralFun

fun main(args: Array<String>) {
    //Breakpoint!
    test(object: A {
        override fun foo(): Int {
            return 1
        }
    })
}

interface A {
    fun foo(): Int
}

fun test(a: A) {
    a.foo()
}

// SMART_STEP_INTO_BY_INDEX: 1
