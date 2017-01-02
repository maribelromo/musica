package com.goosebay.musica;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maribel on 2016-12-29.
 */
public final class SearchUtils {

    private SearchUtils(){}

    // Subject Example: "Watch "JoJo Siwa - BOOMERANG (Official Video)" on YouTube"
    // TODO: smarter removal using regex, using hardcoded strings for now
    public static String getSearchTermFromSubject(String intentSubject){

        if (intentSubject == null)
            return "";

        intentSubject = intentSubject.toLowerCase();
        intentSubject = intentSubject.replace("\"","");
        intentSubject = intentSubject.replace("watch ","");
        intentSubject = intentSubject.replace(" on youtube","");

        // Regex expression to remove anything between () brackets (e.g. [Official video])
        String re = "\\([^()]*\\)";
        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(intentSubject);
        while (m.find()) {
            intentSubject = m.replaceAll("");
            m = p.matcher(intentSubject);
        }

        // Regex expression to remove anything between [] brackets (e.g. [Official video])
        re = "\\[[^\\[\\]]*\\]";
        p = Pattern.compile(re);
        m = p.matcher(intentSubject);
        while (m.find()) {
            intentSubject = m.replaceAll("");
            m = p.matcher(intentSubject);
        }

        return intentSubject;
    }
}
