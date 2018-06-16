package com.lineate.xonix.mind.model;

import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.net.URLClassLoader;

@Builder
@Data
public class BotClassLoader {
    String className;

    URLClassLoader urlClassLoader;

    URL url;
}
