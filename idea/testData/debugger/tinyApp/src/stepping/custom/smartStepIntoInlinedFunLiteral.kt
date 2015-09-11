package smartStepIntoInlinedFunLiteral

fun main(args: Array<String>) {
    val myClass = MyClass()
    //Breakpoint!
    myClass.f1 {
        test()
    }
}

class MyClass {
    fun f1(f1Param: () -> Unit): MyClass {
        f1Param()
        return this
    }
}

fun test() {}

// SMART_STEP_INTO_BY_INDEX: 2