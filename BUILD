java_binary(
    name = "echobot-jvm",
    srcs = glob(["src/main/java/**/*.java"]),
    data = ["//jvm-toxcore-c:libtox4j-c.so"],
    jvm_flags = ["-Djava.library.path=jvm-toxcore-c"],
    main_class = "im.tox.echobot.EchoBot",
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "//jvm-toxcore-api",
        "//jvm-toxcore-c",
        "@log4j_log4j//jar",
        "@org_slf4j_slf4j_log4j12//jar",
    ],
)
