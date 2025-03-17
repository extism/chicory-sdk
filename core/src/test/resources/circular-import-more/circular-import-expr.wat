(module

    (import "add" "add" (func $add (param i32 i32) (result i32)))
    (import "sub" "sub" (func $sub (param i32 i32) (result i32)))

    (func $expr (export "expr") (result i32)
        (i32.const 20)
        (i32.const 50)
        (call $add)
        (i32.const 10)
        (call $sub)
    )

)
