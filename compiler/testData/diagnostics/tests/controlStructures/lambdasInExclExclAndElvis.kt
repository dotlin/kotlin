// !DIAGNOSTICS: -UNUSED_EXPRESSION

fun use(a: Any?) = a

fun test() {
    { }<!NOT_NULL_ASSERTION_ON_FUNCTION_LITERAL!>!!<!>
    use({ }<!NOT_NULL_ASSERTION_ON_FUNCTION_LITERAL!>!!<!>);

    // KT-KT-9070
    <!TYPE_MISMATCH!>{ }<!> <!USELESS_ELVIS!><!USELESS_ELVIS_ON_FUNCTION_LITERAL!>?:<!> 1<!>
    use(<!TYPE_MISMATCH!>{ 2 }<!> <!USELESS_ELVIS_ON_FUNCTION_LITERAL!>?:<!> 1);

    1 <!USELESS_ELVIS!>?: <!TYPE_MISMATCH, UNUSED_FUNCTION_LITERAL!>{ }<!><!>
    use(1 <!USELESS_ELVIS!>?: <!TYPE_MISMATCH!>{ }<!><!>)
}