module fictional.meme {
    exports com.kotori316.fictional;
    requires java.base;
    requires com.google.gson;

    requires jdk.crypto.ec; // required to access with SSL/TLS
}
