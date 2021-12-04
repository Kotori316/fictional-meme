module fictional.maven {
    exports com.kotori316.maven;
    requires java.base;
    requires jdk.crypto.ec;// required to access with SSL/TLS
    requires java.xml;
}
