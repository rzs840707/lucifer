package com.iscas.util;

import java.io.File;

public class FileUtil {
    public static boolean existFile(String path) {
        File f = new File(path);
        return f.exists() && f.isFile();
    }
}
