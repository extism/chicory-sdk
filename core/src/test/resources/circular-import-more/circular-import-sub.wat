(module

    (func $sub (export "sub") (param i32 i32) (result i32)
        (i32.sub
            (local.get 0)
            (local.get 1))
    )

)
