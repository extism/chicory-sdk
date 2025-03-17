(module

    (import "env-2" "do_expr" (func $do_expr (result i32)))

    (func $real_do_expr (export "real_do_expr") (result i32)
        (call $do_expr)
    )
)