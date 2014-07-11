package net.kozelka.contentcheck;

public class PathUtils {
    public static String stripJARNameFromPath(final String path){
        final int index = path.lastIndexOf("/");
        if(index != - 1) {
            return path.substring(index + 1);
        } else {
            return path;
        }
    }
}
