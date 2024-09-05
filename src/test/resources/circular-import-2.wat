(module

    (import "env-1" "expr" (func $expr (result i32)))

    (func $add (export "add") (result i32)
        (i32.add
            (i32.const 10)
            (i32.const 20))
    )

    (func $sub (export "sub") (result i32)
        (i32.sub
            (i32.const 10)
            (i32.const 20))
    )

    (func $do_expr (export "do_expr") (result i32)
        call $expr
    )


)
