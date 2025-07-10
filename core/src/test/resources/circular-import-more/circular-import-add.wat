(module

    (import "expr" "expr" (func $expr (result i32)))

    (func $add (export "add") (param i32 i32) (result i32)
        (i32.add
            (local.get 0)
            (local.get 1))
    )

    (func $do_expr (export "do_expr") (result i32)
        call $expr
    )

)
